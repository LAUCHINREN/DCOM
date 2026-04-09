package hrm.server.dao;

import hrm.common.model.EmployeeProfile;
import hrm.common.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.UUID;

public class UserDAO {

    public void registerUser(EmployeeProfile emp, User user) throws Exception {

        Connection conn = DBConnection.getConnection();

        try {
            conn.setAutoCommit(false);

            UUID empId = UUID.randomUUID();

            // INSERT EMPLOYEE
            String empSql = """
                INSERT INTO employee (
                    emp_id, first_name, last_name,
                    contact_num, email, employment_status
                ) VALUES (?, ?, ?, ?, ?, 'ACTIVE')
            """;

            try (PreparedStatement ps = conn.prepareStatement(empSql)) {
                ps.setObject(1, empId);
                ps.setString(2, emp.getFirstName());
                ps.setString(3, emp.getLastName());
                ps.setString(4, emp.getContactNum());
                ps.setString(5, emp.getEmail());
                ps.executeUpdate();
            }

            // INSERT USER
            String userSql = """
                INSERT INTO "user" (
                    user_id, emp_id, username, password, role, status
                ) VALUES (?, ?, ?, ?, ?, 'ACTIVE')
            """;

            try (PreparedStatement ps = conn.prepareStatement(userSql)) {
                ps.setObject(1, UUID.randomUUID());
                ps.setObject(2, empId);
                ps.setString(3, user.getUsername());
                ps.setString(4, user.getPassword());
                ps.setString(5, user.getRole());
                ps.executeUpdate();
            }

            conn.commit();

        } catch (Exception e) {
            conn.rollback();
            throw e;
        }
    }
}