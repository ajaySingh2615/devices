package com.cadt.devices.service.user;

import com.cadt.devices.dto.user.AddressDto;
import com.cadt.devices.dto.user.CreateAddressRequest;
import com.cadt.devices.dto.user.UpdateAddressRequest;
import com.cadt.devices.exception.ApiException;
import com.cadt.devices.model.user.Address;
import com.cadt.devices.model.user.User;
import com.cadt.devices.repo.user.AddressRepository;
import com.cadt.devices.repo.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressService {
    private final AddressRepository addresses;
    private final UserRepository users;

    @Transactional(readOnly = true)
    public List<AddressDto> list(String userId) {
        return addresses.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId).stream().map(this::toDto).toList();
    }

    @Transactional
    public AddressDto create(String userId, CreateAddressRequest r) {
        User u = users.findById(userId).orElseThrow(() -> new ApiException("USER_NOT_FOUND", "User not found"));
        Address a = Address.builder()
                .user(u)
                .name(r.name())
                .phone(r.phone())
                .line1(r.line1())
                .line2(r.line2())
                .city(r.city())
                .state(r.state())
                .country(r.country())
                .pincode(r.pincode())
                .isDefault(Boolean.TRUE.equals(r.isDefault()))
                .build();
        if (a.isDefault()) {
            unsetDefault(userId);
        } else if (addresses.countByUserId(userId) == 0) {
            a.setDefault(true);
        }
        return toDto(addresses.save(a));
    }

    @Transactional
    public AddressDto update(String userId, String id, UpdateAddressRequest r) {
        Address a = getOwned(userId, id);
        a.setName(r.name());
        a.setPhone(r.phone());
        a.setLine1(r.line1());
        a.setLine2(r.line2());
        a.setCity(r.city());
        a.setState(r.state());
        a.setCountry(r.country());
        a.setPincode(r.pincode());
        boolean newDefault = Boolean.TRUE.equals(r.isDefault());
        if (newDefault && !a.isDefault()) {
            unsetDefault(userId);
            a.setDefault(true);
        }
        return toDto(addresses.save(a));
    }

    @Transactional
    public void delete(String userId, String id) {
        Address a = getOwned(userId, id);
        addresses.delete(a);
    }

    @Transactional
    public AddressDto setDefault(String userId, String id) {
        Address a = getOwned(userId, id);
        unsetDefault(userId);
        a.setDefault(true);
        return toDto(addresses.save(a));
    }

    @Transactional(readOnly = true)
    public AddressDto getForUser(String userId, String id) {
        Address a = getOwned(userId, id);
        return toDto(a);
    }

    private void unsetDefault(String userId) {
        addresses.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId).stream()
                .filter(Address::isDefault)
                .forEach(addr -> { addr.setDefault(false); addresses.save(addr); });
    }

    private Address getOwned(String userId, String id) {
        Address a = addresses.findById(id).orElseThrow(() -> new ApiException("ADDRESS_NOT_FOUND", "Address not found"));
        if (!a.getUser().getId().equals(userId)) throw new ApiException("FORBIDDEN", "Not your address");
        return a;
    }

    private AddressDto toDto(Address a) {
        return new AddressDto(a.getId(), a.getName(), a.getPhone(), a.getLine1(), a.getLine2(), a.getCity(), a.getState(), a.getCountry(), a.getPincode(), a.isDefault(), a.getCreatedAt());
    }
}


