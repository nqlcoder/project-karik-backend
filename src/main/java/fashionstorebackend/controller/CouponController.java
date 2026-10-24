package fashionstorebackend.controller;

import fashionstorebackend.dto.coupon.CouponRequest;
import fashionstorebackend.dto.coupon.CouponResponse;
import fashionstorebackend.service.CouponService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @GetMapping
    public ResponseEntity<List<CouponResponse>> getAll() {
        return ResponseEntity.ok(couponService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CouponResponse> getById(
            @PathVariable Long id) {

        return ResponseEntity.ok(couponService.getById(id));
    }

    @PostMapping
    public ResponseEntity<CouponResponse> create(
            @Valid @RequestBody CouponRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(couponService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CouponResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody CouponRequest request) {

        return ResponseEntity.ok(
                couponService.update(id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id) {

        couponService.delete(id);

        return ResponseEntity.noContent().build();
    }
}