package vineet.order_management.model;

import java.util.concurrent.locks.ReentrantLock;

public class Product {
    private final String sku;
    private final String name;
    private final double price;
    private int stock;
    private final ReentrantLock lock = new ReentrantLock();

    public Product(String sku, String name, double price, int stock) {
        this.sku = sku;
        this.name = name;
        this.price = price;
        this.stock = stock;
    }

    public String getSku() {
        return sku;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public void deductStock(int quantity){
        lock.lock();
        try{
             if(stock < quantity){
                    throw new IllegalArgumentException("Insufficient stock for product: " + sku + ", Available stock: " + stock + ", Requested quantity: " + quantity);
             } 
             stock -= quantity;
        }
        catch (Exception e){
            e.printStackTrace();
        }
        finally {
            lock.unlock();
        }
    }


    public void restoreStock(int quantity){
        lock.lock();
        try{
             stock += quantity; 
        }
        catch (Exception e){
            e.printStackTrace();
        }
        finally {
            lock.unlock();
        }
    }
    public int getStock() {
        lock.lock();
        try {
            return stock;
        } finally {
            lock.unlock();
        }
    }
    

}
