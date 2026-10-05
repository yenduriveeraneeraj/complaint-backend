package com.example.complaintmanagement.service;

import com.example.complaintmanagement.entity.User;
import com.example.complaintmanagement.exception.*;
import com.example.complaintmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<User> searchUsers(String keyword) {
        return userRepository.searchUsers(keyword);
    }

    public User toggleUserStatus(Long id) {
        User user = getUserById(id);
        user.setIsActive(!user.getIsActive());
        return userRepository.save(user);
    }
}
