package fashionstorebackend.service;

import fashionstorebackend.dto.address.AddressRequest;
import fashionstorebackend.dto.address.AddressResponse;
import fashionstorebackend.entity.Address;
import fashionstorebackend.entity.User;
import fashionstorebackend.repository.AddressRepository;
import fashionstorebackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<AddressResponse> getAddressesByUserId(Long userId) {

        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException(
                    "User not found with id: " + userId
            );
        }

        return addressRepository
                .findByUserIdOrderByDefaultAddressDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AddressResponse getAddressById(Long id) {

        Address address = addressRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Address not found with id: " + id
                        )
                );

        return toResponse(address);
    }

    @Transactional
    public AddressResponse createAddress(AddressRequest request) {

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found with id: "
                                        + request.getUserId()
                        )
                );

        boolean makeDefault =
                Boolean.TRUE.equals(request.getDefaultAddress());

        // Nếu đây là địa chỉ đầu tiên thì tự động default
        boolean hasAddress =
                !addressRepository
                        .findByUserId(request.getUserId())
                        .isEmpty();

        if (!hasAddress) {
            makeDefault = true;
        }

        // Nếu địa chỉ mới là default
        if (makeDefault) {
            removeDefaultAddress(request.getUserId());
        }

        Address address = Address.builder()
                .user(user)
                .recipientName(request.getRecipientName())
                .phone(request.getPhone())
                .province(request.getProvince())
                .district(request.getDistrict())
                .ward(request.getWard())
                .addressLine(request.getAddressLine())
                .defaultAddress(makeDefault)
                .build();

        return toResponse(addressRepository.save(address));
    }

    @Transactional
    public AddressResponse updateAddress(
            Long id,
            AddressRequest request
    ) {

        Address address = addressRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Address not found with id: " + id
                        )
                );

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found with id: "
                                        + request.getUserId()
                        )
                );

        boolean makeDefault =
                Boolean.TRUE.equals(request.getDefaultAddress());

        if (makeDefault) {
            removeDefaultAddress(request.getUserId());
        }

        address.setUser(user);
        address.setRecipientName(request.getRecipientName());
        address.setPhone(request.getPhone());
        address.setProvince(request.getProvince());
        address.setDistrict(request.getDistrict());
        address.setWard(request.getWard());
        address.setAddressLine(request.getAddressLine());
        address.setDefaultAddress(makeDefault);

        return toResponse(addressRepository.save(address));
    }

    @Transactional
    public void deleteAddress(Long id) {

        Address address = addressRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Address not found with id: " + id
                        )
                );

        Long userId = address.getUser().getId();
        boolean wasDefault =
                Boolean.TRUE.equals(address.getDefaultAddress());

        addressRepository.delete(address);

        // Nếu xóa default thì chọn một địa chỉ khác làm default
        if (wasDefault) {

            List<Address> remaining =
                    addressRepository
                            .findByUserIdOrderByDefaultAddressDesc(userId);

            if (!remaining.isEmpty()) {
                Address newDefault = remaining.get(0);
                newDefault.setDefaultAddress(true);
                addressRepository.save(newDefault);
            }
        }
    }

    @Transactional
    public AddressResponse setDefaultAddress(Long id) {

        Address address = addressRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Address not found with id: " + id
                        )
                );

        Long userId = address.getUser().getId();

        removeDefaultAddress(userId);

        address.setDefaultAddress(true);

        return toResponse(addressRepository.save(address));
    }

    private void removeDefaultAddress(Long userId) {

        List<Address> addresses =
                addressRepository.findByUserId(userId);

        for (Address address : addresses) {
            if (Boolean.TRUE.equals(address.getDefaultAddress())) {
                address.setDefaultAddress(false);
            }
        }

        addressRepository.saveAll(addresses);
    }

    private AddressResponse toResponse(Address address) {

        return AddressResponse.builder()
                .id(address.getId())
                .userId(address.getUser().getId())
                .recipientName(address.getRecipientName())
                .phone(address.getPhone())
                .province(address.getProvince())
                .district(address.getDistrict())
                .ward(address.getWard())
                .addressLine(address.getAddressLine())
                .defaultAddress(address.getDefaultAddress())
                .build();
    }
}