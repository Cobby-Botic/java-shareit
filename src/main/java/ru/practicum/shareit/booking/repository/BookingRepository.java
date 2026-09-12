package ru.practicum.shareit.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findAllByBookerId(Long userId);

    List<Booking> findAllByItemOwner(Long ownerId);

    boolean existsByBookerIdAndItemIdAndEndBefore(
            Long bookerId,
            Long itemId,
            LocalDateTime time
    );

    Optional<Booking> findFirstByItemIdAndEndBeforeOrderByEndDesc(
            Long itemId,
            LocalDateTime now
    );

    Optional<Booking> findFirstByItemIdAndStartAfterOrderByStartAsc(
            Long itemId,
            LocalDateTime now
    );
}
