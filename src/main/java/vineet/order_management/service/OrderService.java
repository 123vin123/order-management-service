package vineet.order_management.service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vineet.order_management.discount_stretegy.DiscountStrategy;
import vineet.order_management.discount_stretegy.NoDiscount;
import vineet.order_management.model.Account;
import vineet.order_management.model.Booking;
import vineet.order_management.model.CatalogProduct;
import vineet.order_management.model.OrderStatus;
import vineet.order_management.observer.OrderObserver;
import vineet.order_management.repository.BookingRepository;
import vineet.order_management.repository.CatalogProductRepository;
import vineet.order_management.validator.OderStateValidator;

@Service
public class OrderService {
    private final BookingRepository bookings;
    private final CatalogProductRepository products;
    private final List<OrderObserver> observers = new CopyOnWriteArrayList<>();
    private final OderStateValidator validator = new OderStateValidator();
    private DiscountStrategy discountStrategy = new NoDiscount();

    public OrderService(BookingRepository bookings, CatalogProductRepository products, List<OrderObserver> observersList) {
        this.bookings = bookings;
        this.products = products;
        if (observersList != null) {
            this.observers.addAll(observersList);
        }
    }

    public void addObserver(OrderObserver observer) {
        this.observers.add(observer);
    }

    public void setDiscountStrategy(DiscountStrategy discountStrategy) {
        this.discountStrategy = discountStrategy;
    }

    @Transactional(readOnly = true)
    public List<CatalogProduct> getProducts() {
        return products.findAllByOrderByNameAsc();
    }

    @Transactional
    public Booking placeOrder(Account account, Long productId, int quantity) {
        if (quantity < 1 || quantity > 100) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Choose a quantity between 1 and 100.");
        }
        CatalogProduct product = products.findById(productId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Service not found."));
        if (products.reserveStock(productId, quantity) != 1) {
            throw new ApiException(HttpStatus.CONFLICT, "There are not enough slots for that quantity.");
        }
        String reference = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Booking order = bookings.save(new Booking(reference, account, product, quantity));

        // Notify observers (Observer Pattern)
        notifyObservers(order, null, OrderStatus.PLACED);

        return order;
    }

    @Transactional(readOnly = true)
    public List<Booking> getOrders(Long accountId) {
        return bookings.findByAccountIdOrderByCreatedAtDesc(accountId);
    }

    @Transactional(readOnly = true)
    public List<Booking> getAllOrders() {
        return bookings.findAll(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt"));
    }

    @Transactional
    public Booking updateOrderStatus(Long bookingId, OrderStatus newStatus) {
        Booking order = bookings.findById(bookingId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Order not found."));
                
        OrderStatus oldStatus = OrderStatus.valueOf(order.getStatus());
        if (!validator.canTransition(oldStatus, newStatus)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Cannot transition from " + oldStatus + " to " + newStatus);
        }
        
        order.setStatus(newStatus.name());
        bookings.save(order);
        
        // Notify observers about the state change
        notifyObservers(order, oldStatus, newStatus);
        
        return order;
    }

    private void notifyObservers(Booking order, OrderStatus oldStatus, OrderStatus newStatus) {
        for (OrderObserver observer : observers) {
            observer.onOrderStatusChange(order, oldStatus, newStatus);
        }
    }
}
