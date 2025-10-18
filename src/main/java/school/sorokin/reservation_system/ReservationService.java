package school.sorokin.reservation_system;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ReservationService {

    private final Map<Long, Reservation> reservationMap;
    private final AtomicLong idCounter;

    private final ReservationRepository repository;

    public ReservationService(ReservationRepository reservationRepository) {
        this.repository = reservationRepository;
        reservationMap = new HashMap<>();
        idCounter = new AtomicLong(0);
    }

    public Reservation getReservationById(
         Long id
    ) {
        ReservationEntity reservationEntity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reservation with id " + id + " does not exist"));

        return toDomainReservation(reservationEntity);
    }

    public List<Reservation> getAllReservations() {
        List<ReservationEntity> list = repository.findAll();

        List<Reservation> result = list.stream()
                .map(this::toDomainReservation).toList();
        return result;
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

    private Reservation toDomainReservation(ReservationEntity reservation) {
        return new Reservation(
                reservation.getId(),
                reservation.getUserId(),
                reservation.getRoomId(),
                reservation.getStartDate(),
                reservation.getEndDate(),
                reservation.getStatus()
        );
    }
}
