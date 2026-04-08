package hrm.server.impl;

import hrm.common.interfaces.EmployeeService;
import hrm.common.model.EmployeeProfile;
import hrm.server.dao.EmployeeDAO;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

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
}