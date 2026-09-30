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
import vineet.order_management.payment_stretegy.PaymentStrategyFactory;

@RestController
@RequestMapping("/api")
public class BookingController {
    private final OrderService orderService;
    private final AccountService accountService;
    private final PaymentStrategyFactory paymentStrategyFactory;

    public BookingController(
            OrderService orderService, 
            AccountService accountService,
            PaymentStrategyFactory paymentStrategyFactory) {
        this.orderService = orderService;
        this.accountService = accountService;
        this.paymentStrategyFactory = paymentStrategyFactory;
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
        
        try {
            vineet.order_management.payment_stretegy.PaymentContext paymentContext = new vineet.order_management.payment_stretegy.PaymentContext();
            vineet.order_management.payment_stretegy.PaymentStrategy strategy = paymentStrategyFactory.getStrategy(request.paymentMethod());
            paymentContext.setPaymentStrategy(strategy);
            
            String paymentId = paymentContext.executePayment(order);
            response.put("paymentId", paymentId);
        } catch (Exception e) {
            System.err.println("Failed to initiate payment: " + e.getMessage());
            orderService.updateOrderStatus(order.getId(), vineet.order_management.model.OrderStatus.CANCELLED);
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Payment initialization failed: " + e.getMessage());
        }
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/bookings")
    public List<Map<String, Object>> myBookings(HttpSession session) {
        Account account = currentAccount(session);
        return orderService.getOrders(account.getId()).stream().map(this::bookingResponse).toList();
    }

    @PostMapping("/bookings/{id}/verify")
    public ResponseEntity<Map<String, Object>> verifyPayment(
            @org.springframework.web.bind.annotation.PathVariable Long id,
            @RequestBody Map<String, String> payload,
            HttpSession session,
            @org.springframework.beans.factory.annotation.Value("${RAZORPAY_KEY_SECRET:}") String razorpaySecret) {
        Account account = currentAccount(session);
        Booking order = orderService.getOrders(account.getId()).stream()
                .filter(b -> b.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Order not found."));

        String razorpayPaymentId = payload.get("razorpay_payment_id");
        String razorpayOrderId = payload.get("razorpay_order_id");
        String razorpaySignature = payload.get("razorpay_signature");

        if (razorpayPaymentId == null || razorpayOrderId == null || razorpaySignature == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Missing payment details");
        }

        try {
            org.json.JSONObject attributes = new org.json.JSONObject();
            attributes.put("razorpay_order_id", razorpayOrderId);
            attributes.put("razorpay_payment_id", razorpayPaymentId);
            attributes.put("razorpay_signature", razorpaySignature);

            boolean isSignatureValid = com.razorpay.Utils.verifyPaymentSignature(attributes, razorpaySecret.trim());

            if (isSignatureValid) {
                Booking confirmed = orderService.updateOrderStatus(order.getId(), vineet.order_management.model.OrderStatus.CONFIRMED);
                return ResponseEntity.ok(bookingResponse(confirmed));
            } else {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Signature verification failed");
            }
        } catch (Exception e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Signature verification failed: " + e.getMessage());
        }
    }

    @PostMapping("/bookings/{id}/cancel")
    public ResponseEntity<Map<String, Object>> cancelBooking(@org.springframework.web.bind.annotation.PathVariable Long id, HttpSession session) {
        Account account = currentAccount(session);
        Booking order = orderService.getOrders(account.getId()).stream()
                .filter(b -> b.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Order not found."));
        Booking cancelled = orderService.updateOrderStatus(order.getId(), vineet.order_management.model.OrderStatus.CANCELLED);
        return ResponseEntity.ok(bookingResponse(cancelled));
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
        Map<String, Object> map = new java.util.HashMap<>();
        map.put("id", booking.getId());
        map.put("reference", booking.getReference());
        map.put("productName", booking.getProduct().getName());
        map.put("quantity", booking.getQuantity());
        map.put("unitPrice", booking.getUnitPrice());
        map.put("total", booking.getTotal());
        map.put("status", booking.getStatus());
        map.put("createdAt", booking.getCreatedAt());
        return map;
    }

    public record BookingRequest(Long productId, int quantity, String paymentMethod) {
    }
}