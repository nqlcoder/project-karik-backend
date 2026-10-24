package fashionstorebackend.dto.productimage;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ProductImageResponse {

    private Long id;

    private Long productId;

    private String imageUrl;

    private Boolean primary;

    private Integer sortOrder;

    private LocalDateTime createdAt;
}