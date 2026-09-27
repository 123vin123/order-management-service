package vineet.order_management.model;

public class OrderItem {
    private final Product product;
    private final int quantity;
    private final double priceAtOrder;
    public OrderItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
        this.priceAtOrder = product.getPrice();
    }
    public Product getProduct() {
        return product;
    }
    public int getQuantity() {
        return quantity;
    }
    public double getPriceAtOrder() {
        return priceAtOrder;
    }
    public double getSubtotal() {
        return priceAtOrder * quantity;
    }
    

}
