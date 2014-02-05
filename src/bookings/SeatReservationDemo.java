package bookings;

public class SeatReservationDemo {

    public static void main(String[] args) throws ReservationException {
        SeatReservationManager manager = new SeatReservationManager();

        manager.reserve(new Seat('A', 1), new Customer());
        manager.reserve(new Seat('D', 5), new Customer());
        manager.reserve(new Seat('F', 12), new Customer());
        manager.reserve(new Seat('G', 19), new Customer());

    }

}
