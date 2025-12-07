package in.aman.tasks.service;

import in.aman.tasks.repository.UserRepository;
import in.aman.tasks.usermodel.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class CustomerServiceImplementation implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        User user = userRepository.findByEmail(username);

        if(user == null) {
            throw new UsernameNotFoundException(
                    "User not found with this email " + username);
        }

        //  THIS IS THE REQUIRED FIX
        List<GrantedAuthority> authorities =
                Collections.singletonList(
                        new SimpleGrantedAuthority(user.getRole())
                );

        // DEBUG LOG
        System.out.println("Loaded user: "
                + user.getEmail()
                + ", Role: "
                + user.getRole());

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                authorities
        );
    }
}
