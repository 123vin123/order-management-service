package vineet.order_management.payment_stretegy;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import org.json.JSONObject;
import vineet.order_management.model.Booking;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

@Component("RAZORPAY")
public class RazorPayStrategy implements PaymentStrategy {
    
    private final String keyId;
    private final String keySecret;

    public RazorPayStrategy(
            @Value("${RAZORPAY_KEY_ID:}") String keyId, 
            @Value("${RAZORPAY_KEY_SECRET:}") String keySecret) {
        this.keyId = keyId != null ? keyId.trim() : "";
        this.keySecret = keySecret != null ? keySecret.trim() : "";
        System.out.println("Initializing RazorPayStrategy with Key ID: [" + this.keyId + "] (length: " + this.keyId.length() + ")");
    }

    @Override
    public String pay(Booking booking) {
        try {
            RazorpayClient razorpay = new RazorpayClient(keyId, keySecret);
            
            JSONObject orderRequest = new JSONObject();
            // Razorpay expects amount in paise (multiply INR by 100)
            orderRequest.put("amount", booking.getTotal().multiply(new java.math.BigDecimal("100")).intValue());
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", booking.getReference());
            
            Order order = razorpay.orders.create(orderRequest);
            
            // Return the Razorpay Order ID (e.g. order_IluGWxBm9U8zJ8)
            return order.get("id");
            
        } catch (Exception e) {
            System.err.println("Razorpay Error: " + e.getMessage());
            throw new RuntimeException("Failed to initiate Razorpay payment");
        }
    }
}
