package school.sorokin.reservation_system;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reservation")
public class ReservationController {
    private static final Logger log = LoggerFactory.getLogger(ReservationController.class);

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping("/{id}")
    public  ResponseEntity<Reservation> getReservationById(
        @PathVariable("id") Long id
    ) {
        log.info("ReservationController => CRUD => Get Reservation By Id id= {}", id);
        return ResponseEntity.status(HttpStatus.OK).body(reservationService.getReservationById(id));
    };

    @GetMapping()
    public ResponseEntity<List<Reservation>> getAllReservations() {
        log.info("ReservationController => CRUD => Get All Reservations");
        return ResponseEntity.status(HttpStatus.OK).body(reservationService.getAllReservations());
    };

    @PostMapping()
    public ResponseEntity<Reservation> createReservation(
        @RequestBody Reservation reservationToCreate
    ) {
        log.info("ReservationController => CRUD => Create Reservation");
        return ResponseEntity.status(HttpStatus.CREATED).body(reservationService.createReservation(reservationToCreate));
    }
}
