package com.saraf.service.recipient;

import com.saraf.security.user.User;
import com.saraf.security.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RecipientService {

    private final RecipientRepository recipientRepository;
    private final UserRepository userRepository;

    @PreAuthorize("hasRole('USER')")
    public Recipient addRecipient(AddRecipientRequest request) {

        var user = getUser();

        Recipient recipient = new Recipient();
        recipient.setFirstname(request.getFirstName());
        recipient.setLastname(request.getLastName());
        recipient.setCcp(request.getCcp());
        recipient.setPhoneNumber(request.getPhoneNumber());
        recipient.setDoContact(request.isDoContact());
        recipient.setUser(user);
        return recipientRepository.save(recipient);
    }

    private User getUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(email));
    }
}
