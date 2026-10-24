package fashionstorebackend.controller;

import fashionstorebackend.dto.cart.CartItemRequest;
import fashionstorebackend.dto.cart.CartResponse;
import fashionstorebackend.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping("/user/{userId}")
    public CartResponse getCart(
            @PathVariable Long userId) {

        return cartService.getCartByUserId(userId);
    }

    @PostMapping("/user/{userId}")
    public CartResponse getOrCreateCart(
            @PathVariable Long userId) {

        return cartService.getOrCreateCart(userId);
    }

    @PostMapping("/items")
    public CartResponse addItem(
            @Valid @RequestBody CartItemRequest request) {

        return cartService.addItem(request);
    }

    @PutMapping("/items/{cartItemId}")
    public CartResponse updateItemQuantity(
            @PathVariable Long cartItemId,
            @RequestParam Integer quantity) {

        return cartService.updateItemQuantity(
                cartItemId,
                quantity
        );
    }

    @DeleteMapping("/items/{cartItemId}")
    public CartResponse removeItem(
            @PathVariable Long cartItemId) {

        return cartService.removeItem(cartItemId);
    }
}