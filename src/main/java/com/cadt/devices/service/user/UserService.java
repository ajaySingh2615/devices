package com.cadt.devices.service.user;

import com.cadt.devices.dto.user.UpdateProfileRequest;
import com.cadt.devices.model.user.User;
import com.cadt.devices.repo.user.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User get(String id) {
        return userRepository.findById(id).orElseThrow();
    }

    @Transactional
    public User update(String id, UpdateProfileRequest r) {
        var u = get(id);
        u.setName(r.getName());
        u.setPhone(r.getPhone());
        u.setAvatarUrl(r.getAvatarUrl());
        return userRepository.save(u);
    }


}
