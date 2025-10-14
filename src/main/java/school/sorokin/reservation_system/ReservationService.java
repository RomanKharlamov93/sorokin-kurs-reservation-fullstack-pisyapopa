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

    public Reservation updateReservation(
            Long id,
            Reservation reservationToUpdate) {
        if (!reservationMap.containsKey(id)) {
            throw new NoSuchElementException("Reservation with id " + id + " does not exist");
        }
        var reservation = reservationMap.get(id);

        if (reservation.status() != ReservationStatus.PENDING) {
            throw new IllegalStateException("Cannot modify reservation with status " + reservation.status());
        }

        var updatedReservation = new Reservation(
                reservation.id(),
                reservationToUpdate.userId(),
                reservationToUpdate.roomId(),
                reservationToUpdate.startDate(),
                reservationToUpdate.endDate(),
                ReservationStatus.PENDING
        );

        reservationMap.put(reservation.id(), updatedReservation);

        return updatedReservation;
    };

    public void deleteReservation(
            Long id
    ) {
        if (reservationMap.containsKey(id)) {
            reservationMap.remove(id);
        } else {
            throw new NoSuchElementException("Reservation with id " + id + " does not exist");
        }
    }

    public Reservation approveReservation(Long id) {
        if (!reservationMap.containsKey(id)) {
            throw new NoSuchElementException("Reservation with id " + id + " does not exist");
        }

        var reservation = reservationMap.get(id);

        if (reservation.status() != ReservationStatus.PENDING) {
            throw new IllegalStateException("Cannot approve reservation with status " + reservation.status());
        }

        var isConflict = isReservationConflict(reservation);

        if (isConflict) {
            throw new IllegalStateException("Cannot approve reservation with status, due to conflict");
        }

        var approvedReservation = new Reservation(
                reservation.id(),
                reservation.userId(),
                reservation.roomId(),
                reservation.startDate(),
                reservation.endDate(),
                ReservationStatus.APPROVED
        );

        reservationMap.put(id, approvedReservation);

        return approvedReservation;

    };

    private boolean isReservationConflict(
            Reservation reservation
    ) {
        for (Reservation existingReservation : reservationMap.values()) {
            if (reservation.id().equals(existingReservation.id())) {
                continue;
            }
            if (!reservation.roomId().equals(existingReservation.roomId())) {
                continue;
            }
            if (existingReservation.status() != ReservationStatus.APPROVED) {
                continue;
            }
            if (reservation.startDate().isBefore(existingReservation.endDate()) && existingReservation.startDate().isBefore(reservation.endDate())) {
                return true;
            }
        }
        return false;
    };
}
