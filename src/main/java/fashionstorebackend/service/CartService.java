package fashionstorebackend.service;

import fashionstorebackend.dto.cart.CartItemRequest;
import fashionstorebackend.dto.cart.CartItemResponse;
import fashionstorebackend.dto.cart.CartResponse;
import fashionstorebackend.entity.Cart;
import fashionstorebackend.entity.CartItem;
import fashionstorebackend.entity.ProductVariant;
import fashionstorebackend.entity.User;
import fashionstorebackend.exception.CartNotFoundException;
import fashionstorebackend.repository.CartItemRepository;
import fashionstorebackend.repository.CartRepository;
import fashionstorebackend.repository.ProductVariantRepository;
import fashionstorebackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductVariantRepository productVariantRepository;
    private final UserRepository userRepository;

    @Transactional
    public CartResponse getOrCreateCart(Long userId) {

        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> {

                    User user = userRepository.findById(userId)
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "User not found with id: " + userId
                                    )
                            );

                    Cart newCart = Cart.builder()
                            .user(user)
                            .build();

                    return cartRepository.save(newCart);
                });

        return toResponse(cart);
    }

    @Transactional(readOnly = true)
    public CartResponse getCartByUserId(Long userId) {

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException(userId));

        return toResponse(cart);
    }

    @Transactional
    public CartResponse addItem(CartItemRequest request) {

        Cart cart = cartRepository.findByUserId(request.getUserId())
                .orElseGet(() -> {

                    User user = userRepository.findById(request.getUserId())
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "User not found with id: "
                                                    + request.getUserId()
                                    )
                            );

                    Cart newCart = Cart.builder()
                            .user(user)
                            .build();

                    return cartRepository.save(newCart);
                });

        ProductVariant variant = productVariantRepository.findById(
                request.getProductVariantId()
        ).orElseThrow(() ->
                new IllegalArgumentException(
                        "Product variant not found with id: "
                                + request.getProductVariantId()
                )
        );

        CartItem item = cartItemRepository
                .findByCartIdAndProductVariantId(
                        cart.getId(),
                        request.getProductVariantId()
                )
                .orElse(null);

        if (item != null) {

            item.setQuantity(
                    item.getQuantity() + request.getQuantity()
            );

        } else {

            item = CartItem.builder()
                    .cart(cart)
                    .productVariant(variant)
                    .quantity(request.getQuantity())
                    .build();
        }

        cartItemRepository.save(item);

        return toResponse(cart);
    }

    @Transactional
    public CartResponse updateItemQuantity(
            Long cartItemId,
            Integer quantity
    ) {

        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Cart item not found with id: "
                                        + cartItemId
                        )
                );

        if (quantity < 1) {
            throw new IllegalArgumentException(
                    "Quantity must be at least 1"
            );
        }

        item.setQuantity(quantity);

        cartItemRepository.save(item);

        return toResponse(item.getCart());
    }

    @Transactional
    public CartResponse removeItem(Long cartItemId) {

        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Cart item not found with id: "
                                        + cartItemId
                        )
                );

        Cart cart = item.getCart();

        cartItemRepository.delete(item);

        return toResponse(cart);
    }

    private CartResponse toResponse(Cart cart) {

        List<CartItem> items =
                cartItemRepository.findByCartId(cart.getId());

        List<CartItemResponse> itemResponses = items.stream()
                .map(this::toItemResponse)
                .toList();

        BigDecimal totalAmount = itemResponses.stream()
                .map(CartItemResponse::getSubtotal)
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );

        int totalItems = itemResponses.stream()
                .mapToInt(CartItemResponse::getQuantity)
                .sum();

        return CartResponse.builder()
                .id(cart.getId())
                .userId(cart.getUser().getId())
                .items(itemResponses)
                .totalItems(totalItems)
                .totalAmount(totalAmount)
                .build();
    }

    private CartItemResponse toItemResponse(CartItem item) {

        ProductVariant variant = item.getProductVariant();

        BigDecimal subtotal =
                variant.getPrice()
                        .multiply(
                                BigDecimal.valueOf(
                                        item.getQuantity()
                                )
                        );

        return CartItemResponse.builder()
                .id(item.getId())
                .productVariantId(variant.getId())
                .sku(variant.getSku())
                .productName(variant.getProduct().getName())
                .sizeName(variant.getSize().getName())
                .colorName(variant.getColor().getName())
                .colorHexCode(variant.getColor().getHexCode())
                .price(variant.getPrice())
                .quantity(item.getQuantity())
                .subtotal(subtotal)
                .build();
    }
}