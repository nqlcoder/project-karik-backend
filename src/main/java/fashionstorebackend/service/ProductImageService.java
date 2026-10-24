package fashionstorebackend.service;

import fashionstorebackend.dto.productimage.ProductImageRequest;
import fashionstorebackend.dto.productimage.ProductImageResponse;
import fashionstorebackend.entity.Product;
import fashionstorebackend.entity.ProductImage;
import fashionstorebackend.exception.ProductImageNotFoundException;
import fashionstorebackend.exception.ProductNotFoundException;
import fashionstorebackend.repository.ProductImageRepository;
import fashionstorebackend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductImageService {

    private final ProductImageRepository productImageRepository;
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public List<ProductImageResponse> getImagesByProductId(Long productId) {

        if (!productRepository.existsById(productId)) {
            throw new ProductNotFoundException(productId);
        }

        return productImageRepository
                .findByProductIdOrderBySortOrderAsc(productId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductImageResponse getImageById(Long id) {

        ProductImage image = productImageRepository.findById(id)
                .orElseThrow(() ->
                        new ProductImageNotFoundException(id)
                );

        return toResponse(image);
    }

    @Transactional
    public ProductImageResponse createImage(ProductImageRequest request) {

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() ->
                        new ProductNotFoundException(request.getProductId())
                );

        ProductImage image = ProductImage.builder()
                .product(product)
                .imageUrl(request.getImageUrl())
                .primary(
                        request.getPrimary() != null
                                ? request.getPrimary()
                                : false
                )
                .sortOrder(
                        request.getSortOrder() != null
                                ? request.getSortOrder()
                                : 0
                )
                .build();

        ProductImage savedImage =
                productImageRepository.save(image);

        return toResponse(savedImage);
    }

    @Transactional
    public ProductImageResponse updateImage(
            Long id,
            ProductImageRequest request) {

        ProductImage image = productImageRepository.findById(id)
                .orElseThrow(() ->
                        new ProductImageNotFoundException(id)
                );

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() ->
                        new ProductNotFoundException(request.getProductId())
                );

        image.setProduct(product);
        image.setImageUrl(request.getImageUrl());
        image.setPrimary(
                request.getPrimary() != null
                        ? request.getPrimary()
                        : false
        );
        image.setSortOrder(
                request.getSortOrder() != null
                        ? request.getSortOrder()
                        : 0
        );

        ProductImage updatedImage =
                productImageRepository.save(image);

        return toResponse(updatedImage);
    }

    @Transactional
    public void deleteImage(Long id) {

        ProductImage image = productImageRepository.findById(id)
                .orElseThrow(() ->
                        new ProductImageNotFoundException(id)
                );

        productImageRepository.delete(image);
    }

    private ProductImageResponse toResponse(ProductImage image) {

        return ProductImageResponse.builder()
                .id(image.getId())
                .productId(image.getProduct().getId())
                .imageUrl(image.getImageUrl())
                .primary(image.getPrimary())
                .sortOrder(image.getSortOrder())
                .createdAt(image.getCreatedAt())
                .build();
    }
}