package vineet.order_management.discount_stretegy;

public class PercentageDiscount implements DiscountStrategy {
  private final double percentage;

  public PercentageDiscount(double percentage) {
      if (percentage < 0 || percentage > 100)
          throw new IllegalArgumentException("Invalid percentage");
      this.percentage = percentage; 
  }

    @Override
    public double applyDiscount(double amount) {
          return amount * (1 - percentage / 100.0);
    }
    

}
