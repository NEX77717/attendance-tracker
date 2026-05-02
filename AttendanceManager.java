// ============================================================
//  AttendanceManager.java  –  Core business logic
//  Handles: collections, file I/O, attendance operations
// ============================================================
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class AttendanceManager {

    // ── Constants ─────────────────────────────────────────────
    private static final String ATTENDEES_FILE  = "attendees.txt";
    private static final String ABSENTEES_FILE  = "absentees.txt";
    private static final String STUDENTS_FILE   = "students.dat";
    private static final double SHORTAGE_LIMIT  = 75.0;

    // ── Collections ───────────────────────────────────────────
    private Map<String, Student> studentMap;   // rollNo → Student
    private String today;

    // ── Constructor ───────────────────────────────────────────
    public AttendanceManager() {
        studentMap = new LinkedHashMap<>();
        today = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
        loadStudents();
    }

    // ═══════════════════════════════════════════════════════════
    //  STUDENT MANAGEMENT
    // ═══════════════════════════════════════════════════════════

    /** Add a new student to the department roster */
    public boolean addStudent(String rollNo, String name, String branch) {
        if (studentMap.containsKey(rollNo)) {
            System.out.println("  ✗  Roll number " + rollNo + " already exists.");
            return false;
        }
        Student s = new Student(rollNo, name, branch);
        studentMap.put(rollNo, s);
        saveStudents();
        System.out.println("  ✓  Student '" + name + "' added successfully.");
        return true;
    }

    /** Remove a student from the roster */
    public boolean removeStudent(String rollNo) {
        if (!studentMap.containsKey(rollNo)) {
            System.out.println("  ✗  Roll number " + rollNo + " not found.");
            return false;
        }
        String name = studentMap.get(rollNo).getName();
        studentMap.remove(rollNo);
        saveStudents();
        System.out.println("  ✓  Student '" + name + "' removed.");
        return true;
    }

    public Collection<Student> getAllStudents() {
        return studentMap.values();
    }

    public Student getStudent(String rollNo) {
        return studentMap.get(rollNo);
    }

    // ═══════════════════════════════════════════════════════════
    //  DAILY ATTENDANCE MARKING
    // ═══════════════════════════════════════════════════════════

    /**
     * Mark attendance for a single session on today's date.
     * @param presentRollNos  List of roll numbers that were present
     */
    public void markAttendance(List<String> presentRollNos, String subject) {
        if (studentMap.isEmpty()) {
            System.out.println("  ✗  No students registered.");
            return;
        }

        List<String> attendees  = new ArrayList<>();
        List<String> absentees  = new ArrayList<>();

        // Normalise the input list for quick lookup
        Set<String> presentSet = new HashSet<>(presentRollNos);

        for (Student s : studentMap.values()) {
            if (presentSet.contains(s.getRollNo())) {
                s.markPresent();
                attendees.add(s.getRollNo() + " - " + s.getName());
            } else {
                s.markAbsent();
                absentees.add(s.getRollNo() + " - " + s.getName());
            }
        }

        saveStudents();
        writeAttendanceFile(ATTENDEES_FILE, attendees, subject, "ATTENDEES");
        writeAttendanceFile(ABSENTEES_FILE, absentees, subject, "ABSENTEES");

        System.out.printf("%n  ✓  Attendance marked for %s on %s%n", subject, today);
        System.out.printf("     Present : %d  |  Absent : %d%n%n",
                attendees.size(), absentees.size());
    }

    // ═══════════════════════════════════════════════════════════
    //  REPORTS
    // ═══════════════════════════════════════════════════════════

    /** Print full attendance report for all students */
    public void printReport() {
        if (studentMap.isEmpty()) {
            System.out.println("  No students found.");
            return;
        }

        String line = "─".repeat(85);
        System.out.println("\n" + line);
        System.out.printf("%-10s | %-20s | %-8s | %11s | %7s  %s%n",
                "Roll No", "Name", "Branch", "Att/Total", "  %", "");
        System.out.println(line);

        for (Student s : studentMap.values()) {
            System.out.println(s);
        }
        System.out.println(line);
        System.out.printf("  Total students: %d%n%n", studentMap.size());
    }

    /** Print only students with attendance shortage */
    public void printShortageList() {
        List<Student> shortage = studentMap.values().stream()
                .filter(Student::hasShortage)
                .collect(Collectors.toList());

        System.out.println("\n  ╔══════════════════════════════════════╗");
        System.out.println("  ║   ATTENDANCE SHORTAGE (< 75%)        ║");
        System.out.println("  ╚══════════════════════════════════════╝");

        if (shortage.isEmpty()) {
            System.out.println("  ✓  No students have attendance shortage.");
            return;
        }

        String line = "─".repeat(75);
        System.out.println(line);
        System.out.printf("%-10s | %-20s | %-8s | %6s | %s%n",
                "Roll No", "Name", "Branch", "  %", "Classes Needed");
        System.out.println(line);

        for (Student s : shortage) {
            int need = classesNeededFor75(s);
            System.out.printf("%-10s | %-20s | %-8s | %5.2f%% | Need %d more classes%n",
                    s.getRollNo(), s.getName(), s.getBranch(),
                    s.getAttendancePercentage(), need);
        }
        System.out.println(line);
        System.out.printf("  Students in shortage: %d / %d%n%n",
                shortage.size(), studentMap.size());
    }

    /** Calculate extra consecutive classes needed to reach 75% */
    private int classesNeededFor75(Student s) {
        // (attended + x) / (total + x) >= 0.75
        // attended + x >= 0.75 * total + 0.75x
        // 0.25x >= 0.75*total - attended
        // x >= (0.75*total - attended) / 0.25
        double numerator = SHORTAGE_LIMIT / 100.0 * s.getTotalClasses() - s.getClassesAttended();
        if (numerator <= 0) return 0;
        return (int) Math.ceil(numerator / (1 - SHORTAGE_LIMIT / 100.0));
    }

    /** Print attendance summary statistics */
    public void printSummary() {
        if (studentMap.isEmpty()) { System.out.println("  No data."); return; }

        DoubleSummaryStatistics stats = studentMap.values().stream()
                .mapToDouble(Student::getAttendancePercentage)
                .summaryStatistics();

        long shortageCount = studentMap.values().stream()
                .filter(Student::hasShortage).count();

        System.out.println("\n  ┌─────────────────────────────────────────┐");
        System.out.println("  │           DEPARTMENT SUMMARY            │");
        System.out.println("  ├─────────────────────────────────────────┤");
        System.out.printf( "  │  Total Students    : %-20d│%n", studentMap.size());
        System.out.printf( "  │  Average Attendance: %-19.2f%%│%n", stats.getAverage());
        System.out.printf( "  │  Highest Attendance: %-19.2f%%│%n", stats.getMax());
        System.out.printf( "  │  Lowest  Attendance: %-19.2f%%│%n", stats.getMin());
        System.out.printf( "  │  Students in Shortage: %-18d│%n", shortageCount);
        System.out.println("  └─────────────────────────────────────────┘");
        System.out.println();
    }

    // ═══════════════════════════════════════════════════════════
    //  FILE HANDLING
    // ═══════════════════════════════════════════════════════════

    /**
     * Append today's attendance list to the given file.
     * File layout:
     *   ── DATE: dd-MM-yyyy  |  SUBJECT: xxx  |  TYPE ──
     *   rollNo - Name
     *   ...
     *   (blank line)
     */
    private void writeAttendanceFile(String filename, List<String> entries,
                                      String subject, String type) {
        try (FileWriter fw = new FileWriter(filename, true);
             PrintWriter pw = new PrintWriter(new BufferedWriter(fw))) {

            pw.printf("── DATE: %s  |  SUBJECT: %-15s  |  %s ──%n",
                    today, subject, type);

            if (entries.isEmpty()) {
                pw.println("  (none)");
            } else {
                for (String e : entries) pw.println("  " + e);
            }
            pw.println();   // blank line separating sessions

        } catch (IOException e) {
            System.err.println("  ✗  Error writing to " + filename + ": " + e.getMessage());
        }
    }

    /**
     * Persist student records (all fields) to STUDENTS_FILE.
     * Format (CSV):  rollNo|name|branch|totalClasses|classesAttended
     */
    private void saveStudents() {
        try (PrintWriter pw = new PrintWriter(new BufferedWriter(
                new FileWriter(STUDENTS_FILE)))) {

            for (Student s : studentMap.values()) {
                pw.printf("%s|%s|%s|%d|%d%n",
                        s.getRollNo(), s.getName(), s.getBranch(),
                        s.getTotalClasses(), s.getClassesAttended());
            }
        } catch (IOException e) {
            System.err.println("  ✗  Error saving students: " + e.getMessage());
        }
    }

    /** Load student records from STUDENTS_FILE (if it exists) */
    private void loadStudents() {
        File f = new File(STUDENTS_FILE);
        if (!f.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split("\\|");
                if (parts.length < 5) continue;

                Student s = new Student(parts[0], parts[1], parts[2]);
                s.setTotalClasses(Integer.parseInt(parts[3]));
                s.setClassesAttended(Integer.parseInt(parts[4]));
                studentMap.put(s.getRollNo(), s);
            }
            System.out.println("  ✓  Loaded " + studentMap.size() + " student(s) from file.\n");
        } catch (IOException | NumberFormatException e) {
            System.err.println("  ✗  Error loading students: " + e.getMessage());
        }
    }

    /** Read and display raw contents of the attendees log */
    public void viewAttendeesLog() {
        viewFile(ATTENDEES_FILE, "ATTENDEES LOG");
    }

    /** Read and display raw contents of the absentees log */
    public void viewAbsenteesLog() {
        viewFile(ABSENTEES_FILE, "ABSENTEES LOG");
    }

    private void viewFile(String filename, String title) {
        File f = new File(filename);
        if (!f.exists()) {
            System.out.println("  No log file found: " + filename);
            return;
        }
        System.out.println("\n  ═══  " + title + "  ═══");
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) System.out.println("  " + line);
        } catch (IOException e) {
            System.err.println("  ✗  Error reading " + filename + ": " + e.getMessage());
        }
        System.out.println();
    }
}
