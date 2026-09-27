package vineet.order_management.controller;

import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import vineet.order_management.model.Account;
import vineet.order_management.model.Booking;
import vineet.order_management.model.CatalogProduct;
import vineet.order_management.service.AccountService;
import vineet.order_management.service.ApiException;
import vineet.order_management.service.OrderService;

@RestController
@RequestMapping("/api")
public class BookingController {
    private final OrderService orderService;
    private final AccountService accountService;

    public BookingController(OrderService orderService, AccountService accountService) {
        this.orderService = orderService;
        this.accountService = accountService;
    }

    @GetMapping("/products")
    public List<Map<String, Object>> products() {
        return orderService.getProducts().stream().map(this::productResponse).toList();
    }

    @PostMapping("/bookings")
    public ResponseEntity<Map<String, Object>> book(@RequestBody BookingRequest request, HttpSession session) {
        Account account = currentAccount(session);
        if (request.productId() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Choose a service.");
        }
        Booking order = orderService.placeOrder(account, request.productId(), request.quantity());
        Map<String, Object> response = bookingResponse(order);
        response.put("emailSent", true);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/bookings")
    public List<Map<String, Object>> myBookings(HttpSession session) {
        Account account = currentAccount(session);
        return orderService.getOrders(account.getId()).stream().map(this::bookingResponse).toList();
    }

    private Account currentAccount(HttpSession session) {
        Object accountId = session.getAttribute("accountId");
        if (!(accountId instanceof Long id)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Please sign in to continue.");
        }
        return accountService.getAccount(id);
    }

    private Map<String, Object> productResponse(CatalogProduct product) {
        Map<String, Object> map = new java.util.HashMap<>();
        map.put("id", product.getId());
        map.put("sku", product.getSku());
        map.put("name", product.getName());
        map.put("description", product.getDescription());
        map.put("price", product.getPrice());
        map.put("stock", product.getStock());
        map.put("imageUrl", product.getImageUrl());
        return map;
    }

    private Map<String, Object> bookingResponse(Booking booking) {
        return new java.util.HashMap<>(Map.of("reference", booking.getReference(),
                "productName", booking.getProduct().getName(), "quantity", booking.getQuantity(),
                "unitPrice", booking.getUnitPrice(), "total", booking.getTotal(),
                "status", booking.getStatus(), "createdAt", booking.getCreatedAt()));
    }

    public record BookingRequest(Long productId, int quantity) {
    }
}