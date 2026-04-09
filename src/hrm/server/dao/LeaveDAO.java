package hrm.server.dao;

import hrm.common.model.LeaveApplication;

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

        String sql = "SELECT la.*, lc.leave_type " +
                "FROM leave_application la " +
                "JOIN leave_category lc ON la.leave_type_id = lc.leave_type_id " +
                "WHERE la.emp_id = ? ORDER BY la.applied_at DESC";

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
}
