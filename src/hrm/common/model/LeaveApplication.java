package hrm.common.model;


import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class LeaveApplication implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String employeeId;   // ic_number used as employee identifier
    private String leaveType;    // "ANNUAL", "SICK", "EMERGENCY"
    private LocalDate startDate;
    private LocalDate endDate;
    private long totalDays;
    private String reason;
    private String status;       // "PENDING", "APPROVED", "REJECTED"
    private LocalDateTime appliedAt;

    public LeaveApplication() {}

    public LeaveApplication(String employeeId, String leaveType,
                            LocalDate startDate, LocalDate endDate, String reason) {
        this.employeeId = employeeId;
        this.leaveType  = leaveType;
        this.startDate  = startDate;
        this.endDate    = endDate;
        this.reason     = reason;
        this.status     = "PENDING";
    }

    // Getters and setters
    public String getId()                     { return id; }
    public void setId(String id)              { this.id = id; }

    public String getEmployeeId()                   { return employeeId; }
    public void setEmployeeId(String employeeId)    { this.employeeId = employeeId; }

    public String getLeaveType()                    { return leaveType; }
    public void setLeaveType(String leaveType)      { this.leaveType = leaveType; }

    public LocalDate getStartDate()                 { return startDate; }
    public void setStartDate(LocalDate startDate)   { this.startDate = startDate; }

    public LocalDate getEndDate()                   { return endDate; }
    public void setEndDate(LocalDate endDate)       { this.endDate = endDate; }

    public long getTotalDays()                       { return totalDays; }
    public void setTotalDays(long totalDays)         { this.totalDays = totalDays; }

    public String getReason()                       { return reason; }
    public void setReason(String reason)            { this.reason = reason; }

    public String getStatus()                       { return status; }
    public void setStatus(String status)            { this.status = status; }

    public LocalDateTime getAppliedAt()                 { return appliedAt; }
    public void setAppliedAt(LocalDateTime appliedAt)   { this.appliedAt = appliedAt; }

    @Override
    public String toString() {
        return String.format("[%s] %s | %s to %s (%d days) | %s | Status: %s",
                id, leaveType, startDate, endDate, totalDays, reason, status);
    }
}
