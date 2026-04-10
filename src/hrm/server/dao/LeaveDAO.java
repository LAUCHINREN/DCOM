package hrm.server.dao;

import hrm.common.model.LeaveApplication;
import hrm.common.model.LeaveBalance;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.ArrayList;

/**
 * Data Access Object for all leave-related SQL.
 * Only this class (and other DAOs) are allowed to write SQL.
 * Service impl classes call these methods — they never write SQL themselves.
 */

public class LeaveDAO {

    // ------------------------------------------------------------------
    // Leave Applications
    // ------------------------------------------------------------------

    /**
     * Insert a new leave application. Returns the application with its
     * database-assigned ID filled in.
     */

    public LeaveApplication insertApplication(LeaveApplication app) throws SQLException {

        String sql = "INSERT INTO leave_application " +
                "(emp_id, leave_type_id, start_datetime, end_datetime, duration, applied_at, applied_status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?) " +
                "RETURNING apply_id, applied_at";

        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {

            long totalDays = ChronoUnit.DAYS.between(app.getStartDate(), app.getEndDate()) + 1;
            app.setTotalDays(totalDays);

            ps.setObject(1, java.util.UUID.fromString(app.getEmployeeId()));
            ps.setObject(2, getLeaveTypeId(app.getLeaveType()));
            ps.setTimestamp(3, Timestamp.valueOf(app.getStartDate().atStartOfDay()));
            ps.setTimestamp(4, Timestamp.valueOf(app.getEndDate().atStartOfDay()));
            ps.setBigDecimal(5, new java.math.BigDecimal(totalDays));
            ps.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
            ps.setString(7, "PENDING");

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                app.setId(rs.getString("apply_id"));
                app.setAppliedAt(rs.getTimestamp("applied_at").toLocalDateTime());
            }
        }

        return app;
    }

    public List<LeaveApplication> getApplicationsByEmployee(String empId) throws SQLException {

        String sql = "SELECT la.*, lc.leave_type, e.first_name, e.last_name " +
                "FROM leave_application la " +
                "JOIN leave_category lc ON la.leave_type_id = lc.leave_type_id " +
                "JOIN employee e ON la.emp_id = e.emp_id " +
                "WHERE la.emp_id = ? " +
                "ORDER BY la.applied_at DESC";

        List<LeaveApplication> list = new ArrayList<>();

        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {

            ps.setObject(1, java.util.UUID.fromString(empId));
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                LeaveApplication app = new LeaveApplication();

                app.setId(rs.getString("apply_id"));
                app.setEmployeeId(rs.getString("emp_id"));

                app.setStartDate(
                        rs.getTimestamp("start_datetime").toLocalDateTime().toLocalDate());

                app.setEndDate(
                        rs.getTimestamp("end_datetime").toLocalDateTime().toLocalDate());

                app.setTotalDays(rs.getLong("duration"));
                app.setStatus(rs.getString("applied_status"));

                app.setAppliedAt(
                        rs.getTimestamp("applied_at").toLocalDateTime());

                // optional (since using FK)
                app.setLeaveType(rs.getString("leave_type"));

                app.setApprovedBy(rs.getString("approved_by"));

                Timestamp approvedDateTs = rs.getTimestamp("approved_date");
                if (approvedDateTs != null) {
                    app.setApprovedDate(approvedDateTs.toLocalDateTime());
                }

                app.setFirstName(rs.getString("first_name"));
                app.setLastName(rs.getString("last_name"));

                list.add(app);
            }
        }

        return list;
    }

    private Object getLeaveTypeId(String leaveType) throws SQLException {

        String sql = "SELECT leave_type_id FROM leave_category WHERE leave_type = ?";

        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, leaveType);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getObject("leave_type_id");
            } else {
                throw new SQLException("Invalid leave type: " + leaveType);
            }
        }
    }

    public List<LeaveApplication> getPendingApplications() throws SQLException {

        String sql = "SELECT la.*, lc.leave_type, e.first_name, e.last_name " +
                "FROM leave_application la " +
                "JOIN leave_category lc ON la.leave_type_id = lc.leave_type_id " +
                "JOIN employee e ON la.emp_id = e.emp_id " +
                "WHERE la.applied_status = 'PENDING' " +
                "ORDER BY la.applied_at ASC";

        List<LeaveApplication> list = new ArrayList<>();

        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                LeaveApplication app = new LeaveApplication();

                app.setId(rs.getString("apply_id"));
                app.setEmployeeId(rs.getString("emp_id"));
                app.setLeaveType(rs.getString("leave_type"));

                app.setStartDate(rs.getTimestamp("start_datetime")
                        .toLocalDateTime().toLocalDate());

                app.setEndDate(rs.getTimestamp("end_datetime")
                        .toLocalDateTime().toLocalDate());

                app.setTotalDays(rs.getLong("duration"));
                app.setStatus(rs.getString("applied_status"));

                app.setAppliedAt(rs.getTimestamp("applied_at")
                        .toLocalDateTime());
                app.setFirstName(rs.getString("first_name"));
                app.setLastName(rs.getString("last_name"));

                list.add(app);
            }
        }

        return list;
    }

    public void updateLeaveStatus(String applicationId, String status, String approvedBy)
            throws SQLException {

        String sql = "UPDATE leave_application " +
                "SET applied_status = ?, approved_by = ?, approved_date = NOW() " +
                "WHERE apply_id = ?";

        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setObject(2, java.util.UUID.fromString(approvedBy));
            ps.setObject(3, java.util.UUID.fromString(applicationId));

            ps.executeUpdate();
        }
    }

    public List<LeaveBalance> getLeaveBalance(String empId) throws SQLException {

        String sql = "SELECT lb.*, lc.leave_type " +
                "FROM leave_balance lb " +
                "JOIN leave_category lc ON lb.leave_type_id = lc.leave_type_id " +
                "WHERE lb.emp_id = ? AND lb.year = EXTRACT(YEAR FROM CURRENT_DATE)";

        List<LeaveBalance> list = new ArrayList<>();

        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {

            ps.setObject(1, java.util.UUID.fromString(empId));
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                LeaveBalance lb = new LeaveBalance();

                lb.setLeaveId(rs.getString("leave_id"));
                lb.setEmpId(rs.getString("emp_id"));
                lb.setLeaveType(rs.getString("leave_type"));
                lb.setYear(rs.getInt("year"));
                lb.setTotalQuota(rs.getBigDecimal("total_quota"));
                lb.setApplied(rs.getBigDecimal("applied"));
                lb.setBalance(rs.getBigDecimal("balance"));
                lb.setCarryForward(rs.getBigDecimal("carry_forward"));

                list.add(lb);
            }
        }

        return list;
    }

    public void deductLeaveBalance(String empId, String leaveType, long days) throws SQLException {

        String sql = "UPDATE leave_balance lb " +
                "SET applied = applied + ?, " +
                "balance = total_quota - (applied + ?) " +
                "FROM leave_category lc " +
                "WHERE lb.leave_type_id = lc.leave_type_id " +
                "AND lc.leave_type = ? " +
                "AND lb.emp_id = ? " +
                "AND lb.year = EXTRACT(YEAR FROM CURRENT_DATE)";

        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setBigDecimal(1, new java.math.BigDecimal(days));
            ps.setBigDecimal(2, new java.math.BigDecimal(days));
            ps.setString(3, leaveType);
            ps.setObject(4, java.util.UUID.fromString(empId));

            ps.executeUpdate();
        }
    }

    public void initializeLeaveBalance(String empId) throws SQLException {

        String sql = "INSERT INTO leave_balance " +
                "(leave_id, emp_id, leave_type_id, year, total_quota, applied, balance, carry_forward) " +
                "SELECT gen_random_uuid(), ?, lc.leave_type_id, EXTRACT(YEAR FROM CURRENT_DATE), " +
                "lc.annual_quota, 0, lc.annual_quota, 0 " +
                "FROM leave_category lc";

        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {

            ps.setObject(1, java.util.UUID.fromString(empId));
            ps.executeUpdate();
        }
    }

    public LeaveApplication getApplicationById(String applicationId) throws SQLException {

        String sql = "SELECT la.*, lc.leave_type " +
                "FROM leave_application la " +
                "JOIN leave_category lc ON la.leave_type_id = lc.leave_type_id " +
                "WHERE la.apply_id = ?";

        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {

            ps.setObject(1, java.util.UUID.fromString(applicationId));
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                LeaveApplication app = new LeaveApplication();

                app.setId(rs.getString("apply_id"));
                app.setEmployeeId(rs.getString("emp_id"));
                app.setLeaveType(rs.getString("leave_type"));
                app.setTotalDays(rs.getLong("duration"));
                app.setStatus(rs.getString("applied_status"));

                return app;
            }
        }

        return null;
    }
}
