package vineet.order_management.observer;

import vineet.order_management.model.Booking;
import vineet.order_management.model.OrderStatus;

public interface OrderObserver {

    void onOrderStatusChange(Booking order, OrderStatus oldStatus, OrderStatus newStatus);

}
