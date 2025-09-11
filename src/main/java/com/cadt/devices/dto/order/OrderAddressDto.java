package com.cadt.devices.dto.order;

import com.cadt.devices.model.order.AddressType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderAddressDto {

    private String id;

    private AddressType type;

    private String name;

    private String phone;

    private String line1;

    private String line2;

    private String city;

    private String state;

    private String country;

    private String pincode;
}
