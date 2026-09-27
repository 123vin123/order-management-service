package vineet.order_management.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import vineet.order_management.model.Booking;
import vineet.order_management.model.OrderStatus;
import vineet.order_management.service.OrderService;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final OrderService orderService;

    public AdminController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/bookings")
    public List<Map<String, Object>> allBookings() {
        return orderService.getAllOrders().stream().map(this::bookingResponse).toList();
    }

    @PatchMapping("/bookings/{id}/status")
    public ResponseEntity<Map<String, Object>> updateStatus(@PathVariable Long id, @RequestBody StatusUpdateRequest request) {
        OrderStatus newStatus = OrderStatus.valueOf(request.status());
        Booking updatedOrder = orderService.updateOrderStatus(id, newStatus);
        return ResponseEntity.ok(bookingResponse(updatedOrder));
    }

    private Map<String, Object> bookingResponse(Booking booking) {
        return new java.util.HashMap<>(Map.of(
                "id", booking.getId(),
                "reference", booking.getReference(),
                "productName", booking.getProduct().getName(),
                "customerName", booking.getAccount().getName(),
                "customerEmail", booking.getAccount().getEmail(),
                "quantity", booking.getQuantity(),
                "total", booking.getTotal(),
                "status", booking.getStatus(),
                "createdAt", booking.getCreatedAt()));
    }

    public record StatusUpdateRequest(String status) {}
}
