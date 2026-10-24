package fashionstorebackend.service;

import fashionstorebackend.entity.User;
import fashionstorebackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User createUser(
            String email,
            String password,
            String fullName,
            String phone
    ) {

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException(
                    "Email already exists: " + email
            );
        }

        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .fullName(fullName)
                .phone(phone)
                .status("ACTIVE")
                .role("USER")
                .build();

        return userRepository.save(user);
    }
}