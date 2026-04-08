package hrm.server.impl;

import hrm.common.interfaces.LeaveService;
import hrm.common.model.LeaveApplication;
import hrm.server.dao.LeaveDAO;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

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
            // if (startDate.isBefore(LocalDate.now())) {
            //     throw new RemoteException("Start date cannot be in the past.");
            // }
            // if (endDate.isBefore(startDate)) {
            //     throw new RemoteException("End date cannot be before start date.");
            // }
            // System.out.println("[DEBUG] Dates OK");
            //
            // // Validation 2
            // System.out.println("[DEBUG] Checking leave type...");
            // if (!isValidLeaveType(leaveType)) {
            //     throw new RemoteException("Invalid leave type.");
            // }
            // System.out.println("[DEBUG] Leave type OK");
            //
            // // Validation 3
            // System.out.println("[DEBUG] Checking overlapping applications...");
            // if (leaveDAO.hasOverlappingApplication(employeeId, startDate, endDate)) {
            //     throw new RemoteException("Overlapping application exists.");
            // }
            // System.out.println("[DEBUG] No overlap");
            //
            // // Validation 4
            // System.out.println("[DEBUG] Getting leave balance...");
            // LeaveBalance balance = leaveDAO.getLeaveBalance(employeeId);
            // System.out.println("[DEBUG] Balance: " + balance);
            //
            // if (balance == null) {
            //     throw new RemoteException("Employee not found: " + employeeId);
            // }
            //
            // long totalDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;
            // System.out.println("[DEBUG] Total days: " + totalDays);
            //
            // if (balance.getBalanceFor(leaveType) < totalDays) {
            //     throw new RemoteException("Insufficient balance.");
            // }

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

    // @Override
    // public LeaveApplication applyLeave(String employeeId, String leaveType,
    //                                    LocalDate startDate, LocalDate endDate,
    //                                    String reason) throws RemoteException {
    //     try {
    //         // --- Validation 1: dates must make sense ---
    //         if (startDate == null || endDate == null) {
    //             throw new RemoteException("Start date and end date are required.");
    //         }
    //         // if (startDate.isBefore(LocalDate.now())) {
    //         //     throw new RemoteException("Start date cannot be in the past.");
    //         // }
    //         // if (endDate.isBefore(startDate)) {
    //         //     throw new RemoteException("End date cannot be before start date.");
    //         // }
    //         //
    //         // // --- Validation 2: leave type must be valid ---
    //         // if (!isValidLeaveType(leaveType)) {
    //         //     throw new RemoteException("Invalid leave type. Choose ANNUAL, SICK, or EMERGENCY.");
    //         // }
    //         //
    //         // // --- Validation 3: no overlapping applications ---
    //         // if (leaveDAO.hasOverlappingApplication(employeeId, startDate, endDate)) {
    //         //     throw new RemoteException(
    //         //             "You already have a PENDING or APPROVED application covering those dates.");
    //         // }
    //         //
    //         // // --- Validation 4: sufficient balance ---
    //         // int totalDays = (int) ChronoUnit.DAYS.between(startDate, endDate) + 1;
    //         // LeaveBalance balance = leaveDAO.getLeaveBalance(employeeId);
    //         //
    //         // if (balance == null) {
    //         //     throw new RemoteException("Employee not found: " + employeeId);
    //         // }
    //         // if (balance.getBalanceFor(leaveType) < totalDays) {
    //         //     throw new RemoteException(
    //         //             "Insufficient " + leaveType + " leave balance. " +
    //         //                     "Available: " + balance.getBalanceFor(leaveType) +
    //         //                     " days, Requested: " + totalDays + " days.");
    //         // }
    //
    //         // --- All checks passed — persist the application ---
    //         LeaveApplication app = new LeaveApplication(
    //                 employeeId, leaveType.toUpperCase(), startDate, endDate, reason);
    //         //app.setTotalDays(totalDays);
    //
    //         LeaveApplication saved = leaveDAO.insertApplication(app);
    //
    //         // Deduct balance immediately on application (HR can reinstate on rejection)
    //         //leaveDAO.deductLeaveBalance(employeeId, leaveType, totalDays);
    //
    //         System.out.println("[LeaveService] Leave applied: " + saved);
    //         return saved;
    //
    //     } catch (RemoteException re) {
    //         throw re; // re-throw validation errors as-is
    //     } catch (Exception e) {
    //         System.err.println("[LeaveService] Unexpected error: " + e.getMessage());
    //         throw new RemoteException("Server error while applying leave: " + e.getMessage(), e);
    //     }
    // }
}
