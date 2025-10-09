package school.sorokin.reservation_system;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
public class ReservationService {

    private final Map<Long, Reservation> reservationMap = Map.of(
            1L, new Reservation(
                    1L,
                    100L,
                    40L,
                    LocalDate.now(),
                    LocalDateTime.now().plusDays(5),
                    ReservationStatus.APPROVED
            ),
            2L, new Reservation(
                    2L,
                    100L,
                    40L,
                    LocalDate.now(),
                    LocalDateTime.now().plusDays(5),
                    ReservationStatus.APPROVED
            )
    );

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
}
