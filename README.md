# Student Attendance Tracker – Java

## Files
| File | Purpose |
|------|---------|
| `Student.java` | Entity class (data + business logic) |
| `AttendanceManager.java` | Core logic – collections, file I/O, reports |
| `Main.java` | Entry point – interactive console menu |
| `attendees.txt` | **Auto-generated** – daily present log |
| `absentees.txt` | **Auto-generated** – daily absent log |
| `students.dat` | **Auto-generated** – persistent student records |

## Compile & Run
```bash
javac Student.java AttendanceManager.java Main.java
java Main
```

## Menu Options
```
1. Add Student
2. Remove Student
3. Mark Attendance        ← writes attendees.txt & absentees.txt
4. View Full Attendance Report
5. View Shortage List (< 75%)
6. Department Summary
7. Search Student
8. View Attendees Log (file)
9. View Absentees Log (file)
0. Exit
```

## attendees.txt  (sample)
```
── DATE: 01-05-2025  |  SUBJECT: Mathematics      |  ATTENDEES ──
  CS001 - Alice Johnson
  CS002 - Bob Smith
  CS003 - Carol White
```

## absentees.txt  (sample)
```
── DATE: 01-05-2025  |  SUBJECT: Mathematics      |  ABSENTEES ──
  CS004 - David Lee
  CS005 - Eve Brown
```

## Attendance Shortage
Students below **75 %** are flagged and shown how many consecutive
classes they must attend to become eligible again.

Formula used:
  need = ceil( (0.75×total − attended) / 0.25 )
