package fashionstorebackend.dto.cart;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class CartItemResponse {

    private Long id;

    private Long productVariantId;

    private String sku;

    private String productName;

    private String sizeName;

    private String colorName;

    private String colorHexCode;

    private BigDecimal price;

    private Integer quantity;

    private BigDecimal subtotal;
}