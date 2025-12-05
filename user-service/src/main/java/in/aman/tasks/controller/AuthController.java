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
    private UserService userService;

    @Autowired
    private JwtProvider jwtProvider;

    // SIGNUP
    @HystrixCommand(fallbackMethod = "createUserFallback")
    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> createUserHandler(@RequestBody User user) throws UserException {

        String email = user.getEmail();

        if (userRepository.findByEmail(email) != null) {
            throw new UserException("Email already exists");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userRepository.save(user);

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        savedUser.getEmail(),
                        savedUser.getPassword()
                );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = jwtProvider.generateToken(authentication);

        AuthResponse authResponse = new AuthResponse();
        authResponse.setJwt(token);
        authResponse.setMessage("Register Success");
        authResponse.setStatus(true);

        return new ResponseEntity<>(authResponse, HttpStatus.OK);
    }

    public ResponseEntity<AuthResponse> createUserFallback(User user, Throwable throwable) {
        AuthResponse authResponse = new AuthResponse();
        authResponse.setMessage("User registration failed due to a temporary issue.");
        authResponse.setStatus(false);
        return new ResponseEntity<>(authResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // SIGNIN
    @HystrixCommand(fallbackMethod = "signinFallback")
    @PostMapping("/signin")
    public ResponseEntity<AuthResponse> signin(@RequestBody LoginRequest loginRequest) {

        String username = loginRequest.getemail();
        String password = loginRequest.getPassword();

        Authentication authentication = authenticate(username, password);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = jwtProvider.generateToken(authentication);

        AuthResponse authResponse = new AuthResponse();
        authResponse.setMessage("Login success");
        authResponse.setJwt(token);
        authResponse.setStatus(true);

        return new ResponseEntity<>(authResponse, HttpStatus.OK);
    }

    public ResponseEntity<AuthResponse> signinFallback(LoginRequest loginRequest, Throwable throwable) {
        AuthResponse authResponse = new AuthResponse();
        authResponse.setMessage("Login failed due to a temporary issue.");
        authResponse.setStatus(false);
        return new ResponseEntity<>(authResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private Authentication authenticate(String username, String password) {

        UserDetails userDetails =
                customUserDetails.loadUserByUsername(username);

        if (userDetails == null) {
            throw new BadCredentialsException("Invalid username");
        }

        if (!passwordEncoder.matches(password, userDetails.getPassword())) {
            throw new BadCredentialsException("Invalid password");
        }

        return new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
    }
}
