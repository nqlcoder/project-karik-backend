package fashionstorebackend.service;

import fashionstorebackend.dto.productvariant.ProductVariantRequest;
import fashionstorebackend.dto.productvariant.ProductVariantResponse;
import fashionstorebackend.entity.Color;
import fashionstorebackend.entity.Product;
import fashionstorebackend.entity.ProductVariant;
import fashionstorebackend.entity.Size;
import fashionstorebackend.exception.CategoryNotFoundException;
import fashionstorebackend.exception.ProductNotFoundException;
import fashionstorebackend.exception.ProductVariantNotFoundException;
import fashionstorebackend.repository.ColorRepository;
import fashionstorebackend.repository.ProductRepository;
import fashionstorebackend.repository.ProductVariantRepository;
import fashionstorebackend.repository.SizeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductVariantService {

    private final ProductVariantRepository productVariantRepository;
    private final ProductRepository productRepository;
    private final SizeRepository sizeRepository;
    private final ColorRepository colorRepository;

    @Transactional(readOnly = true)
    public List<ProductVariantResponse> getVariantsByProductId(
            Long productId) {

        if (!productRepository.existsById(productId)) {
            throw new ProductNotFoundException(productId);
        }

        return productVariantRepository.findByProductId(productId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductVariantResponse getVariantById(Long id) {

        ProductVariant variant = productVariantRepository.findById(id)
                .orElseThrow(() ->
                        new ProductVariantNotFoundException(id)
                );

        return toResponse(variant);
    }

    @Transactional
    public ProductVariantResponse createVariant(
            ProductVariantRequest request) {

        Product product = productRepository.findById(
                request.getProductId()
        ).orElseThrow(() ->
                new ProductNotFoundException(request.getProductId())
        );

        Size size = sizeRepository.findById(
                request.getSizeId()
        ).orElseThrow(() ->
                new IllegalArgumentException(
                        "Size not found with id: " + request.getSizeId()
                )
        );

        Color color = colorRepository.findById(
                request.getColorId()
        ).orElseThrow(() ->
                new IllegalArgumentException(
                        "Color not found with id: " + request.getColorId()
                )
        );

        if (productVariantRepository.existsBySku(request.getSku())) {
            throw new IllegalArgumentException(
                    "SKU already exists: " + request.getSku()
            );
        }

        if (productVariantRepository
                .existsByProductIdAndSizeIdAndColorId(
                        request.getProductId(),
                        request.getSizeId(),
                        request.getColorId()
                )) {

            throw new IllegalArgumentException(
                    "This product variant already exists"
            );
        }

        ProductVariant variant = ProductVariant.builder()
                .product(product)
                .size(size)
                .color(color)
                .sku(request.getSku())
                .price(request.getPrice())
                .build();

        ProductVariant savedVariant =
                productVariantRepository.save(variant);

        return toResponse(savedVariant);
    }

    @Transactional
    public ProductVariantResponse updateVariant(
            Long id,
            ProductVariantRequest request) {

        ProductVariant variant =
                productVariantRepository.findById(id)
                        .orElseThrow(() ->
                                new ProductVariantNotFoundException(id)
                        );

        Product product = productRepository.findById(
                request.getProductId()
        ).orElseThrow(() ->
                new ProductNotFoundException(request.getProductId())
        );

        Size size = sizeRepository.findById(
                request.getSizeId()
        ).orElseThrow(() ->
                new IllegalArgumentException(
                        "Size not found with id: " + request.getSizeId()
                )
        );

        Color color = colorRepository.findById(
                request.getColorId()
        ).orElseThrow(() ->
                new IllegalArgumentException(
                        "Color not found with id: " + request.getColorId()
                )
        );

        if (productVariantRepository
                .findBySku(request.getSku())
                .filter(existing -> !existing.getId().equals(id))
                .isPresent()) {

            throw new IllegalArgumentException(
                    "SKU already exists: " + request.getSku()
            );
        }

        if (productVariantRepository
                .existsByProductIdAndSizeIdAndColorIdAndIdNot(
                        request.getProductId(),
                        request.getSizeId(),
                        request.getColorId(),
                        id
                )) {

            throw new IllegalArgumentException(
                    "This product variant already exists"
            );
        }

        variant.setProduct(product);
        variant.setSize(size);
        variant.setColor(color);
        variant.setSku(request.getSku());
        variant.setPrice(request.getPrice());

        ProductVariant updatedVariant =
                productVariantRepository.save(variant);

        return toResponse(updatedVariant);
    }

    @Transactional
    public void deleteVariant(Long id) {

        ProductVariant variant =
                productVariantRepository.findById(id)
                        .orElseThrow(() ->
                                new ProductVariantNotFoundException(id)
                        );

        productVariantRepository.delete(variant);
    }

    private ProductVariantResponse toResponse(
            ProductVariant variant) {

        return ProductVariantResponse.builder()
                .id(variant.getId())

                .productId(variant.getProduct().getId())
                .productName(variant.getProduct().getName())

                .sizeId(variant.getSize().getId())
                .sizeName(variant.getSize().getName())

                .colorId(variant.getColor().getId())
                .colorName(variant.getColor().getName())
                .colorHexCode(variant.getColor().getHexCode())

                .sku(variant.getSku())
                .price(variant.getPrice())

                .createdAt(variant.getCreatedAt())
                .updatedAt(variant.getUpdatedAt())

                .build();
    }
}