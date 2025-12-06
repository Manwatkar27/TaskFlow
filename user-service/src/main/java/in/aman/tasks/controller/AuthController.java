package in.aman.tasks.controller;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import in.aman.tasks.exception.UserException;
import in.aman.tasks.repository.UserRepository;
import in.aman.tasks.request.LoginRequest;
import in.aman.tasks.response.AuthResponse;
import in.aman.tasks.service.CustomerServiceImplementation;
import in.aman.tasks.service.UserService;
import in.aman.tasks.taskSecurityConfig.JwtProvider;
import in.aman.tasks.usermodel.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CustomerServiceImplementation customUserDetails;

    @Autowired
    private JwtProvider jwtProvider;

    @HystrixCommand(fallbackMethod = "createUserFallback")
    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> createUserHandler(@RequestBody User user) throws UserException {

        if (userRepository.findByEmail(user.getEmail()) != null) {
            throw new UserException("Email already exists");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        userRepository.save(user);

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        user.getEmail(),
                        null,
                        new ArrayList<>());

        String token = jwtProvider.generateToken(authentication);

        AuthResponse authResponse = new AuthResponse();
        authResponse.setJwt(token);
        authResponse.setMessage("Register Success");
        authResponse.setStatus(true);

        return new ResponseEntity<>(authResponse, HttpStatus.OK);
    }

    public ResponseEntity<AuthResponse> createUserFallback(User user, Throwable t) {
        AuthResponse ar = new AuthResponse();
        ar.setMessage("Signup failed: " + t.getMessage());
        ar.setStatus(false);
        return new ResponseEntity<>(ar, HttpStatus.INTERNAL_SERVER_ERROR);
    }


    @HystrixCommand(fallbackMethod = "signinFallback")
    @PostMapping("/signin")
    public ResponseEntity<AuthResponse> signin(@RequestBody LoginRequest loginRequest) {

        Authentication authentication =
                authenticate(loginRequest.getemail(), loginRequest.getPassword());

        String token = jwtProvider.generateToken(authentication);

        AuthResponse ar = new AuthResponse();
        ar.setJwt(token);
        ar.setMessage("Login success");
        ar.setStatus(true);

        return new ResponseEntity<>(ar, HttpStatus.OK);
    }

    public ResponseEntity<AuthResponse> signinFallback(LoginRequest req, Throwable t) {
        AuthResponse ar = new AuthResponse();
        ar.setMessage("Signin failed: " + t.getMessage());
        ar.setStatus(false);
        return new ResponseEntity<>(ar, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private Authentication authenticate(String username, String password) {

        UserDetails userDetails =
                customUserDetails.loadUserByUsername(username);

        if (userDetails == null) {
            throw new BadCredentialsException("User not found");
        }

        if (!passwordEncoder.matches(password, userDetails.getPassword())) {
            throw new BadCredentialsException("Invalid password");
        }

        return new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities());
    }
}
