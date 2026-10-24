package fashionstorebackend.repository;

import fashionstorebackend.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductVariantRepository
        extends JpaRepository<ProductVariant, Long> {

    List<ProductVariant> findByProductId(Long productId);

    Optional<ProductVariant> findBySku(String sku);

    boolean existsBySku(String sku);

    boolean existsByProductIdAndSizeIdAndColorId(
            Long productId,
            Long sizeId,
            Long colorId
    );

    boolean existsByProductIdAndSizeIdAndColorIdAndIdNot(
            Long productId,
            Long sizeId,
            Long colorId,
            Long id
    );
}