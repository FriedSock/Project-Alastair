package bookings;

public class SeatReservationManager {

    private final Customer[][] seatReservations;
    
    public SeatReservationManager() {
        seatReservations = new Customer[rowToIndex(Seat.MAX_ROW) + 1]
                                       [numberToIndex(Seat.MAX_NUMBER) + 1];
    }

    public boolean isReserved(Seat s) {
        return seatReservations[rowToIndex(s.getRow())]
                               [numberToIndex(s.getNumber())] != null;
    }

    public void reserve(Seat s, Customer c) 
            throws ReservationException {
        if(isReserved(s)) {
            throw new ReservationException();
        }
        seatReservations[rowToIndex(s.getRow())]
                        [numberToIndex(s.getNumber())] = c;
    }
    
    public void unreserve(Seat s)
            throws ReservationException {
        if(!isReserved(s)) {
            throw new ReservationException();
        }
        seatReservations[rowToIndex(s.getRow())]
                        [numberToIndex(s.getNumber())] = null;
    }

    public void reserveNextFree(Customer c) throws ReservationException {
        for(int rowIndex = 0; rowIndex < seatReservations.length; rowIndex++) {
            for(int numberIndex = 0; 
                    numberIndex < seatReservations[rowIndex].length; 
                    numberIndex++) {
                Seat current = new Seat(indexToRow(rowIndex), 
                    indexToNumber(numberIndex));
                if(!isReserved(current)) {
                    reserve(current, c);
                    return;
                }
            }
        }
        throw new ReservationException();
    }
    
    /*@ ghost String toStringResult; in privateState;
        represents theString <- toStringResult;
    @*/
 
    public String toString() {

        String result = " ";
        
        for(int numberIndex = 0; numberIndex < seatReservations[0].length; 
                numberIndex++) {
            result += " " + indexToNumber(numberIndex);
        }
        result += "\n";

        for(int rowIndex = 0;
            rowIndex < seatReservations.length;
            rowIndex++) {
            result += indexToRow(rowIndex);
            for(int numberIndex = 0; numberIndex < 
                    seatReservations[rowIndex].length; numberIndex++) {
                for(int j = numberIndex; j >= 10; j /= 10) {
                    result += " ";
                }
                result += " " + (isReserved(new Seat(indexToRow(rowIndex), 
                    indexToNumber(numberIndex))) ? "X" : " ");
            }
            result += "\n";
        }
        
        //@ set toStringResult = result;
        return result;
    }

    private static int rowToIndex(char row) {
        return row - Seat.MIN_ROW;
    }

    private static int numberToIndex(int number) {
        return number - Seat.MIN_NUMBER;
    }
    
    private static char indexToRow(int index) {
        return (char)(Seat.MIN_ROW + index);
    }

    private static int indexToNumber(int index) {
        return index + Seat.MIN_NUMBER;
    }
    
}
