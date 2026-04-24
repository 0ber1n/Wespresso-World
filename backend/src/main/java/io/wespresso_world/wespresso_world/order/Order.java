package io.wespresso_world.wespresso_world.order;

import jakarta.persistence.*;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table(name = "orders")
@Schema(description = "Order placed by a customer")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Schema(description = "Unique identifier for the order")
    private Long id;

    @Schema(description = "User ID who placed the order")
    private Long userId;

    @Schema(description = "Customer name")
    private String customerName;

    @Schema(description = "Shipping address")
    private String shippingAddress;

    @Schema(description = "Order status")
    private String status;

    @Schema(description = "Total price of the order")
    private Double totalPrice;

    @Schema(description = "Timestamp when the order was placed")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        status = "PENDING";
    }

    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }
}
