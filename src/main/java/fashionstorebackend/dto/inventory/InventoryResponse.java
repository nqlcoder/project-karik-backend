package fashionstorebackend.dto.inventory;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class InventoryResponse {

    private Long id;

    private Long productVariantId;

    private String sku;

    private Integer quantity;

    private Integer reservedQuantity;

    private Integer availableQuantity;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}