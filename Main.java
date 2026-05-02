// ============================================================
//  Main.java  –  Entry point with interactive console menu
// ============================================================
import java.util.*;

public class Main {

    private static final Scanner sc = new Scanner(System.in);
    private static AttendanceManager manager;

    public static void main(String[] args) {

        printBanner();
        manager = new AttendanceManager();

        boolean running = true;
        while (running) {
            printMenu();
            int choice = readInt("  Enter choice: ");

            switch (choice) {
                case 1  -> addStudent();
                case 2  -> removeStudent();
                case 3  -> markAttendance();
                case 4  -> manager.printReport();
                case 5  -> manager.printShortageList();
                case 6  -> manager.printSummary();
                case 7  -> viewStudent();
                case 8  -> manager.viewAttendeesLog();
                case 9  -> manager.viewAbsenteesLog();
                case 0  -> { System.out.println("\n  Goodbye!\n"); running = false; }
                default -> System.out.println("  Invalid option, try again.\n");
            }
        }
        sc.close();
    }

    // ── Menu ─────────────────────────────────────────────────
    private static void printBanner() {
        System.out.println();
        System.out.println("  ╔══════════════════════════════════════════════════╗");
        System.out.println("  ║      STUDENT ATTENDANCE TRACKER  v1.0           ║");
        System.out.println("  ║      College Department Management System        ║");
        System.out.println("  ╚══════════════════════════════════════════════════╝");
        System.out.println();
    }

    private static void printMenu() {
        System.out.println("  ┌────────────────────────────────────┐");
        System.out.println("  │              MAIN MENU             │");
        System.out.println("  ├────────────────────────────────────┤");
        System.out.println("  │  1. Add Student                    │");
        System.out.println("  │  2. Remove Student                 │");
        System.out.println("  │  3. Mark Attendance                │");
        System.out.println("  │  4. View Full Attendance Report    │");
        System.out.println("  │  5. View Shortage List (< 75%)     │");
        System.out.println("  │  6. Department Summary             │");
        System.out.println("  │  7. Search Student                 │");
        System.out.println("  │  8. View Attendees Log (file)      │");
        System.out.println("  │  9. View Absentees Log (file)      │");
        System.out.println("  │  0. Exit                           │");
        System.out.println("  └────────────────────────────────────┘");
    }

    // ── Action helpers ────────────────────────────────────────
    private static void addStudent() {
        System.out.println("\n  ── Add New Student ──");
        System.out.print("  Roll No : "); String roll   = sc.nextLine().trim().toUpperCase();
        System.out.print("  Name    : "); String name   = sc.nextLine().trim();
        System.out.print("  Branch  : "); String branch = sc.nextLine().trim().toUpperCase();

        if (roll.isEmpty() || name.isEmpty() || branch.isEmpty()) {
            System.out.println("  ✗  All fields are required.\n");
            return;
        }
        manager.addStudent(roll, name, branch);
        System.out.println();
    }

    private static void removeStudent() {
        System.out.println("\n  ── Remove Student ──");
        System.out.print("  Roll No : ");
        String roll = sc.nextLine().trim().toUpperCase();
        manager.removeStudent(roll);
        System.out.println();
    }

    private static void markAttendance() {
        System.out.println("\n  ── Mark Attendance ──");

        if (manager.getAllStudents().isEmpty()) {
            System.out.println("  ✗  No students registered. Add students first.\n");
            return;
        }

        System.out.print("  Subject : ");
        String subject = sc.nextLine().trim();
        if (subject.isEmpty()) subject = "General";

        System.out.println("\n  Registered students:");
        for (Student s : manager.getAllStudents()) {
            System.out.printf("    %s  –  %s%n", s.getRollNo(), s.getName());
        }

        System.out.println("\n  Enter PRESENT roll numbers separated by commas");
        System.out.println("  (leave blank if all absent):");
        System.out.print("  > ");
        String input = sc.nextLine().trim().toUpperCase();

        List<String> presentList = new ArrayList<>();
        if (!input.isEmpty()) {
            for (String token : input.split(",")) {
                String r = token.trim();
                if (!r.isEmpty()) {
                    if (manager.getStudent(r) == null) {
                        System.out.println("  ⚠  Roll No '" + r + "' not found – skipped.");
                    } else {
                        presentList.add(r);
                    }
                }
            }
        }

        manager.markAttendance(presentList, subject);
    }

    private static void viewStudent() {
        System.out.println("\n  ── Search Student ──");
        System.out.print("  Roll No : ");
        String roll = sc.nextLine().trim().toUpperCase();
        Student s = manager.getStudent(roll);

        if (s == null) {
            System.out.println("  ✗  Student not found.\n");
            return;
        }

        System.out.println();
        System.out.println("  ┌───────────────────────────────────────────┐");
        System.out.printf( "  │  Roll No    : %-29s│%n", s.getRollNo());
        System.out.printf( "  │  Name       : %-29s│%n", s.getName());
        System.out.printf( "  │  Branch     : %-29s│%n", s.getBranch());
        System.out.printf( "  │  Attended   : %-29d│%n", s.getClassesAttended());
        System.out.printf( "  │  Total      : %-29d│%n", s.getTotalClasses());
        System.out.printf( "  │  Percentage : %-28.2f%%│%n", s.getAttendancePercentage());
        System.out.printf( "  │  Status     : %-29s│%n",
                s.hasShortage() ? "⚠ SHORTAGE" : "✓ ELIGIBLE");
        System.out.println("  └───────────────────────────────────────────┘");
        System.out.println();
    }

    // ── Utility ───────────────────────────────────────────────
    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                int val = Integer.parseInt(sc.nextLine().trim());
                return val;
            } catch (NumberFormatException e) {
                System.out.println("  ✗  Please enter a valid number.");
            }
        }
    }
}
