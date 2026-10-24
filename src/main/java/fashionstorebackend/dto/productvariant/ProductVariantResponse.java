package fashionstorebackend.dto.productvariant;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class ProductVariantResponse {

    private Long id;

    private Long productId;
    private String productName;

    private Long sizeId;
    private String sizeName;

    private Long colorId;
    private String colorName;
    private String colorHexCode;

    private String sku;

    private BigDecimal price;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}