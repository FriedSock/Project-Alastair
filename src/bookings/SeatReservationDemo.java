package bookings;

public class SeatReservationDemo {

    public static void main(String[] args) throws ReservationException {
        SeatReservationManager manager = new SeatReservationManager();

        manager.reserve(new Seat('A', 1), new Customer());
        manager.reserve(new Seat('A', 2), new Customer());
        manager.reserve(new Seat('D', 7), new Customer());
        manager.reserve(new Seat('F', 12), new Customer());
        manager.reserve(new Seat('G', 20), new Customer());

        // Try booking a booked seat
        try {
            manager.reserve(new Seat('A', 1), new Customer());
        } catch (ReservationException e) {
        }

        // Try booking a booked seat
        try {
            manager.reserve(new Seat('D', 7), new Customer());
        } catch (ReservationException e) {
        }

        // Try booking a booked seat
        try {
            manager.reserve(new Seat('F', 12), new Customer());
        } catch (ReservationException e) {
        }

        // Try booking a booked seat
        try {
            manager.reserve(new Seat('G', 20), new Customer());
        } catch (ReservationException e) {
        }

        // Try booking a non-existent seat
        try {
            manager.reserve(new Seat('A', -100), new Customer());
        } catch (ArrayIndexOutOfBoundsException e) {
        }

        // Try booking a non-existent seat
        try {
            manager.reserve(new Seat('A', 100), new Customer());
        } catch (ArrayIndexOutOfBoundsException e) {
        }

        // Try booking a non-existent seat
        try {
            manager.reserve(new Seat((char) 0, 1), new Customer());
        } catch (ArrayIndexOutOfBoundsException e) {
        }

        // Try booking a non-existent seat
        try {
            manager.reserve(new Seat('Z', 1), new Customer());
        } catch (ArrayIndexOutOfBoundsException e) {
        }
    }

}
