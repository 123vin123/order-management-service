package vineet.order_management.payment_stretegy;

import vineet.order_management.model.Booking;

public interface PaymentStrategy {
    String pay(Booking booking);
}