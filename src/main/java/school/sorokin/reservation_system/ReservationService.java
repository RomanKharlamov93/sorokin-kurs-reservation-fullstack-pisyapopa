package school.sorokin.reservation_system;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ReservationService {

    private final Map<Long, Reservation> reservationMap;
    private final AtomicLong idCounter;

    public ReservationService() {
        reservationMap = new HashMap<>();
        idCounter = new AtomicLong(0);
    }

    public Reservation getReservationById(
         Long id
    ) {
         if (!reservationMap.containsKey(id)) {
             throw new NoSuchElementException("Reservation with id " + id + " does not exist");
         }

         return reservationMap.get(id);
    }

    public List<Reservation> getAllReservations() {
        return reservationMap.values().stream().toList();
    };

    public Reservation createReservation(Reservation reservationToCreate) {
         if (reservationToCreate.id() != null) {
             throw new NoSuchElementException("Id should be empty");
         }

         if (reservationToCreate.status() != null) {
             throw new NoSuchElementException("Status should be empty");
         }

         var newReservation = new Reservation(
             idCounter.incrementAndGet(),
                 reservationToCreate.userId(),
                 reservationToCreate.roomId(),
                 reservationToCreate.startDate(),
                 reservationToCreate.endDate(),
                 ReservationStatus.PENDING
         );

         reservationMap.put(newReservation.id(), newReservation);

         return newReservation;
    };
}
