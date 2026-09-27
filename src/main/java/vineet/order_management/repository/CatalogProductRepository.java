package vineet.order_management.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import vineet.order_management.model.CatalogProduct;

public interface CatalogProductRepository extends JpaRepository<CatalogProduct, Long> {
    List<CatalogProduct> findAllByOrderByNameAsc();

    Optional<CatalogProduct> findBySku(String sku);

    @Modifying
    @Query("update CatalogProduct product set product.stock = product.stock - :quantity "
            + "where product.id = :productId and product.stock >= :quantity")
    int reserveStock(@Param("productId") Long productId, @Param("quantity") int quantity);
}