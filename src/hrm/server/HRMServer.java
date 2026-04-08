package hrm.server;

import hrm.server.dao.DBConnection;
import hrm.server.impl.LeaveServiceImpl;
import hrm.server.impl.EmployeeServiceImpl;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class HRMServer {

    private static final int RMI_PORT = 1044;

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("  HRM Server — Starting up...");
        System.out.println("=================================================");

        // Step 1: Verify DB connection FIRST before doing anything else
        // If this fails, the server will not start — fail fast
        System.out.println("[Server] Connecting to database...");
        try {
            DBConnection.getConnection();
            System.out.println("[Server] Database connection verified.");
        } catch (Exception e) {
            System.err.println("[Server] FATAL: Cannot connect to database.");
            System.err.println("[Server] Reason: " + e.getMessage());
            System.err.println("[Server] Check DBConnection.java — host, port, username, password.");
            System.exit(1); // stop here, do not open RMI port
        }

        // Step 2: Only reached if DB is healthy — now bind RMI services
        try {
            Registry registry = LocateRegistry.createRegistry(RMI_PORT);
            System.out.println("[Server] RMI Registry started on port " + RMI_PORT);

            LeaveServiceImpl leaveService = new LeaveServiceImpl();
            registry.rebind("LeaveService", leaveService);
            System.out.println("[Server] LeaveService bound.");

            EmployeeServiceImpl employeeService = new EmployeeServiceImpl();
            registry.rebind("EmployeeService", employeeService);
            System.out.println("[Server] EmployeeService bound.");

            LoginService loginService = new LoginServiceImpl();
            registry.rebind("LoginService", loginService);
            System.out.println("[Server] LoginService ready.");

            // Register more services here as you build them:
            // HRMServiceImpl hrmService = new HRMServiceImpl();
            // registry.rebind("HRMService", hrmService);

            System.out.println("=================================================");
            System.out.println("  HRM Server is ready. Waiting for clients...");
            System.out.println("=================================================");

        } catch (Exception e) {
            System.err.println("[Server] FATAL: Could not start RMI registry.");
            System.err.println("[Server] Reason: " + e.getMessage());
            System.err.println("[Server] Is port " + RMI_PORT + " already in use?");
            e.printStackTrace();
            System.exit(1);
        }
    }
}
