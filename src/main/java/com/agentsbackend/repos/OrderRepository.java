package com.agentsbackend.repos;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Param;
import com.agentsbackend.entities.Order;
import java.util.List;
import java.util.UUID;

@Mapper
public interface OrderRepository {
    
    @Select("SELECT * FROM orders WHERE status = 'PENDING'")
    List<Order> findPendingOrders();

    @Select("SELECT * FROM orders WHERE status = 'PENDING' AND account_id = #{accountId}")
    List<Order> findPendingOrdersByAccount(@Param("accountId") UUID accountId);

    @Select("SELECT * FROM orders WHERE id = #{orderId}")
    Order findById(@Param("orderId") UUID orderId);

    @Update("UPDATE orders SET status = #{status}, updated_at = NOW() WHERE id = #{id}")
    void updateOrder(Order order);
}
