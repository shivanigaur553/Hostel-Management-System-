package BE;

public class Main {
    public static void main(String[] args) {
        System.out.println("   GEHU Hostel Management System      ");
        System.out.println("   Graphic Era Hill University        ");

        HostelSystem.initDefaultData();

        try {
            Server.start();
            Thread.currentThread().join();
        } catch (Exception e) {
            System.err.println("Server failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
