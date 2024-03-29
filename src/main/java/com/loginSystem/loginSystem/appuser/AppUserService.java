package com.loginSystem.loginSystem.appuser;

import java.time.LocalDateTime;
import java.util.UUID;

import com.loginSystem.loginSystem.registration.token.ConfirmationToken;
import com.loginSystem.loginSystem.registration.token.ConfirmationTokenService;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class AppUserService implements UserDetailsService {

    private final static String USER_NOT_FOUND_MSG =
        "user with email %s not found";

    private final AppUserRepository appUserRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final ConfirmationTokenService confirmationTokenService;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return appUserRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException(String.format(USER_NOT_FOUND_MSG, email)));
    }

    // return confirm link
    public String signUpUser(AppUser appUser) {
        boolean userExists = appUserRepository
            .findByEmail(appUser.getEmail())
            .isPresent();

        if (userExists) {
            if (appUser.getPassword().equals(appUserRepository.findByEmail(appUser.getEmail()).get().getPassword())) {
                throw new IllegalArgumentException("atributes are the same");
            }

            if (appUserRepository.findByEmail(appUser.getEmail()).get().isEnabled()) {
                throw new IllegalArgumentException("email already confirmed");
            }

            throw new IllegalStateException("email already taken");
        }

        String encodePassword = bCryptPasswordEncoder.encode(appUser.getPassword());
        appUser.setPassword(encodePassword);
        appUserRepository.save(appUser);

        String token = UUID.randomUUID().toString();
        ConfirmationToken confirmationToken = new ConfirmationToken(
            token,
            LocalDateTime.now(),
            LocalDateTime.now().plusMinutes(15),
            appUser
        );
        confirmationTokenService.saveConfirmationToken(confirmationToken);

        return token;
    }

    public int enableAppUser(String email) {
        return appUserRepository.enableAppUser(email);
    }
}
