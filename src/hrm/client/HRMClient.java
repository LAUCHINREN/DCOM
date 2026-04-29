package hrm.client;

import hrm.common.interfaces.LeaveService;
import hrm.common.model.LeaveApplication;
import hrm.common.interfaces.EmployeeService;
import hrm.common.interfaces.LoginService;
import hrm.common.model.User;
import hrm.common.interfaces.UserService;
import hrm.common.model.EmployeeProfile;
import hrm.common.model.LeaveBalance;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class HRMClient {
    private static final String SERVER_HOST = "localhost";//"localhost";  // change to server IP e.g. "192.168.1.102"
    private static final int    RMI_PORT    = 1044;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static void main(String[] args) {

        // To show SSL/TLS is actually work
        //System.setProperty("javax.net.debug", "ssl,handshake");
        //System.out.println("[SECURITY] SSL/TLS enabled for client.");

        Path trustStorePath = Paths.get("clienttruststore.jks").toAbsolutePath();

        if (!Files.exists(trustStorePath)) {
            System.err.println("[Client] FATAL: clienttruststore.jks not found at:");
            System.err.println(trustStorePath);
            return;
        }

        System.setProperty("javax.net.ssl.trustStore", trustStorePath.toString());
        System.setProperty("javax.net.ssl.trustStorePassword", "password");

        System.out.println("[Client] Using truststore: " + trustStorePath);
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
            Registry registry = LocateRegistry.getRegistry(
                    SERVER_HOST,
                    RMI_PORT,
                    new javax.rmi.ssl.SslRMIClientSocketFactory()
            );
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

        // System CLI Interface
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
                    System.out.println("3. View My Leave Applications");
                    System.out.println("4. View Leave Balance");
                    System.out.println("5. Manage Family");
                } else if ("HR".equalsIgnoreCase(user.getRole())) {
                    System.out.println("1. Approve Leave");
                    System.out.println("2. Export Yearly Leave Report (CSV)");
                    System.out.println("3. Register Employee");
                } else if ("SUPER ADMIN".equalsIgnoreCase(user.getRole())) {
                    System.out.println("1. Register Employee");
                    System.out.println("2. Register HR");
                }

                if ("EMPLOYEE".equalsIgnoreCase(user.getRole())) {
                    System.out.println("6. Logout");
                } else if ("HR".equalsIgnoreCase(user.getRole())) {
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
                            viewMyLeaves(leaveService, user);
                            break;

                        case "4":
                            viewLeaveBalance(leaveService, user);
                            break;

                        case "5":
                            manageFamily(employeeService, sc, user);
                            break;

                        case "6":
                            System.out.println("Logging out...\n");
                            break;

                        default:
                            System.out.println("Invalid choice!");
                    }

                } else if ("HR".equalsIgnoreCase(user.getRole())) {

                    switch (choice) {
                        case "1":
                            approveLeave(leaveService, sc, user);
                            break;

                        case "2":
                            try {
                                System.out.print("Enter Employee ID: ");
                                String empId = sc.nextLine();

                                System.out.print("Enter Year: ");
                                int year = Integer.parseInt(sc.nextLine());

                                System.out.println("Exporting report in background...\n");

                                new Thread(() -> {
                                    try {
                                        List<LeaveBalance> list =
                                                leaveService.getLeaveBalanceByYear(empId, year);

                                        if (list.isEmpty()) {
                                            System.out.println("[Thread] No data found.\n");
                                            return;
                                        }

                                        String fileName = "leave_balance_" + empId + "_" + year + ".csv";

                                        java.io.FileWriter writer = new java.io.FileWriter(fileName);

                                        writer.append("Leave ID,Employee ID,Year,Leave Type,Total Quota,Applied,Balance,Carry Forward\n");

                                        for (LeaveBalance lb : list) {
                                            writer.append(lb.getLeaveId()).append(",");
                                            writer.append(lb.getEmpId()).append(",");
                                            writer.append(String.valueOf(lb.getYear())).append(",");
                                            writer.append(lb.getLeaveType()).append(",");
                                            writer.append(lb.getTotalQuota().toString()).append(",");
                                            writer.append(lb.getApplied().toString()).append(",");
                                            writer.append(lb.getBalance().toString()).append(",");
                                            writer.append(lb.getCarryForward().toString()).append("\n");
                                        }

                                        writer.flush();
                                        writer.close();

                                        System.out.println("[Thread] CSV Export Completed: " + fileName + "\n");

                                    } catch (Exception e) {
                                        System.out.println("[Thread Error] " + e.getMessage());
                                    }
                                }).start();

                            } catch (Exception e) {
                                System.out.println("[Error] " + e.getMessage());
                            }
                            break;

                        case "3":
                            registerUser(userService, sc, "EMPLOYEE");
                            break;

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
                        ("EMPLOYEE".equalsIgnoreCase(user.getRole()) && "6".equals(choice)) ||
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

        System.out.print("Gender (M/F): ");
        String gender = sc.nextLine();

        LocalDate dob = null;
        while (dob == null) {
            System.out.print("Date of Birth (yyyy-MM-dd): ");
            String dobInput = sc.nextLine();
            try {
                dob = LocalDate.parse(dobInput);
            } catch (Exception e) {
                System.out.println("[Warning] Invalid date format!");
            }
        }

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
            profile.setGender(gender);
            profile.setDob(dob);

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

    private static void viewMyLeaves(LeaveService service, User user) {

        System.out.println("\n--- My Leave Applications ---");

        try {
            java.util.List<LeaveApplication> list =
                    service.getMyApplications(user.getEmpId());

            if (list.isEmpty()) {
                System.out.println("No leave applications found.\n");
                return;
            }

            for (LeaveApplication app : list) {
                System.out.println(app); // uses toString()
            }

            System.out.println();

        } catch (Exception e) {
            System.err.println("[Error] " + e.getMessage());
        }
    }

    private static void approveLeave(LeaveService service, Scanner sc, User user) {

        System.out.println("\n--- Pending Leave Applications ---");

        try {
            List<LeaveApplication> list = service.getPendingApplications();

            if (list.isEmpty()) {
                System.out.println("No pending applications.\n");
                return;
            }

            // Display list
            for (int i = 0; i < list.size(); i++) {
                System.out.println((i + 1) + ". " + list.get(i));
            }

            System.out.print("\nSelect application number (0 to cancel): ");
            int choice = Integer.parseInt(sc.nextLine());

            if (choice == 0) return;

            if (choice < 1 || choice > list.size()) {
                System.out.println("Invalid choice!");
                return;
            }

            LeaveApplication selected = list.get(choice - 1);

            System.out.println("\nSelected:");
            System.out.println(selected);

            System.out.print("Approve or Reject? (A/R): ");
            String decision = sc.nextLine().trim().toUpperCase();

            String status;
            if ("A".equals(decision)) {
                status = "APPROVED";
            } else if ("R".equals(decision)) {
                status = "REJECTED";
            } else {
                System.out.println("Invalid input.");
                return;
            }

            service.updateLeaveStatus(
                    selected.getId(),
                    status,
                    user.getUserId()
            );

            System.out.println("\n[Success] Leave " + status + "!\n");

        } catch (Exception e) {
            System.err.println("[Error] " + e.getMessage());
        }
    }

    private static void manageFamily(EmployeeService service, Scanner sc, User user) {

        while (true) {
            System.out.println("\n--- Family Management ---");
            System.out.println("1. View Family");
            System.out.println("2. Add Family Member");
            System.out.println("3. Update Family Member");
            System.out.println("4. Back");
            System.out.print("Choose: ");

            String choice = sc.nextLine();

            switch (choice) {
                case "1":
                    viewFamily(service, user);
                    break;
                case "2":
                    addFamily(service, sc, user);
                    break;
                case "3":
                    updateFamily(service, sc, user);
                    break;
                case "4":
                    return;
                default:
                    System.out.println("Invalid choice!");
            }
        }
    }

    private static void viewFamily(EmployeeService service, User user) {
        try {
            List<EmployeeProfile> list = service.getFamilyMembers(user.getEmpId());

            if (list.isEmpty()) {
                System.out.println("No family records.");
                return;
            }

            for (EmployeeProfile f : list) {
                System.out.println("----------------------------------");
                System.out.println("Family ID                   : " + f.getFamId());
                System.out.println("Name                        : " + f.getFirstName() + " " + f.getFamilyLastName());
                System.out.println("IC                          : " + f.getIdentificationNum());
                System.out.println("Contact                     : " + f.getContactNum());
                System.out.println("Emergency Contact Status    : " + (f.getEmergencyContactStatus() ? "Yes": "No"));
                System.out.println("----------------------------------");
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private static void addFamily(EmployeeService service, Scanner sc, User user) {

        System.out.print("First Name: ");
        String first = sc.nextLine();

        System.out.print("Last Name: ");
        String last = sc.nextLine();

        System.out.print("Contact: ");
        String contact = sc.nextLine();

        System.out.print("IC Number: ");
        String ic = sc.nextLine();

        if (first.isEmpty() || last.isEmpty() || contact.isEmpty()) {
            System.out.println("Fields cannot be empty!");
            return;
        }

        EmployeeProfile p = new EmployeeProfile();
        p.setEmpId(user.getEmpId());
        p.setFirstName(first);
        p.setFamilyLastName(last);
        p.setContactNum(contact);
        p.setIdentificationNum(ic);

        try {
            service.addFamilyMember(p);
            System.out.println("Added successfully!");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private static void updateFamily(EmployeeService service, Scanner sc, User user) {

        viewFamily(service, user);

        System.out.print("Enter Family ID to update: ");
        String famId = sc.nextLine();

        System.out.print("New First Name: ");
        String first = sc.nextLine();

        System.out.print("New Last Name: ");
        String last = sc.nextLine();

        System.out.print("New Contact: ");
        String contact = sc.nextLine();

        if (famId.isEmpty() || first.isEmpty() || last.isEmpty() || contact.isEmpty()) {
            System.out.println("Fields cannot be empty!");
            return;
        }

        EmployeeProfile p = new EmployeeProfile();
        p.setFamId(famId);
        p.setEmpId(user.getEmpId());
        p.setFirstName(first);
        p.setFamilyLastName(last);
        p.setContactNum(contact);

        try {
            service.updateFamilyMember(p);
            System.out.println("Updated successfully!");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private static void viewLeaveBalance(LeaveService service, User user) {

        System.out.println("\n--- My Leave Balance ---");

        try {
            List<LeaveBalance> list = service.getLeaveBalance(user.getEmpId());

            if (list.isEmpty()) {
                System.out.println("No leave balance found.\n");
                return;
            }

            for (LeaveBalance lb : list) {
                System.out.println("----------------------------------");
                System.out.println("Type    : " + lb.getLeaveType());
                System.out.println("Quota   : " + lb.getTotalQuota());
                System.out.println("Applied : " + lb.getApplied());
                System.out.println("Balance : " + lb.getBalance());
                System.out.println("----------------------------------");
            }

        } catch (Exception e) {
            System.err.println("[Error] " + e.getMessage());
        }
    }
}
