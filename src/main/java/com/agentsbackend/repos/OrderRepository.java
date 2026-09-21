package com.agentsbackend.repos;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import com.agentsbackend.entities.Order;
import java.util.List;

@Mapper
public interface OrderRepository {
    
    @Select("SELECT * FROM orders WHERE status = 'PENDING'")
    List<Order> findPendingOrders();
}
