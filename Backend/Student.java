package BE;

public class Student {
    String name;
    String rollNo;
    String course;
    String mobile;
    int    roomNo;
    String feeStatus;
    String password;
    String registeredOn;

    public Student(String name, String rollNo, String course, String mobile, String password) {
        this.name         = name;
        this.rollNo       = rollNo;
        this.course       = course;
        this.mobile       = mobile;
        this.password     = password;
        this.roomNo       = -1;
        this.feeStatus    = "Pending";
        this.registeredOn = java.time.LocalDate.now().toString();
    }

    public void displayInfo() {
        System.out.printf("%-20s %-10s %-8s %-15s Room: %-5s Fee: %s%n",
            name, rollNo, course, mobile,
            roomNo == -1 ? "N/A" : String.valueOf(roomNo),
            feeStatus);
    }
}
