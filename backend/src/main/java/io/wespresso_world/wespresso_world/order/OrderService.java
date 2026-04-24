package io.wespresso_world.wespresso_world.order;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import io.wespresso_world.wespresso_world.cart.Cart;
import io.wespresso_world.wespresso_world.cart.CartItem;
import io.wespresso_world.wespresso_world.cart.CartRepository;

import java.util.List;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CartRepository cartRepository;

    public Order placeOrder(Long cartId, String shippingAddress, Long userId, String customerName) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Cannot checkout with an empty cart");
        }

        Order order = new Order();
        order.setUserId(userId);
        order.setCustomerName(customerName);
        order.setShippingAddress(shippingAddress);
        order.setTotalPrice(cart.getTotalPrice());

        for (CartItem cartItem : cart.getItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setItemName(cartItem.getItemName());
            orderItem.setCategory(cartItem.getCategory());
            orderItem.setPrice(cartItem.getPrice());
            orderItem.setQuantity(cartItem.getQuantity());
            order.addItem(orderItem);
        }

        Order savedOrder = orderRepository.save(order);

        // Clear cart after successful order
        cart.getItems().clear();
        cartRepository.save(cart);

        return savedOrder;
    }

    public Order getOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }

    public List<Order> getOrdersByUser(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    public String getOrderOwnerUsername(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        return order.getCustomerName();
    }
}
