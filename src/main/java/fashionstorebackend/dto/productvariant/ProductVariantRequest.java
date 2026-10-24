package fashionstorebackend.dto.productvariant;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProductVariantRequest {

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotNull(message = "Size ID is required")
    private Long sizeId;

    @NotNull(message = "Color ID is required")
    private Long colorId;

    @NotBlank(message = "SKU is required")
    @Size(max = 100, message = "SKU must not exceed 100 characters")
    private String sku;

    @NotNull(message = "Price is required")
    @DecimalMin(
            value = "0.0",
            inclusive = true,
            message = "Price must be greater than or equal to 0"
    )
    private BigDecimal price;
}