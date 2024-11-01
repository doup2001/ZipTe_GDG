package org.gdg.zipte_gdg.api.service.delivery.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DeliveryResponseDto {

    private Long id;
    private String city;
    private String streetAddress;
    private int zipCode;
    private String orderDesc;
    private String deliveryDesc;
    private String status;
}
