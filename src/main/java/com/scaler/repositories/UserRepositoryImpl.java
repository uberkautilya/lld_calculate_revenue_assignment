package com.scaler.repositories;

import com.scaler.models.User;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class UserRepositoryImpl implements UserRepository {
    List<User> users = new ArrayList<User>();
    @Override
    public Optional<User> findById(long id) {
        return users.stream().filter(user -> user.getId() == id).findFirst();
    }

    @Override
    public User save(User user) {
        if(users.stream().anyMatch(u -> u.getId() == user.getId())) {
            User existingUser = users.stream().filter(u -> u.getId() == user.getId()).findFirst().get();
            existingUser.setName(user.getName());
            existingUser.setPassword(user.getPassword());
            existingUser.setUserType(user.getUserType());
            existingUser.setPhone(user.getPhone());
        }
        return user;
    }
}
