package vineet.order_management.config;

import java.math.BigDecimal;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import vineet.order_management.model.CatalogProduct;
import vineet.order_management.repository.CatalogProductRepository;

@Configuration
public class ApplicationConfiguration {
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    CommandLineRunner seedCatalog(CatalogProductRepository products) {
        return args -> {
            if (products.count() == 0) {
                products.save(new CatalogProduct("CLEAN-01", "House Cleaning",
                        "Professional deep cleaning for your entire home. Our trained team uses eco-friendly products to leave every room spotless — kitchens, bathrooms, bedrooms and living areas.",
                        new BigDecimal("1499.00"), 25, "/images/house-cleaning.jpg"));
                products.save(new CatalogProduct("SALON-02", "Home Salon",
                        "Get salon-quality haircuts, styling, facials and beauty treatments at your doorstep. Our certified beauticians bring professional equipment right to your home.",
                        new BigDecimal("999.00"), 30, "/images/home-salon.jpg"));
                products.save(new CatalogProduct("AC-03", "AC Repair & Service",
                        "Expert AC servicing, gas refill, deep cleaning and repair by certified technicians. We service all brands — split, window and central air conditioning units.",
                        new BigDecimal("699.00"), 20, "/images/ac-repair.jpg"));
            }
        };
    }
}