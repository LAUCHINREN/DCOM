package hrm.server.dao;

import hrm.common.model.LeaveApplication;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

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
        // String sql = "INSERT INTO leave_applications " +
        //         "(id, leave_type, start_date, end_date, total_days, reason, applied_at, status) " +
        //         "SELECT e.id, ?, ?, ?, ?, ?, 'PENDING', CURRENT_DATE " +
        //         "FROM employees e WHERE e.ic_number = ? " +
        //         "RETURNING id, applied_at";
        String sql = "INSERT INTO leave_applications " +
                "(leave_type, start_date, end_date, total_days, reason, applied_at, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?) " + // OR "VALUES (?, ?, ?, ?, ?, CURRENT_DATE, ?) " //NOW(), 'PENDING'
                "RETURNING id, applied_at";

        try (PreparedStatement ps = DBConnection.getConnection()
                .prepareStatement(sql)) { //, Statement.RETURN_GENERATED_KEYS)

            ps.setString(1, app.getLeaveType());
            ps.setDate(2, Date.valueOf(app.getStartDate()));
            ps.setDate(3, Date.valueOf(app.getEndDate()));
            ps.setLong(4, 10); //app.getTotalDays()
            ps.setString(5, app.getReason());
            ps.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
            ps.setString(7, "PENDING");

            // ps.executeUpdate();

            // ResultSet keys = ps.getGeneratedKeys();
            // if (keys.next()) {
            //     app.setId(keys.getObject("id").toString());
            //     app.setAppliedAt(keys.getDate("applied_at").toLocalDate());
            // }

            System.out.println("[DEBUG] SQL: " + sql);
            System.out.println("[DEBUG] leaveType: " + app.getLeaveType());
            System.out.println("[DEBUG] startDate: " + app.getStartDate());
            System.out.println("[DEBUG] endDate:   " + app.getEndDate());
            System.out.println("[DEBUG] totalDays: " + app.getTotalDays());
            System.out.println("[DEBUG] reason:    " + app.getReason());

            ResultSet rs = ps.executeQuery();  // Use executeQuery() for RETURNING
            if (rs.next()) {
                app.setId(rs.getString("id"));  // Get generated UUID
                app.setAppliedAt(rs.getTimestamp("applied_at").toLocalDateTime()); //rs.getDate
            }
        }
        return app;
    }
}
