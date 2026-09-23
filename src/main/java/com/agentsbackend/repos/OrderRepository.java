package com.agentsbackend.repos;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import com.agentsbackend.entities.Order;
import java.util.List;
import java.util.UUID;
import java.math.BigDecimal;

@Mapper
public interface OrderRepository {
    
    @Select("SELECT * FROM orders WHERE status = 'PENDING'")
    List<Order> findPendingOrders();

    @Select("SELECT * FROM orders WHERE status = 'PENDING' AND account_id = #{accountId}")
    List<Order> findPendingOrdersByAccount(@Param("accountId") UUID accountId);

    @Select("SELECT * FROM orders WHERE order_id = #{orderId}")
    Order findById(@Param("orderId") UUID orderId);

    @Select("SELECT * FROM orders WHERE account_id = #{accountId} ORDER BY created_at DESC")
    List<Order> findAllByAccount(@Param("accountId") UUID accountId);

    @Update("UPDATE orders SET status = #{status}, updated_at = NOW() WHERE order_id = #{orderId}")
    void updateOrder(Order order);

    @Insert("INSERT INTO orders (order_id, account_id, instrument_id, quantity, limit_price, order_type, status, created_at) " +
            "VALUES (#{orderId}, #{account.accountId}, #{instrument.instrumentId}, #{quantity}, #{limitPrice}, #{orderType}::order_type, #{status}::order_status, #{createdAt})")
    void save(Order order);

    @Select("SELECT COUNT(*) FROM orders WHERE account_id = #{accountId} AND instrument_id = #{instrumentId} " +
            "AND quantity = #{quantity} AND limit_price = #{limitPrice} AND status != 'CANCELLED' " +
            "AND created_at > NOW() - INTERVAL '60 seconds'")
    int countDuplicateOrders(@Param("accountId") UUID accountId, @Param("instrumentId") UUID instrumentId, 
                            @Param("quantity") BigDecimal quantity, @Param("limitPrice") BigDecimal limitPrice);
}
