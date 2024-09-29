package com.saraf.security.user;

import com.saraf.security.email.EmailService;
import com.saraf.security.email.EmailTemplateName;
import com.saraf.security.exception.PasswordResetException;
import com.saraf.security.user.forgot_password.ForgotPasswordRequest;
import com.saraf.security.user.forgot_password.PasswordResetToken;
import com.saraf.security.user.forgot_password.PasswordResetTokenRepository;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository repository;
    private final PasswordResetTokenRepository resetTokenRepository;
    private final EmailService emailService;

    public void changePassword(ChangePasswordRequest request, Principal connectedUser) {

        var user = (User) ((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new PasswordResetException("Incorrect password");
        }
        if (!request.getNewPassword().equals(request.getConfirmationPassword())) {
            throw new PasswordResetException("Passwords doesn't match");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        repository.save(user);
    }

     void updatePhoneNumber(Principal connectedUser, String phoneNumber) {
        var user = (User) ((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();

        if (!isValidPhoneNumber(phoneNumber)) {
            throw new IllegalArgumentException("Invalid phone number format");
        }

        user.setPhoneNumber(phoneNumber);
        repository.save(user);
    }

    public void forgotPassword (ForgotPasswordRequest request) throws MessagingException {
        var user = repository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalStateException("User not found"));

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpirationTime(LocalDateTime.now().plusMinutes(15));

        resetTokenRepository.save(resetToken);

        String resetLink = "https://sarafbrk.com/reset-password/?token=" + token;
        emailService.sendEmail(
                user.getEmail(),
                user.getFullName(),
                EmailTemplateName.RESET_PASSWORD,
                resetLink,
                token,
                "Forgot Password"
        );
    }

    public void resetPassword(String token, String newPassword, String confirmationPassword) {
        var resetToken = resetTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalStateException("Invalid request, Please make a new one."));

        if (resetToken.getExpirationTime().isBefore(LocalDateTime.now()))
            throw new IllegalStateException("Session is expired, Please make a new request.");
        if (!newPassword.equals(confirmationPassword))
            throw new IllegalStateException("Passwords do not match.");

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        repository.save(user);

        resetTokenRepository.delete(resetToken);
    }

    private boolean isValidPhoneNumber(String phoneNumber) {
        String phonePattern = "^\\+?[0-9]{10,15}$";
        return phoneNumber.matches(phonePattern);
    }

    public boolean hasPhoneNumber(Principal connectedUser) {
        var user = (User) ((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();
        String phoneNumber = user.getPhoneNumber();

        return phoneNumber != null && !phoneNumber.isEmpty();
    }

}
