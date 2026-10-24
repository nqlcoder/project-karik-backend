package fashionstorebackend.controller;

import fashionstorebackend.dto.productvariant.ProductVariantRequest;
import fashionstorebackend.dto.productvariant.ProductVariantResponse;
import fashionstorebackend.service.ProductVariantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product-variants")
@RequiredArgsConstructor
public class ProductVariantController {

    private final ProductVariantService productVariantService;

    @GetMapping("/product/{productId}")
    public List<ProductVariantResponse> getVariantsByProductId(
            @PathVariable Long productId) {

        return productVariantService
                .getVariantsByProductId(productId);
    }

    @GetMapping("/{id}")
    public ProductVariantResponse getVariantById(
            @PathVariable Long id) {

        return productVariantService.getVariantById(id);
    }

    @PostMapping
    public ProductVariantResponse createVariant(
            @Valid @RequestBody ProductVariantRequest request) {

        return productVariantService.createVariant(request);
    }

    @PutMapping("/{id}")
    public ProductVariantResponse updateVariant(
            @PathVariable Long id,
            @Valid @RequestBody ProductVariantRequest request) {

        return productVariantService.updateVariant(id, request);
    }

    @DeleteMapping("/{id}")
    public void deleteVariant(@PathVariable Long id) {

        productVariantService.deleteVariant(id);
    }
}