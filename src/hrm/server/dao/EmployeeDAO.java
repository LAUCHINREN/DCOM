package hrm.server.dao;

import hrm.common.model.EmployeeProfile;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.UUID;

public class EmployeeDAO {

    public EmployeeProfile updateEmployeeProfile(EmployeeProfile profile) throws Exception {

        Connection conn = DBConnection.getConnection();
        String sql1 = "UPDATE employee SET first_name = ?, last_name = ?, contact_num = ?, email = ? WHERE emp_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql1)) {
            ps.setString(1, profile.getFirstName());
            ps.setString(2, profile.getLastName());
            ps.setString(3, profile.getContactNum());
            ps.setString(4, profile.getEmail());
            ps.setObject(5, UUID.fromString(profile.getEmpId()));
            ps.executeUpdate();
        }

        String sql2 = "UPDATE employee_family SET first_name = ?, contact_num = ? WHERE emp_id = ? AND is_emergency_contact = true";

        try (PreparedStatement ps = conn.prepareStatement(sql2)) {
            ps.setString(1, profile.getEmergencyName());
            ps.setString(2, profile.getEmergencyContact());
            ps.setObject(3, UUID.fromString(profile.getEmpId()));
            ps.executeUpdate();
        }

        return profile;
    }
}