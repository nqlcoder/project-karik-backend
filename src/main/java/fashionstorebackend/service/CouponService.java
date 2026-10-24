package fashionstorebackend.service;

import fashionstorebackend.dto.coupon.CouponRequest;
import fashionstorebackend.dto.coupon.CouponResponse;
import fashionstorebackend.entity.Coupon;
import fashionstorebackend.exception.CouponNotFoundException;
import fashionstorebackend.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;

    @Transactional(readOnly = true)
    public List<CouponResponse> getAll() {
        return couponRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CouponResponse getById(Long id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new CouponNotFoundException(id));

        return toResponse(coupon);
    }

    @Transactional
    public CouponResponse create(CouponRequest request) {

        String code = request.getCode().trim().toUpperCase();

        if (couponRepository.existsByCode(code)) {
            throw new IllegalArgumentException(
                    "Coupon code already exists: " + code
            );
        }

        validateRequest(request);

        Coupon coupon = Coupon.builder()
                .code(code)
                .discountType(request.getDiscountType().toUpperCase())
                .discountValue(request.getDiscountValue())
                .minOrderAmount(request.getMinOrderAmount())
                .maxDiscountAmount(request.getMaxDiscountAmount())
                .startAt(request.getStartAt())
                .endAt(request.getEndAt())
                .usageLimit(request.getUsageLimit())
                .usedCount(0)
                .status(request.getStatus().toUpperCase())
                .build();

        return toResponse(couponRepository.save(coupon));
    }

    @Transactional
    public CouponResponse update(Long id, CouponRequest request) {

        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new CouponNotFoundException(id));

        String code = request.getCode().trim().toUpperCase();

        couponRepository.findByCode(code)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException(
                            "Coupon code already exists: " + code
                    );
                });

        validateRequest(request);

        coupon.setCode(code);
        coupon.setDiscountType(request.getDiscountType().toUpperCase());
        coupon.setDiscountValue(request.getDiscountValue());
        coupon.setMinOrderAmount(request.getMinOrderAmount());
        coupon.setMaxDiscountAmount(request.getMaxDiscountAmount());
        coupon.setStartAt(request.getStartAt());
        coupon.setEndAt(request.getEndAt());
        coupon.setUsageLimit(request.getUsageLimit());
        coupon.setStatus(request.getStatus().toUpperCase());

        return toResponse(couponRepository.save(coupon));
    }

    @Transactional
    public void delete(Long id) {

        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new CouponNotFoundException(id));

        couponRepository.delete(coupon);
    }

    private void validateRequest(CouponRequest request) {

        String discountType = request.getDiscountType().toUpperCase();

        if (!discountType.equals("PERCENTAGE")
                && !discountType.equals("FIXED")) {

            throw new IllegalArgumentException(
                    "Discount type must be PERCENTAGE or FIXED"
            );
        }

        if (discountType.equals("PERCENTAGE")
                && request.getDiscountValue().compareTo(BigDecimal.valueOf(100)) > 0) {

            throw new IllegalArgumentException(
                    "Percentage discount cannot exceed 100"
            );
        }

        if (request.getEndAt().isBefore(request.getStartAt())) {
            throw new IllegalArgumentException(
                    "End time must be after start time"
            );
        }
    }

    private CouponResponse toResponse(Coupon coupon) {

        return CouponResponse.builder()
                .id(coupon.getId())
                .code(coupon.getCode())
                .discountType(coupon.getDiscountType())
                .discountValue(coupon.getDiscountValue())
                .minOrderAmount(coupon.getMinOrderAmount())
                .maxDiscountAmount(coupon.getMaxDiscountAmount())
                .startAt(coupon.getStartAt())
                .endAt(coupon.getEndAt())
                .usageLimit(coupon.getUsageLimit())
                .usedCount(coupon.getUsedCount())
                .status(coupon.getStatus())
                .createdAt(coupon.getCreatedAt())
                .updatedAt(coupon.getUpdatedAt())
                .build();
    }
}