package BE;

public class Room {
    int      roomNo;
    int      capacity;
    int      occupied;
    String   type;        // AC, Non-AC, Deluxe
    String   block;       // A, B, C
    int      price;
    boolean  available;
    String[] facilities;

    public Room(int roomNo, int capacity, String type, String block) {
        this.roomNo     = roomNo;
        this.capacity   = capacity;
        this.type       = type;
        this.block      = block;
        this.occupied   = 0;
        this.price      = 0;
        this.available  = true;
        this.facilities = new String[]{};
    }

    public boolean isAvailable() {
        return available && occupied < capacity;
    }

    public int getAvailableSeats() {
        return capacity - occupied;
    }
}
