// ============================================================
//  Student.java  –  Entity class
// ============================================================
public class Student {

    private String rollNo;
    private String name;
    private String branch;
    private int totalClasses;
    private int classesAttended;

    // ── Constructors ─────────────────────────────────────────
    public Student() {}

    public Student(String rollNo, String name, String branch) {
        this.rollNo        = rollNo;
        this.name          = name;
        this.branch        = branch;
        this.totalClasses  = 0;
        this.classesAttended = 0;
    }

    // ── Business logic ───────────────────────────────────────
    public void markPresent() {
        totalClasses++;
        classesAttended++;
    }

    public void markAbsent() {
        totalClasses++;
    }

    /** @return attendance percentage rounded to 2 decimal places */
    public double getAttendancePercentage() {
        if (totalClasses == 0) return 0.0;
        return Math.round(((double) classesAttended / totalClasses) * 10000.0) / 100.0;
    }

    /** Students below 75 % are considered shortage cases */
    public boolean hasShortage() {
        return getAttendancePercentage() < 75.0;
    }

    // ── Getters & Setters ────────────────────────────────────
    public String getRollNo()            { return rollNo; }
    public void   setRollNo(String r)    { this.rollNo = r; }

    public String getName()              { return name; }
    public void   setName(String n)      { this.name = n; }

    public String getBranch()            { return branch; }
    public void   setBranch(String b)    { this.branch = b; }

    public int getTotalClasses()         { return totalClasses; }
    public void setTotalClasses(int t)   { this.totalClasses = t; }

    public int getClassesAttended()      { return classesAttended; }
    public void setClassesAttended(int a){ this.classesAttended = a; }

    // ── String representation ────────────────────────────────
    @Override
    public String toString() {
        return String.format("%-10s | %-20s | %-8s | %4d / %4d | %6.2f%%  %s",
                rollNo, name, branch,
                classesAttended, totalClasses,
                getAttendancePercentage(),
                hasShortage() ? "[SHORTAGE]" : "");
    }
}
