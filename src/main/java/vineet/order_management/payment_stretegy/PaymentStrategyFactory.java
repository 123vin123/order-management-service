package vineet.order_management.payment_stretegy;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import java.util.Map;

@Component
public class PaymentStrategyFactory {

    private final Map<String, PaymentStrategy> strategies;
    private final boolean razorpayEnabled;

    public PaymentStrategyFactory(
            Map<String, PaymentStrategy> strategies,
            @Value("${RAZORPAY_KEY_ID:}") String razorpayKey) {
        this.strategies = strategies;
        this.razorpayEnabled = razorpayKey != null && !razorpayKey.isEmpty();
    }

    public PaymentStrategy getStrategy(String paymentMethod) {
        if (paymentMethod == null) {
            throw new IllegalArgumentException("Payment method cannot be null");
        }
        
        String key = paymentMethod.toUpperCase();
        
        // If the user requested UPI but Razorpay is configured, use Razorpay
        if ("UPI".equals(key) && razorpayEnabled) {
            key = "RAZORPAY";
        }
        
        PaymentStrategy strategy = strategies.get(key);
        if (strategy == null) {
            throw new IllegalArgumentException("Unsupported payment method: " + paymentMethod);
        }
        
        return strategy;
    }
}
