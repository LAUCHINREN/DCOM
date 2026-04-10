package hrm.common.interfaces;

import hrm.common.model.EmployeeProfile;
import hrm.common.model.User;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface UserService extends Remote {

    void registerUser(EmployeeProfile emp, User user) throws RemoteException;
}