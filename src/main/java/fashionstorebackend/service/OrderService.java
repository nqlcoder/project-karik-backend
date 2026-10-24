package fashionstorebackend.service;

import fashionstorebackend.dto.order.CheckoutRequest;
import fashionstorebackend.dto.order.OrderItemResponse;
import fashionstorebackend.dto.order.OrderResponse;
import fashionstorebackend.entity.*;
import fashionstorebackend.exception.OrderNotFoundException;
import fashionstorebackend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final InventoryRepository inventoryRepository;
    private final CouponRepository couponRepository;

    @Transactional
    public OrderResponse checkout(CheckoutRequest request) {

        // =========================
        // 1. Find user
        // =========================

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found with id: " + request.getUserId()
                        )
                );

        // =========================
        // 2. Find address
        // =========================

        Address address = addressRepository.findById(request.getAddressId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Address not found with id: " + request.getAddressId()
                        )
                );

        // Make sure address belongs to user
        if (!address.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException(
                    "Address does not belong to user"
            );
        }

        // =========================
        // 3. Find cart
        // =========================

        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Cart not found for user: " + user.getId()
                        )
                );

        // =========================
        // 4. Find cart items
        // =========================

        List<CartItem> cartItems =
                cartItemRepository.findByCartId(cart.getId());

        if (cartItems.isEmpty()) {
            throw new IllegalArgumentException("Cart is empty");
        }

        // =========================
        // 5. Calculate subtotal
        // =========================

        BigDecimal subtotal = BigDecimal.ZERO;

        for (CartItem cartItem : cartItems) {

            ProductVariant variant = cartItem.getProductVariant();

            BigDecimal itemSubtotal =
                    variant.getPrice()
                            .multiply(
                                    BigDecimal.valueOf(cartItem.getQuantity())
                            );

            subtotal = subtotal.add(itemSubtotal);
        }

        // =========================
        // 6. Validate coupon
        // =========================

        Coupon coupon = null;

        BigDecimal discountAmount = BigDecimal.ZERO;

        String couponCode = request.getCouponCode();

        if (couponCode != null && !couponCode.trim().isEmpty()) {

            String normalizedCode =
                    couponCode.trim().toUpperCase();

            coupon = couponRepository
                    .findByCodeForUpdate(normalizedCode)
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Coupon not found: " + normalizedCode
                            )
                    );

            validateCoupon(coupon, subtotal);

            discountAmount = calculateDiscount(
                    coupon,
                    subtotal
            );

            // Increase usage count
            coupon.setUsedCount(
                    coupon.getUsedCount() + 1
            );
        }

        // =========================
        // 7. Calculate total
        // =========================

        BigDecimal totalAmount =
                subtotal.subtract(discountAmount);

        if (totalAmount.compareTo(BigDecimal.ZERO) < 0) {
            totalAmount = BigDecimal.ZERO;
        }

        // =========================
        // 8. Create Order
        // =========================

        Order order = Order.builder()
                .user(user)
                .coupon(coupon)
                .recipientName(address.getRecipientName())
                .phone(address.getPhone())
                .province(address.getProvince())
                .district(address.getDistrict())
                .ward(address.getWard())
                .addressLine(address.getAddressLine())
                .totalAmount(totalAmount)
                .discountAmount(discountAmount)
                .status("PENDING")
                .paymentStatus("UNPAID")
                .build();

        order = orderRepository.save(order);

        // =========================
        // 9. Create Order Items
        // =========================

        for (CartItem cartItem : cartItems) {

            ProductVariant variant =
                    cartItem.getProductVariant();

            // =========================
            // Check inventory
            // =========================

            Inventory inventory =
                    inventoryRepository
                            .findByProductVariantId(
                                    variant.getId()
                            )
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Inventory not found for variant: "
                                                    + variant.getId()
                                    )
                            );

            int availableQuantity =
                    inventory.getQuantity()
                            - inventory.getReservedQuantity();

            if (availableQuantity < cartItem.getQuantity()) {

                throw new IllegalArgumentException(
                        "Insufficient inventory for SKU: "
                                + variant.getSku()
                );
            }

            // =========================
            // Snapshot product information
            // =========================

            BigDecimal unitPrice =
                    variant.getPrice();

            BigDecimal itemSubtotal =
                    unitPrice.multiply(
                            BigDecimal.valueOf(
                                    cartItem.getQuantity()
                            )
                    );

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .productVariant(variant)
                    .productName(
                            variant.getProduct().getName()
                    )
                    .sku(variant.getSku())
                    .sizeName(
                            variant.getSize().getName()
                    )
                    .colorName(
                            variant.getColor().getName()
                    )
                    .unitPrice(unitPrice)
                    .quantity(cartItem.getQuantity())
                    .subtotal(itemSubtotal)
                    .build();

            orderItemRepository.save(orderItem);

            // =========================
            // Reduce inventory
            // =========================

            inventory.setQuantity(
                    inventory.getQuantity()
                            - cartItem.getQuantity()
            );

            inventoryRepository.save(inventory);
        }

        // =========================
        // 10. Delete cart items
        // =========================

        cartItemRepository.deleteAll(cartItems);

        // =========================
        // 11. Return response
        // =========================

        return toResponse(
                order,
                subtotal,
                discountAmount,
                coupon
        );
    }

    // =========================================================
    // Coupon validation
    // =========================================================

    private void validateCoupon(
            Coupon coupon,
            BigDecimal subtotal
    ) {

        LocalDateTime now = LocalDateTime.now();

        // Status
        if (!"ACTIVE".equalsIgnoreCase(coupon.getStatus())) {
            throw new IllegalArgumentException(
                    "Coupon is not active"
            );
        }

        // Start time
        if (now.isBefore(coupon.getStartAt())) {
            throw new IllegalArgumentException(
                    "Coupon is not active yet"
            );
        }

        // End time
        if (now.isAfter(coupon.getEndAt())) {
            throw new IllegalArgumentException(
                    "Coupon has expired"
            );
        }

        // Usage limit
        if (coupon.getUsageLimit() != null
                && coupon.getUsedCount()
                >= coupon.getUsageLimit()) {

            throw new IllegalArgumentException(
                    "Coupon usage limit has been reached"
            );
        }

        // Minimum order amount
        if (subtotal.compareTo(
                coupon.getMinOrderAmount()
        ) < 0) {

            throw new IllegalArgumentException(
                    "Order does not meet minimum amount for coupon"
            );
        }
    }

    // =========================================================
    // Calculate discount
    // =========================================================

    private BigDecimal calculateDiscount(
            Coupon coupon,
            BigDecimal subtotal
    ) {

        BigDecimal discount;

        if ("PERCENTAGE".equalsIgnoreCase(
                coupon.getDiscountType()
        )) {

            discount = subtotal
                    .multiply(coupon.getDiscountValue())
                    .divide(
                            BigDecimal.valueOf(100),
                            2,
                            RoundingMode.HALF_UP
                    );

        } else if ("FIXED".equalsIgnoreCase(
                coupon.getDiscountType()
        )) {

            discount = coupon.getDiscountValue();

        } else {

            throw new IllegalArgumentException(
                    "Invalid coupon discount type"
            );
        }

        // Maximum discount
        if (coupon.getMaxDiscountAmount() != null
                && discount.compareTo(
                coupon.getMaxDiscountAmount()
        ) > 0) {

            discount = coupon.getMaxDiscountAmount();
        }

        // Discount cannot exceed subtotal
        if (discount.compareTo(subtotal) > 0) {
            discount = subtotal;
        }

        return discount;
    }

    // =========================================================
    // Get order
    // =========================================================

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() ->
                        new OrderNotFoundException(id)
                );

        return buildOrderResponse(order);
    }

    // =========================================================
    // Get user orders
    // =========================================================

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByUserId(Long userId) {

        return orderRepository
                .findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::buildOrderResponse)
                .toList();
    }

    // =========================================================
    // Response
    // =========================================================

    private OrderResponse buildOrderResponse(
            Order order
    ) {

        List<OrderItem> items =
                orderItemRepository.findByOrderId(
                        order.getId()
                );

        BigDecimal subtotal = items.stream()
                .map(OrderItem::getSubtotal)
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );

        return toResponse(
                order,
                subtotal,
                order.getDiscountAmount(),
                order.getCoupon()
        );
    }

    private OrderResponse toResponse(
            Order order,
            BigDecimal subtotal,
            BigDecimal discountAmount,
            Coupon coupon
    ) {

        List<OrderItemResponse> itemResponses =
                orderItemRepository
                        .findByOrderId(order.getId())
                        .stream()
                        .map(this::toItemResponse)
                        .toList();

        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUser().getId())
                .recipientName(order.getRecipientName())
                .phone(order.getPhone())
                .province(order.getProvince())
                .district(order.getDistrict())
                .ward(order.getWard())
                .addressLine(order.getAddressLine())
                .subtotal(subtotal)
                .discountAmount(discountAmount)
                .couponCode(
                        coupon != null
                                ? coupon.getCode()
                                : null
                )
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .paymentStatus(order.getPaymentStatus())
                .items(itemResponses)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    private OrderItemResponse toItemResponse(
            OrderItem item
    ) {

        return OrderItemResponse.builder()
                .id(item.getId())
                .productVariantId(
                        item.getProductVariant().getId()
                )
                .productName(item.getProductName())
                .sku(item.getSku())
                .sizeName(item.getSizeName())
                .colorName(item.getColorName())
                .unitPrice(item.getUnitPrice())
                .quantity(item.getQuantity())
                .subtotal(item.getSubtotal())
                .build();
    }
}