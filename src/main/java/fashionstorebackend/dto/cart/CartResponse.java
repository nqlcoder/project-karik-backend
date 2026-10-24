package fashionstorebackend.dto.cart;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class CartResponse {

    private Long id;

    private Long userId;

    private List<CartItemResponse> items;

    private Integer totalItems;

    private BigDecimal totalAmount;
}