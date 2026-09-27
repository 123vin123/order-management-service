package vineet.order_management.discount_stretegy;

public class NoDiscount implements DiscountStrategy{

    @Override
    public double applyDiscount(double amount) {
            return amount;
    }

}
