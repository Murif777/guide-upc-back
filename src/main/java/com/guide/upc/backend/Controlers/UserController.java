package com.guide.upc.backend.Controlers;

import com.guide.upc.backend.Entities.User;
import com.guide.upc.backend.Exception.ResourceNotFoundException;
import com.guide.upc.backend.Repositories.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/usuarios")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @PostMapping("/usuarios")
    public User createUser(@RequestBody User user) {
        return userRepository.save(user);
    }

    @GetMapping("/usuarios/{id}")
    public User getUserById(@PathVariable Long id) {
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"+id));
    }

    @PutMapping("/usuarios/{id}")
    public User updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"+id));
        user.setEmail(userDetails.getEmail());
        user.setPassword(userDetails.getPassword());
        return userRepository.save(user);
    }

    @DeleteMapping("/usuarios/{id}")
    public void deleteUser(@PathVariable Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"+id));
        userRepository.delete(user);
        Map<String,Boolean> response = new HashMap<>();
        response.put( "Deleted",Boolean.TRUE) ;
    }
}
