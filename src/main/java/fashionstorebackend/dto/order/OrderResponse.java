package fashionstorebackend.dto.order;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class OrderResponse {

    private Long id;

    private Long userId;

    private String recipientName;

    private String phone;

    private String province;

    private String district;

    private String ward;

    private String addressLine;

    private BigDecimal totalAmount;

    private String status;

    private String paymentStatus;

    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private String couponCode;

    private List<OrderItemResponse> items;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}