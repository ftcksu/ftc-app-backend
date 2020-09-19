package com.ftcksu.app;

import com.ftcksu.app.model.entity.User;
import com.ftcksu.app.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class CommandLineApplication implements ApplicationRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public CommandLineApplication(UserRepository userRepository,
                                  PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        List<String> usernames = args.getOptionValues("username");
        List<String> passwords = args.getOptionValues("password");

        if (usernames == null || passwords == null) {
            return;
        }

        for (int i = 0; i < Math.min(usernames.size(), passwords.size()); i++) {
            Integer userId = Integer.valueOf(usernames.get(i));
            String password = passwords.get(i);

            if (userRepository.existsById(userId)) {
                log.error("User " + userId + " already exists.");
                continue;
            }

            User userToAdd = new User(userId, password, "ROLE_ADMIN");
            userRepository.save(userToAdd);
            log.info("User " + userId + " added.");
        }
    }
}
