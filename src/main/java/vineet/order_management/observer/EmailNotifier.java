package vineet.order_management.observer;

import org.springframework.stereotype.Component;

import vineet.order_management.model.Booking;
import vineet.order_management.model.OrderStatus;
import vineet.order_management.service.BookingEmailService;

@Component
public class EmailNotifier implements OrderObserver {
    
    private final BookingEmailService emailService;

    public EmailNotifier(BookingEmailService emailService) {
        this.emailService = emailService;
    }

    @Override
    public void onOrderStatusChange(Booking order, OrderStatus oldStatus, OrderStatus newStatus) {
        if (newStatus == OrderStatus.CANCELLED) {
            return;
        }

        System.out.println("  [Email] Order " + order.getReference() + " moved from "
                + oldStatus + " to " + newStatus
                + " | Notifying: " + order.getAccount().getEmail());
                
        emailService.sendConfirmation(order);
    }
}
