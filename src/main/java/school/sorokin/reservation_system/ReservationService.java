package school.sorokin.reservation_system;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ReservationService {

    private final ReservationRepository repository;

    public ReservationService(ReservationRepository reservationRepository) {
        this.repository = reservationRepository;
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

         var newReservation = new ReservationEntity(
                    null,
                 reservationToCreate.userId(),
                 reservationToCreate.roomId(),
                 reservationToCreate.startDate(),
                 reservationToCreate.endDate(),
                 ReservationStatus.PENDING
         );

         repository.save(newReservation);

         return toDomainReservation(newReservation);
    };

    public Reservation updateReservation(
            Long id,
            Reservation reservationToUpdate) {

        var existedReservation = repository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Reservation with id " + id + " does not exist")
        );

        if (existedReservation.getStatus() != ReservationStatus.PENDING) {
            throw new IllegalStateException("Cannot modify reservation with status " + existedReservation.getStatus());
        }

        var updatedReservation = new ReservationEntity(
                existedReservation.getId(),
                reservationToUpdate.userId(),
                reservationToUpdate.roomId(),
                reservationToUpdate.startDate(),
                reservationToUpdate.endDate(),
                ReservationStatus.PENDING
        );

        repository.save(updatedReservation);

        return toDomainReservation(updatedReservation);
    };

    public void cancelReservation(
            Long id
    ) {
        if (repository.existsById(id)) {
            repository.setStatus(id, ReservationStatus.CANCELLED);
        } else {
            throw new EntityNotFoundException("Reservation with id " + id + " does not exist");
        }
    }

    @Transactional
    public void cancelReservation1(Long id) {
        ReservationEntity reservation = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reservation with id " + id + " does not exist"));
        reservation.setStatus(ReservationStatus.CANCELLED);
    }

    public Reservation approveReservation(Long id) {
        var reservation = repository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Reservation with id " + id + " does not exist")
        );

        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new IllegalStateException("Cannot approve reservation with status " + reservation.getStatus());
        }

        var isConflict = isReservationConflict(reservation);

        if (isConflict) {
            throw new IllegalStateException("Cannot approve reservation with status, due to conflict");
        }

        reservation.setStatus(ReservationStatus.APPROVED);

        repository.save(reservation);

        return toDomainReservation(reservation);

    };

    private boolean isReservationConflict(
            ReservationEntity reservation
    ) {
        var allReservations = repository.findAll();
        for (ReservationEntity existingReservation : allReservations) {
            if (reservation.getId().equals(existingReservation.getId())) {
                continue;
            }
            if (!reservation.getRoomId().equals(existingReservation.getRoomId())) {
                continue;
            }
            if (existingReservation.getStatus() != ReservationStatus.APPROVED) {
                continue;
            }
            if (reservation.getStartDate().isBefore(existingReservation.getEndDate())
                    && existingReservation.getStartDate().isBefore(reservation.getEndDate())) {
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
