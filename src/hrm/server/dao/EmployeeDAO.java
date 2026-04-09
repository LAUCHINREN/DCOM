package hrm.server.dao;

import hrm.common.model.EmployeeProfile;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;
import java.sql.ResultSet;

public class EmployeeDAO {

    public EmployeeProfile updateEmployeeProfile(EmployeeProfile profile) throws Exception {

        Connection conn = DBConnection.getConnection();
        String sql1 = "UPDATE employee SET first_name = ?, last_name = ?, contact_num = ?, email = ?, gender = ?, dob = ? WHERE emp_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql1)) {
            ps.setString(1, profile.getFirstName());
            ps.setString(2, profile.getLastName());
            ps.setString(3, profile.getContactNum());
            ps.setString(4, profile.getEmail());
            ps.setString(5, profile.getGender());
            ps.setObject(6, profile.getDob());
            ps.setObject(7, UUID.fromString(profile.getEmpId()));
            int rows = ps.executeUpdate();

            if (rows == 0) {
                throw new Exception("Employee not found.");
            }
        }

        String sql2 = "UPDATE employee_family SET first_name = ?, contact_num = ? WHERE emp_id = ? AND is_emergency_contact = true";

        try (PreparedStatement ps = conn.prepareStatement(sql2)) {
            ps.setString(1, profile.getEmergencyName());
            ps.setString(2, profile.getEmergencyContact());
            ps.setObject(3, UUID.fromString(profile.getEmpId()));

            int rows = ps.executeUpdate();

            if (rows == 0) {
                // ensure no duplicate emergency contact
                String clearSql = "UPDATE employee_family SET is_emergency_contact = false WHERE emp_id = ?";
                try (PreparedStatement psClear = conn.prepareStatement(clearSql)) {
                    psClear.setObject(1, UUID.fromString(profile.getEmpId()));
                    psClear.executeUpdate();
                }

                String insertSql = "INSERT INTO employee_family (fam_id, emp_id, first_name, contact_num, is_emergency_contact) VALUES (?, ?, ?, ?, true)";
                try (PreparedStatement ps2 = conn.prepareStatement(insertSql)) {
                    ps2.setObject(1, UUID.randomUUID());
                    ps2.setObject(2, UUID.fromString(profile.getEmpId()));
                    ps2.setString(3, profile.getEmergencyName());
                    ps2.setString(4, profile.getEmergencyContact());
                    ps2.executeUpdate();
                }
            }
        }

        return profile;
    }

    public List<EmployeeProfile> getFamilyMembers(String empId) throws Exception {
        List<EmployeeProfile> list = new ArrayList<>();

        String sql = "SELECT * FROM employee_family WHERE emp_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setObject(1, UUID.fromString(empId));
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                EmployeeProfile p = new EmployeeProfile();

                p.setFamId(rs.getString("fam_id"));
                p.setEmpId(empId);
                p.setFirstName(rs.getString("first_name"));
                p.setFamilyLastName(rs.getString("last_name"));
                p.setIdentificationNum(rs.getString("identification_num"));
                p.setContactNum(rs.getString("contact_num"));

                list.add(p);
            }
        }

        return list;
    }

    public void addFamilyMember(EmployeeProfile profile) throws Exception {

        String sql = "INSERT INTO employee_family " +
                "(fam_id, emp_id, first_name, last_name, identification_num, contact_num, is_emergency_contact) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setObject(1, UUID.randomUUID());
            ps.setObject(2, UUID.fromString(profile.getEmpId()));
            ps.setString(3, profile.getFirstName());
            ps.setString(4, profile.getFamilyLastName());
            ps.setString(5, profile.getIdentificationNum());
            ps.setString(6, profile.getContactNum());
            ps.setBoolean(7, false); // default

            int rows = ps.executeUpdate();

            if (rows == 0) {
                throw new Exception("Employee not found.");
            }
        }
    }

    public void updateFamilyMember(EmployeeProfile profile) throws Exception {

        String sql = "UPDATE employee_family \n" +
                "SET first_name=?, last_name=?, contact_num=? \n" +
                "WHERE fam_id=? AND emp_id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, profile.getFirstName());
            ps.setString(2, profile.getFamilyLastName());
            ps.setString(3, profile.getContactNum());
            ps.setObject(4, UUID.fromString(profile.getFamId()));
            ps.setObject(5, UUID.fromString(profile.getEmpId()));

            int rows = ps.executeUpdate();

            if (rows == 0) {
                throw new Exception("Update failed: invalid family ID or not your record.");
            }
        }
    }
}