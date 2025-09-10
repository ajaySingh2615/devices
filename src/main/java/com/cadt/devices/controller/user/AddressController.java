package com.cadt.devices.controller.user;

import com.cadt.devices.dto.user.AddressDto;
import com.cadt.devices.dto.user.CreateAddressRequest;
import com.cadt.devices.dto.user.UpdateAddressRequest;
import com.cadt.devices.service.user.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addresses;

    @GetMapping
    public ResponseEntity<List<AddressDto>> list(Authentication auth) {
        return ResponseEntity.ok(addresses.list(auth.getName()));
    }

    @PostMapping
    public ResponseEntity<AddressDto> create(Authentication auth, @Valid @RequestBody CreateAddressRequest r) {
        return ResponseEntity.ok(addresses.create(auth.getName(), r));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<AddressDto> update(Authentication auth, @PathVariable String id, @Valid @RequestBody UpdateAddressRequest r) {
        return ResponseEntity.ok(addresses.update(auth.getName(), id, r));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(Authentication auth, @PathVariable String id) {
        addresses.delete(auth.getName(), id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/default")
    public ResponseEntity<AddressDto> makeDefault(Authentication auth, @PathVariable String id) {
        return ResponseEntity.ok(addresses.setDefault(auth.getName(), id));
    }
}


