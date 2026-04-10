package hrm.common.interfaces;

import hrm.common.model.LeaveApplication;
import hrm.common.model.EmployeeProfile;
import hrm.common.model.LeaveBalance;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.time.LocalDate;
import java.util.List;
public interface LeaveService extends Remote{

    LeaveApplication applyLeave(String employeeId, String leaveType,
                                LocalDate startDate, LocalDate endDate,
                                String reason) throws RemoteException;

    List<LeaveApplication> getMyApplications(String employeeId) throws RemoteException;

    void updateLeaveStatus(String applicationId, String status, String approvedBy)
            throws RemoteException;

    List<LeaveApplication> getPendingApplications() throws RemoteException;

    List<LeaveBalance> getLeaveBalance(String empId) throws RemoteException;
    
}
