package hrm.server;

import hrm.server.impl.LeaveServiceImpl;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Entry point for the SERVER laptop.
 * Run this first (after PostgreSQL is up on the DB laptop).
 *
 * Compile:  javac -cp .:postgresql-42.x.x.jar -d out src/hrm/.../*.java
 * Run:      java  -cp out:postgresql-42.x.x.jar hrm.server.HRMServer
 */
public class HRMServer_old {

    private static final int RMI_PORT = 1044;

    public static void main(String[] args) {
        try {
            // Create the RMI registry on this machine
            Registry registry = LocateRegistry.createRegistry(RMI_PORT);
            System.out.println("[Server] RMI Registry started on port " + RMI_PORT);

            // Instantiate and bind each service
            LeaveServiceImpl leaveService = new LeaveServiceImpl();
            registry.rebind("LeaveService", leaveService);
            System.out.println("[Server] LeaveService bound.");

            // Add more services here as you build them:
            // HRMServiceImpl  hrmService  = new HRMServiceImpl();
            // registry.rebind("HRMService", hrmService);

            System.out.println("[Server] HRM Server is ready. Waiting for clients...");

        } catch (Exception e) {
            System.err.println("[Server] Fatal error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
