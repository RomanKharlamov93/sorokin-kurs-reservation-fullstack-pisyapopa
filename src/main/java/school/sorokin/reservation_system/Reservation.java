package school.sorokin.reservation_system;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record Reservation (
        Long id,
        Long userId,
        Long roomId,
        LocalDate startDate,
        LocalDateTime endDate,
        ReservationStatus status
) {

}
