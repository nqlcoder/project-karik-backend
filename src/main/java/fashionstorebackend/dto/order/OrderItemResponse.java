package fashionstorebackend.dto.order;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class OrderItemResponse {

    private Long id;

    private Long productVariantId;

    private String productName;

    private String sku;

    private String sizeName;

    private String colorName;

    private BigDecimal unitPrice;

    private Integer quantity;

    private BigDecimal subtotal;
}