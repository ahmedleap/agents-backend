package com.agentsbackend.controllers;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;
import java.util.UUID;
import com.agentsbackend.services.OrderService;
import com.agentsbackend.services.MissionAuthorizationService;
import com.agentsbackend.entities.Order;
import com.agentsbackend.DTO.requests.CancelOrderRequest;
import com.agentsbackend.DTO.requests.CreateOrderRequest;
import com.agentsbackend.DTO.requests.GetPendingOrdersRequest;
import com.agentsbackend.DTO.response.CancelOrderResponse;
import com.agentsbackend.DTO.response.CreateOrderResponse;
import com.agentsbackend.DTO.response.OrderSummaryResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;
    private final MissionAuthorizationService authorizationService;

    public OrderController(OrderService orderService, MissionAuthorizationService authorizationService) {
        this.orderService = orderService;
        this.authorizationService = authorizationService;
    }

    // HTTP GET endpoint for viewing pending orders with optional filtering
    @GetMapping("/pending")
    public List<Order> getPendingOrders(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(value = "accountId", required = false) UUID accountId,
            @RequestParam(value = "limit", required = false) Integer limit,
            @RequestParam(value = "offset", required = false) Integer offset) {
        
        if (accountId != null) authorizationService.requireOwnAccountOrPrivilegedRead(jwt, accountId);
        GetPendingOrdersRequest request = new GetPendingOrdersRequest(accountId, limit, offset);
        return orderService.getPendingOrders(request);
    }

    // HTTP DELETE endpoint for cancelling an order
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<CancelOrderResponse> cancelOrder(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID orderId,
            @RequestBody CancelOrderRequest request) {
        authorizationService.requireOwnOrder(authorizationService.clientId(jwt), orderId);
        request.setOrderId(orderId);
        CancelOrderResponse response = orderService.cancelOrder(request);
        return ResponseEntity.ok(response);
    }

    // HTTP POST endpoint for creating a new order
    @PostMapping
    public ResponseEntity<CreateOrderResponse> createOrder(@AuthenticationPrincipal Jwt jwt,
                                                            @Valid @RequestBody CreateOrderRequest request) {
        authorizationService.requireOwnAccount(authorizationService.clientId(jwt), request.getAccountId());
        CreateOrderResponse response = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // HTTP GET endpoint for retrieving order history for an account
    @GetMapping
    public ResponseEntity<List<OrderSummaryResponse>> getOrderHistory(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(value = "accountId") UUID accountId) {
        authorizationService.requireOwnAccountOrPrivilegedRead(jwt, accountId);
        List<OrderSummaryResponse> orders = orderService.getOrderHistory(accountId);
        return ResponseEntity.ok(orders);
    }

    // HTTP GET endpoint for retrieving a specific order by ID
    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrderById(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID orderId) {
        authorizationService.requireOwnOrderOrPrivilegedRead(jwt, orderId);
        Order order = orderService.getOrderById(orderId);
        
        
        return ResponseEntity.ok(order);
    }


    
}

// - post orders (order creation logic)
// - post order preview
// - get orders (history)
// - get specific order information
