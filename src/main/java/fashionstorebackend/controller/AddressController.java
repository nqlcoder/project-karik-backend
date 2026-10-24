package fashionstorebackend.controller;

import fashionstorebackend.dto.address.AddressRequest;
import fashionstorebackend.dto.address.AddressResponse;
import fashionstorebackend.service.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @GetMapping("/user/{userId}")
    public List<AddressResponse> getAddressesByUserId(
            @PathVariable Long userId) {

        return addressService.getAddressesByUserId(userId);
    }

    @GetMapping("/{id}")
    public AddressResponse getAddressById(
            @PathVariable Long id) {

        return addressService.getAddressById(id);
    }

    @PostMapping
    public AddressResponse createAddress(
            @Valid @RequestBody AddressRequest request) {

        return addressService.createAddress(request);
    }

    @PutMapping("/{id}")
    public AddressResponse updateAddress(
            @PathVariable Long id,
            @Valid @RequestBody AddressRequest request) {

        return addressService.updateAddress(id, request);
    }

    @PutMapping("/{id}/default")
    public AddressResponse setDefaultAddress(
            @PathVariable Long id) {

        return addressService.setDefaultAddress(id);
    }

    @DeleteMapping("/{id}")
    public void deleteAddress(
            @PathVariable Long id) {

        addressService.deleteAddress(id);
    }
}