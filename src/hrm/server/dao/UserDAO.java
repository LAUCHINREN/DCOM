package hrm.server.dao;

import hrm.common.model.EmployeeProfile;
import hrm.common.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;

public class UserDAO {

    public void registerUser(EmployeeProfile emp, User user) throws Exception {

        Connection conn = DBConnection.getConnection();

        try {
            conn.setAutoCommit(false);

            // =========================
            // CHECK DUPLICATE USERNAME
            // =========================
            String checkSql = "SELECT COUNT(*) FROM \"user\" WHERE username = ?";

            try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                ps.setString(1, user.getUsername());
                ResultSet rs = ps.executeQuery();

                if (rs.next() && rs.getInt(1) > 0) {
                    throw new Exception("Username already exists!");
                }
            }

            UUID empId;

            // =========================
            // INSERT EMPLOYEE (AUTO ID)
            // =========================
            String empSql = """
                INSERT INTO employee (
                    first_name, last_name,
                    identification_num,
                    contact_num, email, hire_date, employment_status
                )
                VALUES (?, ?, ?, ?, ?, ?, 'ACTIVE')
                RETURNING emp_id
            """;

            try (PreparedStatement ps = conn.prepareStatement(empSql)) {
                ps.setString(1, emp.getFirstName());
                ps.setString(2, emp.getLastName());
                ps.setString(3, emp.getIdentificationNum());
                ps.setString(4, emp.getContactNum());
                ps.setString(5, emp.getEmail());
                ps.setDate(6, java.sql.Date.valueOf(emp.getHireDate()));

                ResultSet rs = ps.executeQuery();
                rs.next();
                empId = (UUID) rs.getObject("emp_id");
            }

            // =========================
            // INSERT USER (AUTO user_id)
            // =========================
            String userSql = """
                INSERT INTO "user" (
                    emp_id, username, password, role, status, created_at
                ) VALUES (?, ?, ?, ?, 'ACTIVE', NOW())
            """;

            try (PreparedStatement ps = conn.prepareStatement(userSql)) {
                ps.setObject(1, empId);
                ps.setString(2, user.getUsername());
                ps.setString(3, user.getPassword());
                ps.setString(4, user.getRole());
                ps.executeUpdate();
            }

            String leaveSql = """
                INSERT INTO leave_balance (
                    leave_id, emp_id, leave_type_id, year,
                    total_quota, applied, balance, carry_forward
                )
                SELECT gen_random_uuid(), ?, lc.leave_type_id,
                       EXTRACT(YEAR FROM CURRENT_DATE),
                       lc.annual_quota, 0, lc.annual_quota, lc.annual_quota
                FROM leave_category lc
            """;

            try (PreparedStatement ps = conn.prepareStatement(leaveSql)) {
                ps.setObject(1, empId);
                ps.executeUpdate();
            }

            conn.commit();

        } catch (Exception e) {
            conn.rollback();

            if (e.getMessage() != null &&
                    e.getMessage().toLowerCase().contains("duplicate")) {
                throw new Exception("Username already exists!");
            }

            throw e;
        }
    }
}