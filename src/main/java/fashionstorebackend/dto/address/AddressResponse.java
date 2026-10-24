package fashionstorebackend.dto.address;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AddressResponse {

    private Long id;

    private Long userId;

    private String recipientName;

    private String phone;

    private String province;

    private String district;

    private String ward;

    private String addressLine;

    private Boolean defaultAddress;
}