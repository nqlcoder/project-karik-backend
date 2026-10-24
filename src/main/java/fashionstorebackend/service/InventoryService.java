package fashionstorebackend.service;

import fashionstorebackend.dto.inventory.InventoryRequest;
import fashionstorebackend.dto.inventory.InventoryResponse;
import fashionstorebackend.entity.Inventory;
import fashionstorebackend.entity.ProductVariant;
import fashionstorebackend.exception.InventoryNotFoundException;
import fashionstorebackend.repository.InventoryRepository;
import fashionstorebackend.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProductVariantRepository productVariantRepository;

    @Transactional(readOnly = true)
    public InventoryResponse getByVariantId(Long variantId) {

        Inventory inventory = inventoryRepository
                .findByProductVariantId(variantId)
                .orElseThrow(() ->
                        new InventoryNotFoundException(variantId)
                );

        return toResponse(inventory);
    }

    @Transactional
    public InventoryResponse createInventory(
            InventoryRequest request) {

        ProductVariant variant = productVariantRepository
                .findById(request.getProductVariantId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Product variant not found with id: "
                                        + request.getProductVariantId()
                        )
                );

        if (inventoryRepository.existsByProductVariantId(
                request.getProductVariantId())) {

            throw new IllegalArgumentException(
                    "Inventory already exists for product variant: "
                            + request.getProductVariantId()
            );
        }

        Inventory inventory = Inventory.builder()
                .productVariant(variant)
                .quantity(request.getQuantity())
                .reservedQuantity(0)
                .build();

        Inventory savedInventory =
                inventoryRepository.save(inventory);

        return toResponse(savedInventory);
    }

    @Transactional
    public InventoryResponse updateQuantity(
            Long variantId,
            Integer quantity) {

        Inventory inventory = inventoryRepository
                .findByProductVariantId(variantId)
                .orElseThrow(() ->
                        new InventoryNotFoundException(variantId)
                );

        if (quantity < inventory.getReservedQuantity()) {
            throw new IllegalArgumentException(
                    "Quantity cannot be lower than reserved quantity"
            );
        }

        inventory.setQuantity(quantity);

        return toResponse(
                inventoryRepository.save(inventory)
        );
    }

    private InventoryResponse toResponse(
            Inventory inventory) {

        int availableQuantity =
                inventory.getQuantity()
                        - inventory.getReservedQuantity();

        return InventoryResponse.builder()
                .id(inventory.getId())
                .productVariantId(
                        inventory.getProductVariant().getId()
                )
                .sku(
                        inventory.getProductVariant().getSku()
                )
                .quantity(inventory.getQuantity())
                .reservedQuantity(
                        inventory.getReservedQuantity()
                )
                .availableQuantity(availableQuantity)
                .createdAt(inventory.getCreatedAt())
                .updatedAt(inventory.getUpdatedAt())
                .build();
    }
}