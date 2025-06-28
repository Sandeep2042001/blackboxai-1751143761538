package com.blingbag.clone.repository;

import com.blingbag.clone.model.OrderItem;
import com.blingbag.clone.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    
    // Basic finder methods
    List<OrderItem> findByOrderId(Long orderId);
    
    List<OrderItem> findByProduct(Product product);
    
    // Find items by product and date range
    @Query("SELECT oi FROM OrderItem oi WHERE oi.product = :product " +
           "AND oi.order.orderDate BETWEEN :startDate AND :endDate")
    List<OrderItem> findByProductAndDateRange(
            @Param("product") Product product,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
    
    // Get total quantity sold for a product
    @Query("SELECT SUM(oi.quantity) FROM OrderItem oi WHERE oi.product = :product")
    Integer getTotalQuantitySold(@Param("product") Product product);
    
    // Get total revenue for a product
    @Query("SELECT SUM(oi.total) FROM OrderItem oi WHERE oi.product = :product")
    BigDecimal getTotalRevenue(@Param("product") Product product);
    
    // Find items by material type
    List<OrderItem> findByMaterialType(String materialType);
    
    // Find items by price range
    List<OrderItem> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);
    
    // Get best-selling products
    @Query("SELECT oi.product, SUM(oi.quantity) as totalQuantity " +
           "FROM OrderItem oi " +
           "GROUP BY oi.product " +
           "ORDER BY totalQuantity DESC")
    Page<Object[]> findBestSellingProducts(Pageable pageable);
    
    // Get items with highest revenue
    @Query("SELECT oi.product, SUM(oi.total) as totalRevenue " +
           "FROM OrderItem oi " +
           "GROUP BY oi.product " +
           "ORDER BY totalRevenue DESC")
    Page<Object[]> findHighestRevenueProducts(Pageable pageable);
    
    // Get sales by material type
    @Query("SELECT oi.materialType, COUNT(oi) as count, SUM(oi.total) as total " +
           "FROM OrderItem oi " +
           "GROUP BY oi.materialType")
    List<Object[]> getSalesByMaterialType();
    
    // Get average order item value
    @Query("SELECT AVG(oi.total) FROM OrderItem oi")
    BigDecimal getAverageOrderItemValue();
    
    // Find items by color
    List<OrderItem> findBySelectedColor(String color);
    
    // Get popular colors
    @Query(value = "SELECT selected_color, COUNT(selected_color) as count " +
           "FROM order_items " +
           "WHERE selected_color IS NOT NULL " +
           "GROUP BY selected_color " +
           "ORDER BY count DESC", nativeQuery = true)
    List<Object[]> findPopularColors(Pageable pageable);
    
    // Get sales trends by date
    @Query(value = "SELECT DATE(order_date) as date, " +
           "COUNT(*) as itemCount, " +
           "SUM(quantity) as totalQuantity, " +
           "SUM(total) as totalRevenue " +
           "FROM order_items oi " +
           "JOIN orders o ON oi.order_id = o.id " +
           "WHERE o.order_date BETWEEN :startDate AND :endDate " +
           "GROUP BY DATE(o.order_date)", nativeQuery = true)
    List<Map<String, Object>> getSalesTrendsByDate(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
    
    // Find items with discounts
    @Query("SELECT oi FROM OrderItem oi WHERE oi.discountAmount > 0")
    List<OrderItem> findItemsWithDiscounts();
    
    // Get average discount percentage
    @Query("SELECT AVG((oi.discountAmount / oi.price) * 100) " +
           "FROM OrderItem oi WHERE oi.discountAmount > 0")
    Double getAverageDiscountPercentage();
    
    // Find items by weight range
    List<OrderItem> findByWeightInGramsBetween(Double minWeight, Double maxWeight);
    
    // Get sales by weight category
    @Query("SELECT " +
           "CASE " +
           "  WHEN oi.weightInGrams < 50 THEN 'Light' " +
           "  WHEN oi.weightInGrams < 100 THEN 'Medium' " +
           "  ELSE 'Heavy' " +
           "END as weightCategory, " +
           "COUNT(oi) as count, " +
           "SUM(oi.total) as totalRevenue " +
           "FROM OrderItem oi " +
           "WHERE oi.weightInGrams IS NOT NULL " +
           "GROUP BY weightCategory")
    List<Object[]> getSalesByWeightCategory();
}
