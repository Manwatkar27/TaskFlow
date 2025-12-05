package in.aman.tasks.service;

import in.aman.tasks.exception.UserException;
import in.aman.tasks.repository.UserRepository;
import in.aman.tasks.taskSecurityConfig.JwtProvider;
import in.aman.tasks.usermodel.User;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImplementation implements UserService {

    @Autowired
    private UserRepository userRepository;

    // JWT provider injected
    @Autowired
    private JwtProvider jwtProvider;


    @Override
    public User findUserProfileByJwt(String jwt) throws UserException {

        String email = jwtProvider.getEmailFromJwtToken(jwt);

        User user = userRepository.findByEmail(email);

        if (user == null) {
            throw new UserException("User not exist with email " + email);
        }

        return user;
    }

    @Override
    public User findUserByEmail(String email) throws UserException {
        return userRepository.findByEmail(email);
    }

    @Override
    public User findUserById(String userId) throws UserException {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserException("User not found with id " + userId));
    }

    @Override
    public List<User> getAllUser() throws UserException {
        return userRepository.findAll();
    }

    @Override
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }
}
