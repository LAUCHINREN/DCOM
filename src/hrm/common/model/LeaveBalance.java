package hrm.common.model;

import java.io.Serializable;
import java.math.BigDecimal;

public class LeaveBalance implements Serializable {

    private String leaveId;
    private String empId;
    private String leaveType;
    private int year;
    private BigDecimal totalQuota;
    private BigDecimal applied;
    private BigDecimal balance;
    private BigDecimal carryForward;

    public String getLeaveId() { return leaveId; }
    public void setLeaveId(String leaveId) { this.leaveId = leaveId; }

    public String getEmpId() { return empId; }
    public void setEmpId(String empId) { this.empId = empId; }

    public String getLeaveType() { return leaveType; }
    public void setLeaveType(String leaveType) { this.leaveType = leaveType; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public BigDecimal getTotalQuota() { return totalQuota; }
    public void setTotalQuota(BigDecimal totalQuota) { this.totalQuota = totalQuota; }

    public BigDecimal getApplied() { return applied; }
    public void setApplied(BigDecimal applied) { this.applied = applied; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public BigDecimal getCarryForward() { return carryForward; }
    public void setCarryForward(BigDecimal carryForward) { this.carryForward = carryForward; }
}