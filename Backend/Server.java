package BE;

import com.sun.net.httpserver.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class Server {

    static final int PORT = 8080;

    public static void start() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        server.createContext("/api/rooms", new RoomsHandler());
        server.createContext("/api/students", new StudentsHandler());
        server.createContext("/api/login", new LoginHandler());
        server.createContext("/api/stats", new StatsHandler());

        server.setExecutor(null);
        server.start();
        System.out.println("GEHU Hostel — Server started ");
        System.out.println("http://localhost:" + PORT + " ");
    }

    // ── CORS helper
    static void cors(HttpExchange ex) throws IOException {
        Headers h = ex.getResponseHeaders();
        h.set("Access-Control-Allow-Origin", "*");
        h.set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        h.set("Access-Control-Allow-Headers", "Content-Type, Authorization");
        h.set("Content-Type", "application/json; charset=utf-8");
    }

    static void ok(HttpExchange ex, String json) throws IOException {
        cors(ex);
        byte[] b = json.getBytes(StandardCharsets.UTF_8);
        ex.sendResponseHeaders(200, b.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(b);
        }
    }

    static void err(HttpExchange ex, int code, String msg) throws IOException {
        cors(ex);
        byte[] b = ("{\"error\":\"" + msg + "\"}").getBytes(StandardCharsets.UTF_8);
        ex.sendResponseHeaders(code, b.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(b);
        }
    }

    static String body(HttpExchange ex) throws IOException {
        return new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    // Simple JSON value extractor (handles strings and booleans)
    static String val(String json, String key) {
        String search = "\"" + key + "\"";
        int i = json.indexOf(search);
        if (i < 0)
            return "";
        i = json.indexOf(":", i + search.length());
        if (i < 0)
            return "";
        i++;
        // Skip whitespace
        while (i < json.length() && json.charAt(i) == ' ')
            i++;
        if (i >= json.length())
            return "";
        boolean quoted = json.charAt(i) == '"';
        if (quoted)
            i++;
        StringBuilder sb = new StringBuilder();
        for (; i < json.length(); i++) {
            char c = json.charAt(i);
            if (quoted && c == '"')
                break;
            if (!quoted && (c == ',' || c == '}' || c == ' '))
                break;
            sb.append(c);
        }
        return sb.toString().trim();
    }

    // ── OPTIONS preflight helper ─────────────────────────────────────
    static boolean preflight(HttpExchange ex) throws IOException {
        if ("OPTIONS".equals(ex.getRequestMethod())) {
            cors(ex);
            ex.sendResponseHeaders(204, -1);
            return true;
        }
        return false;
    }

    // /api/rooms
    static class RoomsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if (preflight(ex))
                return;
            String method = ex.getRequestMethod();
            String path = ex.getRequestURI().getPath();

            // GET /api/rooms
            if ("GET".equals(method) && "/api/rooms".equals(path)) {
                ok(ex, roomsJson());
                return;
            }
            // POST /api/rooms
            if ("POST".equals(method) && "/api/rooms".equals(path)) {
                String b = body(ex);
                try {
                    int roomNo = Integer.parseInt(val(b, "roomNo"));
                    int capacity = Integer.parseInt(val(b, "capacity"));
                    String type = val(b, "type");
                    String block = val(b, "block");
                    int price = Integer.parseInt(val(b, "price"));

                    Room nr = new Room(roomNo, capacity, type, block);
                    nr.price = price;
                    if (HostelSystem.addRoom(nr))
                        ok(ex, "{\"message\":\"Room added\"}");
                    else
                        err(ex, 409, "Room already exists");
                } catch (Exception e) {
                    err(ex, 400, "Invalid data: " + e.getMessage());
                }
                return;
            }
            // DELETE /api/rooms/{roomNo}
            if ("DELETE".equals(method) && path.startsWith("/api/rooms/")) {
                try {
                    int rn = Integer.parseInt(path.substring("/api/rooms/".length()));
                    boolean removed = HostelSystem.rooms.removeIf(r -> r.roomNo == rn);
                    if (removed)
                        ok(ex, "{\"message\":\"Deleted\"}");
                    else
                        err(ex, 404, "Room not found");
                } catch (Exception e) {
                    err(ex, 400, "Bad room number");
                }
                return;
            }
            // PUT /api/rooms/{roomNo}
            if ("PUT".equals(method) && path.startsWith("/api/rooms/")) {
                try {
                    int rn = Integer.parseInt(path.substring("/api/rooms/".length()));
                    String b = body(ex);
                    boolean found = false;
                    for (Room r : HostelSystem.rooms) {
                        if (r.roomNo == rn) {
                            found = true;
                            String type = val(b, "type");
                            String block = val(b, "block");
                            String cap = val(b, "capacity");
                            String occ = val(b, "occupied");
                            String prc = val(b, "price");
                            String avail = val(b, "available");
                            if (!type.isEmpty())
                                r.type = type;
                            if (!block.isEmpty())
                                r.block = block;
                            if (!cap.isEmpty())
                                r.capacity = Integer.parseInt(cap);
                            if (!occ.isEmpty())
                                r.occupied = Integer.parseInt(occ);
                            if (!prc.isEmpty())
                                r.price = Integer.parseInt(prc);
                            if (!avail.isEmpty())
                                r.available = Boolean.parseBoolean(avail);
                            ok(ex, "{\"message\":\"Updated\"}");
                            break;
                        }
                    }
                    if (!found)
                        err(ex, 404, "Room not found");
                } catch (Exception e) {
                    err(ex, 400, "Bad data: " + e.getMessage());
                }
                return;
            }
            err(ex, 405, "Method not allowed");
        }
    }

    // /api/students
    static class StudentsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if (preflight(ex))
                return;
            String method = ex.getRequestMethod();
            String path = ex.getRequestURI().getPath();

            // GET /api/students
            if ("GET".equals(method) && "/api/students".equals(path)) {
                ok(ex, studentsJson());
                return;
            }
            // GET /api/students/{rollNo}
            if ("GET".equals(method) && path.startsWith("/api/students/")
                    && !path.contains("/room") && !path.contains("/fee")) {
                String rollNo = path.substring("/api/students/".length());
                Student s = HostelSystem.findStudent(rollNo);
                if (s != null)
                    ok(ex, studentJson(s));
                else
                    err(ex, 404, "Student not found");
                return;
            }
            // POST /api/students — register new student
            if ("POST".equals(method) && "/api/students".equals(path)) {
                String b = body(ex);
                try {
                    String name = val(b, "name");
                    String rollNo = val(b, "rollNo");
                    String course = val(b, "course");
                    String mobile = val(b, "mobile");
                    String pass = val(b, "password");
                    if (name.isEmpty() || rollNo.isEmpty() || pass.isEmpty()) {
                        err(ex, 400, "name, rollNo and password are required");
                        return;
                    }
                    boolean added = HostelSystem.addStudent(new Student(name, rollNo, course, mobile, pass));
                    if (added)
                        ok(ex, "{\"message\":\"Student registered\"}");
                    else
                        err(ex, 409, "Roll number already exists");
                } catch (Exception e) {
                    err(ex, 400, e.getMessage());
                }
                return;
            }
            // DELETE /api/students/{rollNo}
            if ("DELETE".equals(method) && path.startsWith("/api/students/")
                    && !path.contains("/room") && !path.contains("/fee")) {
                String rollNo = path.substring("/api/students/".length());
                if (HostelSystem.removeStudent(rollNo))
                    ok(ex, "{\"message\":\"Removed\"}");
                else
                    err(ex, 404, "Student not found");
                return;
            }
            // PUT /api/students/{rollNo}/fee
            if ("PUT".equals(method) && path.contains("/fee")) {
                String rollNo = path.split("/")[3];
                String b = body(ex);
                String status = val(b, "status");
                Student s = HostelSystem.findStudent(rollNo);
                if (s == null) {
                    err(ex, 404, "Student not found");
                    return;
                }
                if ("Paid".equalsIgnoreCase(status))
                    HostelSystem.markFeePaid(rollNo);
                else
                    HostelSystem.markFeePending(rollNo);
                ok(ex, "{\"message\":\"Fee updated\"}");
                return;
            }
            // PUT /api/students/{rollNo}/room — allocate
            if ("PUT".equals(method) && path.contains("/room")) {
                String rollNo = path.split("/")[3];
                String b = body(ex);
                String roomNoStr = val(b, "roomNo");
                boolean result;
                if (roomNoStr.isEmpty())
                    result = HostelSystem.allocateRoom(rollNo);
                else
                    result = HostelSystem.allocateSpecificRoom(rollNo, Integer.parseInt(roomNoStr));
                if (result)
                    ok(ex, "{\"message\":\"Room allocated\"}");
                else
                    err(ex, 400, "Could not allocate room — already assigned or room full");
                return;
            }
            // DELETE /api/students/{rollNo}/room — deallocate
            if ("DELETE".equals(method) && path.contains("/room")) {
                String rollNo = path.split("/")[3];
                if (HostelSystem.deallocateRoom(rollNo))
                    ok(ex, "{\"message\":\"Room deallocated\"}");
                else
                    err(ex, 400, "Could not deallocate");
                return;
            }
            err(ex, 405, "Method not allowed");
        }
    }

    // /api/login
    static class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if (preflight(ex))
                return;
            if (!"POST".equals(ex.getRequestMethod())) {
                err(ex, 405, "POST only");
                return;
            }
            String b = body(ex);
            String username = val(b, "username");
            String password = val(b, "password");
            String role = val(b, "role");
            if (username.isEmpty() || password.isEmpty() || role.isEmpty()) {
                err(ex, 400, "username, password and role required");
                return;
            }
            User u = HostelSystem.login(username, password, role);
            if (u != null) {
                // For student role, include extra profile data
                if ("Student".equalsIgnoreCase(u.getRole())) {
                    Student s = HostelSystem.findStudent(u.getUsername());
                    if (s != null) {
                        ok(ex, String.format(
                                "{\"success\":true,\"role\":\"Student\",\"username\":\"%s\",\"name\":\"%s\",\"course\":\"%s\",\"roomNo\":%d,\"feeStatus\":\"%s\"}",
                                s.rollNo, s.name, s.course, s.roomNo, s.feeStatus));
                        return;
                    }
                }
                ok(ex, String.format("{\"success\":true,\"role\":\"%s\",\"username\":\"%s\",\"name\":\"%s\"}",
                        u.getRole(), u.getUsername(), u.getUsername()));
            } else {
                err(ex, 401, "Invalid credentials");
            }
        }
    }

    // /api/stats
    static class StatsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if (preflight(ex))
                return;
            cors(ex); // ← FIX: was missing in original
            long allocated = HostelSystem.students.stream().filter(s -> s.roomNo != -1).count();
            long pendingFee = HostelSystem.students.stream().filter(s -> "Pending".equals(s.feeStatus)).count();
            long availRooms = HostelSystem.rooms.stream().filter(r -> r.isAvailable()).count();
            String json = String.format(
                    "{\"totalStudents\":%d,\"totalRooms\":%d,\"allocated\":%d,\"pendingFee\":%d,\"availableRooms\":%d}",
                    HostelSystem.students.size(), HostelSystem.rooms.size(), allocated, pendingFee, availRooms);
            byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
            ex.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = ex.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

    // JSON builders
    static String roomsJson() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < HostelSystem.rooms.size(); i++) {
            if (i > 0)
                sb.append(",");
            sb.append(roomJson(HostelSystem.rooms.get(i)));
        }
        return sb.append("]").toString();
    }

    static String roomJson(Room r) {
        return String.format(
                "{\"roomNo\":%d,\"block\":\"%s\",\"type\":\"%s\",\"capacity\":%d,\"occupied\":%d,\"price\":%d,\"available\":%b,\"facilities\":[%s]}",
                r.roomNo, r.block, r.type, r.capacity, r.occupied, r.price, r.available,
                facilitiesJson(r.facilities));
    }

    static String facilitiesJson(String[] facs) {
        if (facs == null || facs.length == 0)
            return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < facs.length; i++) {
            if (i > 0)
                sb.append(",");
            sb.append("\"").append(facs[i]).append("\"");
        }
        return sb.toString();
    }

    static String studentsJson() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < HostelSystem.students.size(); i++) {
            if (i > 0)
                sb.append(",");
            sb.append(studentJson(HostelSystem.students.get(i)));
        }
        return sb.append("]").toString();
    }

    static String studentJson(Student s) {
        return String.format(
                "{\"name\":\"%s\",\"rollNo\":\"%s\",\"course\":\"%s\",\"mobile\":\"%s\",\"roomNo\":%d,\"feeStatus\":\"%s\",\"registeredOn\":\"%s\"}",
                s.name, s.rollNo, s.course, s.mobile, s.roomNo, s.feeStatus, s.registeredOn);
    }
}
