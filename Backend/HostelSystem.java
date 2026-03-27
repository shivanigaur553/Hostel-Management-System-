package BE;

import java.util.*;

public class HostelSystem {
    static ArrayList<Student> students = new ArrayList<>();
    static ArrayList<Room> rooms = new ArrayList<>();
    static ArrayList<User> users = new ArrayList<>();

    // ── Initialization
    public static void initDefaultData() {
        // Admin user
        users.add(new User("admin", "admin123", "Admin"));

        // Seed rooms
        Room r1 = new Room(101, 2, "AC", "A");
        r1.price = 15000;
        r1.facilities = new String[] { "WiFi", "Hot Water", "Study Table" };
        Room r2 = new Room(102, 2, "Non-AC", "A");
        r2.price = 9000;
        r2.facilities = new String[] { "WiFi", "Study Table" };
        Room r3 = new Room(103, 1, "Deluxe", "B");
        r3.price = 22000;
        r3.facilities = new String[] { "WiFi", "AC", "Hot Water", "TV" };
        Room r4 = new Room(201, 3, "AC", "B");
        r4.price = 12000;
        r4.facilities = new String[] { "WiFi", "Hot Water" };
        Room r5 = new Room(202, 2, "Non-AC", "C");
        r5.price = 8500;
        r5.facilities = new String[] { "WiFi" };

        addRoom(r1);
        addRoom(r2);
        addRoom(r3);
        addRoom(r4);
        addRoom(r5);

        // Seed demo students
        addStudent(new Student("Aryan Negi", "CS101", "CSE", "9876543210", "pass123"));
        // addStudent(new Student("Priya Sharma", "CS102", "CSE", "9876543211",
        // "pass456"));
        // addStudent(new Student("Rohan Mehta", "ME101", "MECH", "9876543212",
        // "pass789"));

        // Allocate rooms to some students
        allocateSpecificRoom("CS101", 101);
        allocateSpecificRoom("CS102", 102);
        markFeePaid("CS101");
    }

    // ── Authentication ─
    public static User login(String username, String password, String role) {
        // Check admin/custom users
        for (User u : users) {
            if (u.getUsername().equals(username)
                    && u.authenticate(password)
                    && u.getRole().equalsIgnoreCase(role)) {
                System.out.println("Login OK: " + username + " (" + role + ")");
                return u;
            }
        }
        // Check students
        if ("Student".equalsIgnoreCase(role)) {
            for (Student s : students) {
                if (s.rollNo.equals(username) && s.password.equals(password)) {
                    System.out.println("Student login OK: " + s.name);
                    return new User(s.rollNo, s.password, "Student");
                }
            }
        }
        System.out.println("Login FAILED: " + username + " role=" + role);
        return null;
    }

    // ── Student Management
    public static boolean addStudent(Student s) {
        for (Student e : students) {
            if (e.rollNo.equals(s.rollNo)) {
                System.out.println("Duplicate rollNo: " + s.rollNo);
                return false;
            }
        }
        students.add(s);
        // Register in users list too so login works
        users.removeIf(u -> u.getUsername().equals(s.rollNo));
        users.add(new User(s.rollNo, s.password, "Student"));
        System.out.println("Student added: " + s.name + " (" + s.rollNo + ")");
        return true;
    }

    public static boolean removeStudent(String rollNo) {
        Iterator<Student> it = students.iterator();
        while (it.hasNext()) {
            Student s = it.next();
            if (s.rollNo.equals(rollNo)) {
                if (s.roomNo != -1) {
                    for (Room r : rooms)
                        if (r.roomNo == s.roomNo && r.occupied > 0)
                            r.occupied--;
                }
                it.remove();
                users.removeIf(u -> u.getUsername().equals(rollNo));
                System.out.println("Student removed: " + rollNo);
                return true;
            }
        }
        return false;
    }

    public static Student findStudent(String rollNo) {
        for (Student s : students)
            if (s.rollNo.equals(rollNo))
                return s;
        return null;
    }

    // ── Room Management
    public static boolean addRoom(Room r) {
        for (Room e : rooms) {
            if (e.roomNo == r.roomNo) {
                System.out.println("Duplicate roomNo: " + r.roomNo);
                return false;
            }
        }
        rooms.add(r);
        System.out.println("Room added: " + r.roomNo + " Block-" + r.block + " " + r.type);
        return true;
    }

    // ── Room Allocation
    public static boolean allocateRoom(String rollNo) {
        Student student = findStudent(rollNo);
        if (student == null || student.roomNo != -1)
            return false;
        for (Room r : rooms) {
            if (r.isAvailable()) {
                r.occupied++;
                student.roomNo = r.roomNo;
                System.out.println("Auto-allocated Room " + r.roomNo + " → " + student.name);
                return true;
            }
        }
        return false;
    }

    public static boolean allocateSpecificRoom(String rollNo, int roomNo) {
        Student student = findStudent(rollNo);
        if (student == null || student.roomNo != -1)
            return false;
        for (Room r : rooms) {
            if (r.roomNo == roomNo) {
                if (!r.isAvailable())
                    return false;
                r.occupied++;
                student.roomNo = roomNo;
                System.out.println("Room " + roomNo + " → " + student.name);
                return true;
            }
        }
        return false;
    }

    public static boolean deallocateRoom(String rollNo) {
        Student student = findStudent(rollNo);
        if (student == null || student.roomNo == -1)
            return false;
        for (Room r : rooms)
            if (r.roomNo == student.roomNo && r.occupied > 0)
                r.occupied--;
        System.out.println("Room " + student.roomNo + " deallocated from " + student.name);
        student.roomNo = -1;
        return true;
    }

    // ── Fee Management ─
    public static void markFeePaid(String rollNo) {
        Student s = findStudent(rollNo);
        if (s != null) {
            s.feeStatus = "Paid";
            System.out.println("Fee Paid: " + rollNo);
        }
    }

    public static void markFeePending(String rollNo) {
        Student s = findStudent(rollNo);
        if (s != null) {
            s.feeStatus = "Pending";
            System.out.println("Fee Pending: " + rollNo);
        }
    }
}
