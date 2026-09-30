package vineet.order_management.payment_stretegy;

import vineet.order_management.model.Booking;
import org.springframework.stereotype.Component;

@Component("UPI")
public class UPI implements PaymentStrategy {
    @Override
    public String pay(Booking booking) {
        System.out.println("Initiating UPI payment of " + booking.getTotal() + " for order " + booking.getReference());
        return "upi://pay?pa=6395814983@ptsbi"
             + "&pn=VineetEService"
             + "&am=" + booking.getTotal().stripTrailingZeros().toPlainString()
             + "&tn=" + booking.getReference()
             + "&cu=INR";
}
}