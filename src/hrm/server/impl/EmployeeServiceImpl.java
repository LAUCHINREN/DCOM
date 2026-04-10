package hrm.server.impl;

import hrm.common.interfaces.EmployeeService;
import hrm.common.model.EmployeeProfile;
import hrm.server.dao.EmployeeDAO;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class EmployeeServiceImpl extends UnicastRemoteObject implements EmployeeService {

    private final EmployeeDAO employeeDAO;

    public EmployeeServiceImpl() throws RemoteException {
        super();
        this.employeeDAO = new EmployeeDAO();
    }

    @Override
    public EmployeeProfile updateEmployeeProfile(EmployeeProfile profile)
            throws RemoteException {
        try {
            System.out.println("[DEBUG] EmployeeService.updateEmployeeProfile()");
            return employeeDAO.updateEmployeeProfile(profile);
        } catch (Exception e) {
            throw new RemoteException("Failed to update profile: " + e.getMessage(), e);
        }
    }

    @Override
    public List<EmployeeProfile> getFamilyMembers(String empId) throws RemoteException {
        try {
            return employeeDAO.getFamilyMembers(empId);
        } catch (Exception e) {
            throw new RemoteException(e.getMessage());
        }
    }

    @Override
    public void addFamilyMember(EmployeeProfile profile) throws RemoteException {
        try {
            employeeDAO.addFamilyMember(profile);
        } catch (Exception e) {
            throw new RemoteException(e.getMessage());
        }
    }

    @Override
    public void updateFamilyMember(EmployeeProfile profile) throws RemoteException {
        try {
            employeeDAO.updateFamilyMember(profile);
        } catch (Exception e) {
            throw new RemoteException(e.getMessage());
        }
    }
}