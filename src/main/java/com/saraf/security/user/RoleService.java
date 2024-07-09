package com.saraf.security.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RoleService {

    private final UserRepository userRepository;

    @Autowired
    public RoleService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void updateUserRole(Integer userId, Role newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with ID " + userId + " not found"));

        user.setRole(newRole);
        userRepository.save(user);
    }
}
