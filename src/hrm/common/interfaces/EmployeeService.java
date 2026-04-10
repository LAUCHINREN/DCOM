package hrm.common.interfaces;

import hrm.common.model.EmployeeProfile;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface EmployeeService extends Remote {

    EmployeeProfile updateEmployeeProfile(EmployeeProfile profile)
            throws RemoteException;

    List<EmployeeProfile> getFamilyMembers(String empId) throws RemoteException;

    void addFamilyMember(EmployeeProfile profile) throws RemoteException;

    void updateFamilyMember(EmployeeProfile profile) throws RemoteException;
}