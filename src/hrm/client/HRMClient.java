package hrm.client;

import hrm.common.interfaces.LeaveService;
import hrm.common.model.LeaveApplication;
import hrm.common.interfaces.EmployeeService;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public class HRMClient {
    private static final String SERVER_HOST = "localhost";//"localhost";  // change to server IP e.g. "192.168.1.102"
    private static final int    RMI_PORT    = 1044;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static void main(String[] args) {
        //region [console]
        Scanner sc = new Scanner(System.in);
        //endregion

        //region [connect to SERVER]
        System.out.println("=================================================");
        System.out.println("  HRM System — Console Test Client");
        System.out.println("  Connecting to server: " + SERVER_HOST + ":" + RMI_PORT);
        System.out.println("=================================================\n");

        LeaveService leaveService;
        EmployeeService employeeService;

        try {
            Registry registry = LocateRegistry.getRegistry(SERVER_HOST, RMI_PORT);
            leaveService = (LeaveService) registry.lookup("LeaveService");
            employeeService = (EmployeeService) registry.lookup("EmployeeService");
        } catch (Exception e) {
            System.err.println("[Client] Cannot connect to server: " + e.getMessage());
            System.err.println("Make sure HRMServer is running on " + SERVER_HOST + ":" + RMI_PORT);
            return;
        }
        //endregion

        while (true) {
            System.out.println("\n===============================");
            System.out.println("        HRM SYSTEM");
            System.out.println("===============================");
            System.out.println("1. Apply Leave");
            System.out.println("2. Update Profile");
            System.out.println("3. Exit");
            System.out.print("Choose option: ");

            String choice = sc.nextLine();

            switch (choice) {
                case "1":
                    applyLeave(leaveService, sc);
                    break;

                case "2":
                    updateProfile(employeeService, sc);
                    break;

                case "3":
                    System.out.println("Exiting...");
                    sc.close();
                    return;

                default:
                    System.out.println("Invalid choice!");
            }
        }
    }

    private static LocalDate promptDate(Scanner sc, String prompt) {
        System.out.print(prompt);
        String input = sc.nextLine().trim();
        try {
            return LocalDate.parse(input, DATE_FMT);
        } catch (DateTimeParseException e) {
            System.err.println("[Error] Invalid date format. Use yyyy-MM-dd.\n");
            return null;
        }
    }

    // ------------------------------------------------------------------
    // Feature 2: Apply Leave
    // ------------------------------------------------------------------

    private static void applyLeave(LeaveService leaveService, Scanner sc) {
        System.out.println("\n--- Apply for Leave ---");

        // Leave type
        System.out.println("Leave types: ANNUAL, SICK, EMERGENCY");
        System.out.print("Enter leave type: ");
        String leaveType = sc.nextLine().trim().toUpperCase();

        // Start date
        LocalDate startDate = promptDate(sc, "Enter start date (yyyy-MM-dd): ");
        if (startDate == null) return;

        // End date
        LocalDate endDate = promptDate(sc, "Enter end date   (yyyy-MM-dd): ");
        if (endDate == null) return;

        // Reason
        System.out.print("Enter reason: ");
        String reason = sc.nextLine().trim();

        // Confirm
        long days = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
        System.out.println("\nSummary:");
        System.out.println("  Type   : " + leaveType);
        System.out.println("  From   : " + startDate);
        System.out.println("  To     : " + endDate);
        System.out.println("  Days   : " + days);
        System.out.println("  Reason : " + reason);
        System.out.print("Confirm? (y/n): ");
        String confirm = sc.nextLine().trim();

        if (!confirm.equalsIgnoreCase("y")) {
            System.out.println("Cancelled.\n");
            return;
        }

        // Call the RMI service
        try {
            System.out.print("Enter Employee ID: ");
            String empId = sc.nextLine();
            LeaveApplication result = leaveService.applyLeave(
                    empId, leaveType, startDate, endDate, reason);

            System.out.println("\n[Success] Leave application submitted!");
            System.out.println("  Application ID : " + result.getId());
            System.out.println("  Status         : " + result.getStatus());
            System.out.println("  Applied on     : " + result.getAppliedAt());
            System.out.println();

        } catch (Exception e) {
            // // Server sends back validation messages as RemoteException
            // System.err.println("\n[Failed] " + e.getMessage() + "\n");
            System.err.println("[FULL ERROR]");
            Throwable t = e;
            while (t != null) {
                System.err.println("  → " + t.getClass().getName() + ": " + t.getMessage());
                t = t.getCause();
            }
            e.printStackTrace();
        }
    }

    private static void updateProfile(EmployeeService service, Scanner sc) {

        System.out.println("\n--- Update Profile ---");

        System.out.print("Employee ID: ");
        String empId = sc.nextLine();

        System.out.print("First Name: ");
        String first = sc.nextLine();

        System.out.print("Last Name: ");
        String last = sc.nextLine();

        System.out.print("Contact Number: ");
        String contact = sc.nextLine();

        System.out.print("Email: ");
        String email = sc.nextLine();

        System.out.print("Emergency Contact Name: ");
        String ecName = sc.nextLine();

        System.out.print("Emergency Contact Phone: ");
        String ecPhone = sc.nextLine();

        try {
            hrm.common.model.EmployeeProfile profile =
                    new hrm.common.model.EmployeeProfile();

            profile.setEmpId(empId);
            profile.setFirstName(first);
            profile.setLastName(last);
            profile.setContactNum(contact);
            profile.setEmail(email);
            profile.setEmergencyName(ecName);
            profile.setEmergencyContact(ecPhone);

            service.updateEmployeeProfile(profile);

            System.out.println("\n[Success] Profile updated!\n");

        } catch (Exception e) {
            System.err.println("[Error] " + e.getMessage());
        }
    }
}
