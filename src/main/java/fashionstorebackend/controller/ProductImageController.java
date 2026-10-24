package fashionstorebackend.controller;

import fashionstorebackend.dto.productimage.ProductImageRequest;
import fashionstorebackend.dto.productimage.ProductImageResponse;
import fashionstorebackend.service.ProductImageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product-images")
@RequiredArgsConstructor
public class ProductImageController {

    private final ProductImageService productImageService;

    @GetMapping("/product/{productId}")
    public List<ProductImageResponse> getImagesByProductId(
            @PathVariable Long productId) {

        return productImageService.getImagesByProductId(productId);
    }

    @GetMapping("/{id}")
    public ProductImageResponse getImageById(
            @PathVariable Long id) {

        return productImageService.getImageById(id);
    }

    @PostMapping
    public ProductImageResponse createImage(
            @Valid @RequestBody ProductImageRequest request) {

        return productImageService.createImage(request);
    }

    @PutMapping("/{id}")
    public ProductImageResponse updateImage(
            @PathVariable Long id,
            @Valid @RequestBody ProductImageRequest request) {

        return productImageService.updateImage(id, request);
    }

    @DeleteMapping("/{id}")
    public void deleteImage(@PathVariable Long id) {

        productImageService.deleteImage(id);
    }
}