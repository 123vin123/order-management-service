package vineet.order_management.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/* 
The Order class uses the Builder pattern to construct complex order objects with validation. The builder ensures that an order always has an id, customer, and at least one item before it can be built.
It will prevent from partial contruction of order.
*/
public class Order {
 private final String id;
 private final Customer customer;
 private final List<OrderItem>items;
 private OrderStatus status;
 private final double totalAmount;
 private final LocalDateTime createdAt;
 private LocalDateTime updatedAt;

 private Order(Builder builder) {
    this.id = builder.id;
    this.customer = builder.customer;
    this.items = Collections.unmodifiableList(new ArrayList<>(builder.items)); // Make items list immutable
    this.status = OrderStatus.PLACED; // Default status when order is created
    this.totalAmount = builder.totalAmount;
    this.createdAt = LocalDateTime.now();
    this.updatedAt = this.updatedAt;

 }

 public void setStatus(OrderStatus status){
    this.status = status;
    this.updatedAt = LocalDateTime.now();
 }

 public String getId() {
     return id;
 }

 public Customer getCustomer() {
     return customer;
 }

 public List<OrderItem> getItems() {
     return items;
 }

 public OrderStatus getStatus() {
     return status;
 }

 public double getTotalAmount() {
     return totalAmount;
 }

 public LocalDateTime getCreatedAt() {
     return createdAt;
 }

 public LocalDateTime getUpdatedAt() {
     return updatedAt;
 }


 @Override 
 public  String toString() {
     return "Order{id=" + id + " status=" + status + " total=$" + totalAmount + "}";
 }



 public static class Builder {
    private String id;
    private Customer customer;
    private List<OrderItem> items = new ArrayList<>();
    private double totalAmount;

    public Builder id(String id){
        this.id = id;
        return this;
    }
    public Builder customer(Customer customer){
        this.customer = customer;
        return this;
    }
    public Builder addItem(OrderItem item){
        this.items.add(item);
        return this;
    }
    public Builder totalAmount(double totalAmount){
        this.totalAmount = totalAmount;
        return this;
    }


    public Order build() {
        if(id == null || customer == null || items.isEmpty()){
            throw new IllegalStateException("Order must have an id, customer, and at least one item.");
        }
        return new Order(this);
    }

 }

}
