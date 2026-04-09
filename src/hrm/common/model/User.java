package hrm.common.model;

import java.io.Serializable;

public class User implements Serializable {

    private String userId;
    private String empId;
    private String username;
    private String password;
    private String role;
    private String status;

    public User() {}

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getEmpId() { return empId; }
    public void setEmpId(String empId) { this.empId = empId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}