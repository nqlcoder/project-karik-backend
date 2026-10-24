package fashionstorebackend.controller;

import fashionstorebackend.dto.inventory.InventoryRequest;
import fashionstorebackend.dto.inventory.InventoryResponse;
import fashionstorebackend.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/variant/{variantId}")
    public InventoryResponse getByVariantId(
            @PathVariable Long variantId) {

        return inventoryService.getByVariantId(variantId);
    }

    @PostMapping
    public InventoryResponse createInventory(
            @Valid @RequestBody InventoryRequest request) {

        return inventoryService.createInventory(request);
    }

    @PutMapping("/variant/{variantId}")
    public InventoryResponse updateQuantity(
            @PathVariable Long variantId,
            @RequestParam Integer quantity) {

        return inventoryService.updateQuantity(
                variantId,
                quantity
        );
    }
}