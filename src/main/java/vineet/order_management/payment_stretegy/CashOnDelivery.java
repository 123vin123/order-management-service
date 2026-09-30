package vineet.order_management.payment_stretegy;

import vineet.order_management.model.Booking;
import org.springframework.stereotype.Component;

@Component("COD")
public class CashOnDelivery implements PaymentStrategy {
    @Override
    public String pay(Booking booking) {
        System.out.println("COD payment selected for order " + booking.getReference() + " | Amount: " + booking.getTotal());
        return "COD";
    }
}
