package io.wespresso_world.wespresso_world.cart;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Optional;
import io.wespresso_world.wespresso_world.beans.BeansRepository;    
import io.wespresso_world.wespresso_world.drinks.DrinksRepository;

@Service
public class CartService {
    
    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private BeansRepository beansRepository;

    @Autowired
    private DrinksRepository drinksRepository;

    // Creates a new cart for a customer
    public Cart createCart(String customerName) {
        Cart cart = new Cart();
        cart.setCustomerName(customerName);
        return cartRepository.save(cart);
    }

    public Cart addDrinkToCart(Long cartId, Long drinkId, int quantity) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

                // Price is determined by the drink's price in database, security check
        var drink = drinksRepository.findById(drinkId)
                .orElseThrow(() -> new RuntimeException("Drink not found"));
                
        CartItem item = new CartItem();
        item.setItemName(drink.getName());
        item.setPrice(drink.getPrice());
        item.setQuantity(quantity);
        item.setCategory("drink");
        item.setCart(cart);

        cart.getItems().add(item);
        return cartRepository.save(cart);

    }

    public Cart addBeansToCart(Long cartId, Long beansId, int quantity) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

                // Price is determined by the beans' price in database, security check
        var beans = beansRepository.findById(beansId)
                .orElseThrow(() -> new RuntimeException("Beans not found"));
                
        CartItem item = new CartItem();
        item.setItemName(beans.getName());
        item.setPrice(beans.getPrice());
        item.setQuantity(quantity);
        item.setCategory("bean");
        item.setCart(cart);

        cart.getItems().add(item);
        return cartRepository.save(cart);

    }


    // Retrieves a cart by its ID
    public Cart getCart(Long cartId) {
        return cartRepository.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));
    }
    // Create the getCartOwner
    public String getCartOwner(Long cartId) {
        return cartRepository.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Cart not found"))
                .getCustomerName();
    }
}
