package hrm.common.interfaces;

import hrm.common.model.EmployeeProfile;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface EmployeeService extends Remote {

    EmployeeProfile updateEmployeeProfile(EmployeeProfile profile)
            throws RemoteException;
}