package com.agentsbackend.controllers;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Orders", description = "Order creation, retrieval, and cancellation")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    private final OrderService orderService;
    private final MissionAuthorizationService authorizationService;

    public OrderController(OrderService orderService, MissionAuthorizationService authorizationService) {
        this.orderService = orderService;
        this.authorizationService = authorizationService;
    }

    // HTTP GET endpoint for viewing pending orders with optional filtering
    @GetMapping("/pending")
        @Operation(summary = "Get pending orders", description = "Returns pending orders for an account with optional pagination.")
        @ApiResponse(responseCode = "200", description = "Pending orders returned")
    public List<Order> getPendingOrders(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "Account whose pending orders should be returned", required = true)
            @RequestParam(value = "accountId") UUID accountId,
            @Parameter(description = "Maximum number of orders to return", example = "20")
            @RequestParam(value = "limit", required = false) Integer limit,
            @Parameter(description = "Number of orders to skip", example = "0")
            @RequestParam(value = "offset", required = false) Integer offset) {
        
        if (accountId != null) authorizationService.requireOwnAccountOrPrivilegedRead(jwt, accountId);
        GetPendingOrdersRequest request = new GetPendingOrdersRequest(accountId, limit, offset);
        return orderService.getPendingOrders(request);
    }

    // HTTP DELETE endpoint for cancelling an order
    @DeleteMapping("/{orderId}")
    @Operation(summary = "Cancel an order", description = "Cancels an order owned by the authenticated client.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Order cancelled"),
        @ApiResponse(responseCode = "400", description = "Invalid cancellation request"),
        @ApiResponse(responseCode = "404", description = "Order not found")
    })
    public ResponseEntity<CancelOrderResponse> cancelOrder(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "Order identifier", required = true)
            @PathVariable("orderId") UUID orderId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Cancellation details",
                required = true,
                content = @Content(schema = @Schema(implementation = CancelOrderRequest.class))
            )
            @RequestBody CancelOrderRequest request) {
        authorizationService.requireOwnOrder(authorizationService.clientId(jwt), orderId);
        request.setOrderId(orderId);
        CancelOrderResponse response = orderService.cancelOrder(request);
        return ResponseEntity.ok(response);
    }

    // HTTP POST endpoint for creating a new order
    @PostMapping
    @Operation(summary = "Create an order", description = "Creates a new order for an account owned by the authenticated client.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Order created"),
        @ApiResponse(responseCode = "400", description = "Invalid order request")
    })
    public ResponseEntity<CreateOrderResponse> createOrder(@AuthenticationPrincipal Jwt jwt,
                                                            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                                                description = "Order creation details",
                                                                required = true,
                                                                content = @Content(schema = @Schema(implementation = CreateOrderRequest.class))
                                                            )
                                                            @Valid @RequestBody CreateOrderRequest request) {
        authorizationService.requireOwnAccount(authorizationService.clientId(jwt), request.getAccountId());
        CreateOrderResponse response = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // HTTP GET endpoint for retrieving order history for an account
    @GetMapping
        @Operation(summary = "Get order history", description = "Returns order history for an account the caller may access.")
    @ApiResponse(responseCode = "200", description = "Order history returned, including account ID, instrument ticker, and submission time")
    public ResponseEntity<List<OrderSummaryResponse>> getOrderHistory(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "Account whose order history should be returned", required = true)
            @RequestParam(value = "accountId") UUID accountId) {
        authorizationService.requireOwnAccountOrPrivilegedRead(jwt, accountId);
        List<OrderSummaryResponse> orders = orderService.getOrderHistory(accountId);
        return ResponseEntity.ok(orders);
    }

    // HTTP GET endpoint for retrieving a specific order by ID
    @GetMapping("/{orderId}")
    @Operation(summary = "Get an order", description = "Returns a specific order the caller may access.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Order returned"),
        @ApiResponse(responseCode = "404", description = "Order not found")
    })
    public ResponseEntity<Order> getOrderById(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "Order identifier", required = true)
            @PathVariable("orderId") UUID orderId) {
        authorizationService.requireOwnOrderOrPrivilegedRead(jwt, orderId);
        Order order = orderService.getOrderById(orderId);
        
        
        return ResponseEntity.ok(order);
    }


    
}

// - post orders (order creation logic)
// - post order preview
// - get orders (history)
// - get specific order information
