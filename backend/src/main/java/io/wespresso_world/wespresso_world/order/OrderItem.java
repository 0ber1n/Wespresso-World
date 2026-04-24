package io.wespresso_world.wespresso_world.order;

import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;

@Entity
@Data
@Schema(description = "Line item within an order")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Schema(description = "Unique identifier for the order item")
    private Long id;

    @Schema(description = "Name of the item")
    private String itemName;

    @Schema(description = "Category of the item (drink or bean)")
    private String category;

    @Schema(description = "Price at time of purchase")
    private Double price;

    @Schema(description = "Quantity ordered")
    private Integer quantity;

    @ManyToOne
    @JoinColumn(name = "order_id")
    @JsonIgnore
    @Schema(hidden = true)
    private Order order;
}
