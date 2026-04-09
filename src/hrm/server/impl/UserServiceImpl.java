package hrm.server.impl;

import hrm.common.interfaces.UserService;
import hrm.common.model.EmployeeProfile;
import hrm.common.model.User;
import hrm.server.dao.UserDAO;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class UserServiceImpl extends UnicastRemoteObject implements UserService {

    private final UserDAO dao;

    public UserServiceImpl() throws RemoteException {
        this.dao = new UserDAO();
    }

    @Override
    public void registerUser(EmployeeProfile emp, User user) throws RemoteException {
        try {
            dao.registerUser(emp, user);
            System.out.println("[DEBUG] Registered: " + user.getUsername());
        } catch (Exception e) {
            throw new RemoteException("Register failed: " + e.getMessage(), e);
        }
    }
}