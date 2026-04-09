package hrm.client;

import hrm.common.interfaces.LeaveService;
import hrm.common.model.LeaveApplication;
import hrm.common.interfaces.EmployeeService;
import hrm.common.interfaces.LoginService;
import hrm.common.model.User;
import hrm.common.interfaces.UserService;
import hrm.common.model.EmployeeProfile;

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
        LoginService loginService;
        UserService userService;

        try {
            Registry registry = LocateRegistry.getRegistry(SERVER_HOST, RMI_PORT);
            leaveService = (LeaveService) registry.lookup("LeaveService");
            employeeService = (EmployeeService) registry.lookup("EmployeeService");
            loginService = (LoginService) registry.lookup("LoginService");
            userService = (UserService) registry.lookup("UserService");
        } catch (Exception e) {
            System.err.println("[Client] Cannot connect to server: " + e.getMessage());
            System.err.println("Make sure HRMServer is running on " + SERVER_HOST + ":" + RMI_PORT);
            return;
        }
        //endregion

        while (true) {

            // ================= LOGIN =================
            User user = null;
            int attempts = 0;

            while (user == null && attempts < 3) {
                System.out.println("===== LOGIN =====");

                System.out.print("Username: ");
                String username = sc.nextLine();

                System.out.print("Password: ");
                String password = sc.nextLine();

                try {
                    user = loginService.login(username, password);
                } catch (Exception e) {
                    System.err.println("Login service error: " + e.getMessage());
                    return;
                }

                if (user == null) {
                    attempts++;
                    System.out.println("Invalid username or password! Attempts left: " + (3 - attempts) + "\n");
                }
            }

            if (user == null) {
                System.out.println("Too many failed attempts. Exiting...");
                return;
            }

            System.out.println("Login successful!");
            System.out.println("Role: " + user.getRole());

            // ================= MENU =================
            while (true) {
                System.out.println("\n===============================");
                System.out.println("        HRM SYSTEM");
                System.out.println("===============================");

                if ("EMPLOYEE".equalsIgnoreCase(user.getRole())) {
                    System.out.println("1. Apply Leave");
                    System.out.println("2. Update Profile");
                } else if ("HR".equalsIgnoreCase(user.getRole())) {
                    System.out.println("1. Approve Leave (Coming Soon)");
                    System.out.println("2. Manage Employees (Coming Soon)");
                    System.out.println("3. Register Employee");
                } else if ("SUPER ADMIN".equalsIgnoreCase(user.getRole())) {
                    System.out.println("1. Register Employee");
                    System.out.println("2. Register HR");
                }

                if ("HR".equalsIgnoreCase(user.getRole())) {
                    System.out.println("4. Logout");
                } else {
                    System.out.println("3. Logout");
                }
                System.out.print("Choose option: ");

                String choice = sc.nextLine();

                if ("EMPLOYEE".equalsIgnoreCase(user.getRole())) {

                    switch (choice) {
                        case "1":
                            applyLeave(leaveService, sc, user);
                            break;

                        case "2":
                            updateProfile(employeeService, sc, user);
                            break;

                        case "3":
                            System.out.println("Logging out...\n");
                            break;

                        default:
                            System.out.println("Invalid choice!");
                    }

                } else if ("HR".equalsIgnoreCase(user.getRole())) {

                    switch (choice) {
                        case "1":
                            System.out.println("HR Approve Leave (TODO)");
                            break;

                        case "2":
                            System.out.println("Manage Employees (TODO)");
                            break;

                        case "3":
                            registerUser(userService, sc, "EMPLOYEE");

                        case "4":
                            System.out.println("Logging out...\n");
                            break;

                        default:
                            System.out.println("Invalid choice!");
                    }
                } else if ("SUPER ADMIN".equalsIgnoreCase(user.getRole())) {

                    switch (choice) {
                        case "1":
                            registerUser(userService, sc, "EMPLOYEE");
                            break;

                        case "2":
                            registerUser(userService, sc, "HR");
                            break;

                        case "3":
                            System.out.println("Logging out...\n");
                            break;

                        default:
                            System.out.println("Invalid choice!");
                    }
                }

                if (
                        ("EMPLOYEE".equalsIgnoreCase(user.getRole()) && "3".equals(choice)) ||
                                ("HR".equalsIgnoreCase(user.getRole()) && "4".equals(choice)) ||
                                ("SUPER ADMIN".equalsIgnoreCase(user.getRole()) && "3".equals(choice))
                ) {
                    break;
                }
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

    private static void applyLeave(LeaveService leaveService, Scanner sc, User user) {
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
            String empId = user.getEmpId();
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

    private static void updateProfile(EmployeeService service, Scanner sc, User user) {

        System.out.println("\n--- Update Profile ---");

        String empId = user.getEmpId();
        System.out.println("Employee ID: " + empId);

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
            EmployeeProfile profile = new EmployeeProfile();

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

    private static void registerUser(UserService service, Scanner sc, String role) {

        System.out.println("\n--- Register " + role + " ---");

        while (true) {

            System.out.print("First Name: ");
            String first = sc.nextLine();

            System.out.print("Last Name: ");
            String last = sc.nextLine();

            System.out.print("Contact Number: ");
            String contact = sc.nextLine();

            System.out.print("Email: ");
            String email = sc.nextLine();

            System.out.print("Identification Number: ");
            String ic = sc.nextLine();

            System.out.print("Username: ");
            String username = sc.nextLine();

            System.out.print("Password: ");
            String password = sc.nextLine();

            LocalDate hireDateObj;
            while (true) {
                System.out.print("Hire Date (yyyy-MM-dd): ");
                String hireDate = sc.nextLine();

                try {
                    hireDateObj = LocalDate.parse(hireDate);
                    break;
                } catch (Exception e) {
                    System.out.println("[Warning] Invalid date format! Try again.");
                }
            }

            try {
                EmployeeProfile emp = new EmployeeProfile();
                emp.setFirstName(first);
                emp.setLastName(last);
                emp.setContactNum(contact);
                emp.setEmail(email);
                emp.setHireDate(hireDateObj);
                emp.setIdentificationNum(ic);

                User user = new User();
                user.setUsername(username);
                user.setPassword(password);
                user.setRole(role);

                service.registerUser(emp, user);

                System.out.println("\n[Success] " + role + " registered!\n");
                return;

            } catch (Exception e) {

                String msg = e.getMessage();

                if (msg != null && msg.toLowerCase().contains("username already exists")) {
                    System.out.println("[Warning] Username already exists. Please try another.\n");
                    continue;
                }

                System.out.println("[Error] Unexpected error: " + msg);
                return;
            }
        }
    }
}
