package hrm.server.impl;

import hrm.common.interfaces.LeaveService;
import hrm.common.model.LeaveApplication;
import hrm.server.dao.LeaveDAO;
import hrm.common.model.LeaveBalance;

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

            if (startDate == null || endDate == null) {
                throw new RemoteException("Start date and end date are required.");
            }

            if (endDate.isBefore(startDate)) {
                throw new RemoteException("End date cannot be before start date!");
            }

            List<LeaveBalance> balances = leaveDAO.getLeaveBalance(employeeId);

            long requestedDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;

            boolean valid = false;

            for (LeaveBalance lb : balances) {
                if (lb.getLeaveType().equalsIgnoreCase(leaveType)) {

                    if (lb.getBalance().intValue() < requestedDays) {
                        throw new RemoteException("Insufficient leave balance!");
                    }

                    valid = true;
                    break;
                }
            }

            if (!valid) {
                throw new RemoteException("Invalid leave type!");
            }

            LeaveApplication app = new LeaveApplication(
                    employeeId, leaveType.toUpperCase(), startDate, endDate, reason);

            LeaveApplication saved = leaveDAO.insertApplication(app);

            return saved;

        } catch (RemoteException re) {
            throw re;
        } catch (Exception e) {
            e.printStackTrace();
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

    @Override
    public List<LeaveApplication> getPendingApplications() throws RemoteException {
        try {
            return leaveDAO.getPendingApplications();
        } catch (Exception e) {
            throw new RemoteException("Failed to fetch pending applications", e);
        }
    }

    @Override
    public void updateLeaveStatus(String applicationId, String status, String approvedBy)
            throws RemoteException {
        try {

            LeaveApplication app = leaveDAO.getApplicationById(applicationId);

            leaveDAO.updateLeaveStatus(applicationId, status, approvedBy);

            if ("APPROVED".equalsIgnoreCase(status) &&
                    app != null &&
                    !"APPROVED".equalsIgnoreCase(app.getStatus())) {

                leaveDAO.deductLeaveBalance(
                        app.getEmployeeId(),
                        app.getLeaveType(),
                        app.getTotalDays()
                );
            }

        } catch (Exception e) {
            throw new RemoteException("Failed to update leave status", e);
        }
    }

    @Override
    public List<LeaveBalance> getLeaveBalance(String empId) throws RemoteException {
        try {
            return leaveDAO.getLeaveBalance(empId);
        } catch (Exception e) {
            throw new RemoteException("Failed to fetch leave balance", e);
        }
    }

    @Override
    public List<LeaveBalance> getLeaveBalanceByYear(String empId, int year) throws RemoteException {
        try {
            return leaveDAO.getLeaveBalanceByYear(empId, year);
        } catch (Exception e) {
            throw new RemoteException("Failed to fetch leave balance", e);
        }
    }
}
