package hrm.server.impl;

import hrm.common.interfaces.LeaveService;
import hrm.common.model.LeaveApplication;
import hrm.server.dao.LeaveDAO;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class LeaveServiceImpl extends UnicastRemoteObject implements LeaveService {

    private static final long serialVersionUID = 1L;
    private final LeaveDAO leaveDAO;

    public LeaveServiceImpl() throws RemoteException {
        super();
        this.leaveDAO = new LeaveDAO();
    }

    @Override
    public LeaveApplication applyLeave(String employeeId, String leaveType,
                                       LocalDate startDate, LocalDate endDate,
                                       String reason) throws RemoteException {
        try {
            System.out.println("[DEBUG] applyLeave() entered");

            // Validation 1
            System.out.println("[DEBUG] Checking dates...");
            if (startDate == null || endDate == null) {
                throw new RemoteException("Start date and end date are required.");
            }

            // Insert
            System.out.println("[DEBUG] About to call insertApplication...");
            LeaveApplication app = new LeaveApplication(
                    employeeId, leaveType.toUpperCase(), startDate, endDate, reason);
            //app.setTotalDays(totalDays);

            LeaveApplication saved = leaveDAO.insertApplication(app);
            System.out.println("[DEBUG] insertApplication returned: " + saved);

            //leaveDAO.deductLeaveBalance(employeeId, leaveType, totalDays);
            System.out.println("[DEBUG] Balance deducted");

            return saved;

        } catch (RemoteException re) {
            throw re;
        } catch (Exception e) {
            e.printStackTrace();  // full stack trace
            throw new RemoteException("Server error while applying leave: " + e.getMessage(), e);
        }
    }

    @Override
    public List<LeaveApplication> getMyApplications(String employeeId) throws RemoteException {
        try {
            return leaveDAO.getApplicationsByEmployee(employeeId);
        } catch (Exception e) {
            throw new RemoteException("Failed to fetch leave applications: " + e.getMessage(), e);
        }
    }
}
