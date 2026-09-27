package vineet.order_management.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import vineet.order_management.model.Booking;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByAccountIdOrderByCreatedAtDesc(Long accountId);
}