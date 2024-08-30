package com.saraf.security.oauth2;

import com.saraf.security.auth.AuthenticationService;
import com.saraf.security.config.JwtService;
import com.saraf.security.token.Token;
import com.saraf.security.token.TokenRepository;
import com.saraf.security.token.TokenType;
import com.saraf.security.user.Role;
import com.saraf.security.user.User;
import com.saraf.security.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;


    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String email = oAuth2User.getAttribute("email");

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> registerNewUser(oAuth2User, registrationId));

        String jwtToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        revokeAllUserTokens(user);
        saveUserToken(user, jwtToken);

        return oAuth2User;
    }

    private User registerNewUser(OAuth2User oAuth2User, String registrationId) {
        String firstName = null;
        String lastName = null;
        String email = oAuth2User.getAttribute("email");

        if ("google".equals(registrationId)) {
            firstName = oAuth2User.getAttribute("given_name");
            lastName = oAuth2User.getAttribute("family_name");
        } else if ("facebook".equals(registrationId)) {
            // Facebook's user profile attributes
            String fullName = oAuth2User.getAttribute("name");
            if (fullName != null && fullName.contains(" ")) {
                int index = fullName.indexOf(' ');
                firstName = fullName.substring(0, index);
                lastName = fullName.substring(index + 1);
            } else {
                firstName = fullName != null ? fullName : "";
                lastName = "";
            }
        } else {
            // Handle other providers or default case
            firstName = oAuth2User.getAttribute("given_name");
            lastName = oAuth2User.getAttribute("family_name");
        }

        // Default values if any information is missing
        if (firstName == null) firstName = "OAuth2User";
        if (lastName == null) lastName = "";
        if (email == null) email = firstName + "@oauth2.com"; // Default email format

        User user = User.builder()
                .firstname(firstName)
                .lastname(lastName)
                .email(email)
                .password(passwordEncoder.encode("oauth2user")) // Placeholder password
                .enabled(true)
                .role(Role.USER)
                .build();

        return userRepository.save(user);
    }


    private void saveUserToken(User user, String jwtToken) {
        // Implement the logic to save the token to your database
        var token = Token.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);
    }

    private void revokeAllUserTokens(User user) {
        var validUserTokens = tokenRepository.findAllValidTokenByUser(user.getId());
        if (validUserTokens.isEmpty())
            return;
        validUserTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        tokenRepository.saveAll(validUserTokens);
    }

}
