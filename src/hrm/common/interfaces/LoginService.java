package hrm.common.interfaces;

import hrm.common.model.User;
import hrm.common.interfaces.LoginService;
import hrm.common.model.User;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface LoginService extends Remote {

    User login(String username, String password) throws RemoteException;
}