package com.saraf.service.recipient;

import com.saraf.security.user.User;
import com.saraf.security.user.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecipientService {

    private final RecipientRepository recipientRepository;
    private final UserRepository userRepository;
    private final AuditorAware<Integer> auditorAware;

    public Recipient addRecipient(RecipientRequest request) {
        var user = getCurrentUser();

        Optional<Recipient> existingRecipient = Optional.ofNullable(recipientRepository.findByUserIdAndAndCcp(user.getId(), request.getCcp()));

        if (existingRecipient.isPresent()) {
            Recipient recipient = existingRecipient.get();
            recipient.setActive(true);
            return recipientRepository.save(recipient);
        }

        Recipient recipient = new Recipient();
        recipient.setFirstname(request.getFirstName());
        recipient.setLastname(request.getLastName());
        recipient.setCcp(request.getCcp());
        recipient.setPhoneNumber(request.getPhoneNumber());
        recipient.setDoContact(request.isDoContact());
        recipient.setUser(user);
        return recipientRepository.save(recipient);
    }

    public Recipient editRecipient(String ccp, @Valid EditRecipientRequest request) {
        var recipient = recipientRepository.findByCcp(ccp)
                .orElseThrow(() -> new IllegalArgumentException("Recipient with CCP " + ccp + " not found."));

        recipient.setFirstname(request.getFirstName());
        recipient.setLastname(request.getLastName());
        recipient.setPhoneNumber(request.getPhoneNumber());
        recipient.setDoContact(request.isDoContact());

        return recipientRepository.save(recipient);
    }

    private User getCurrentUser() {
        Integer userId = auditorAware.getCurrentAuditor()
                .orElseThrow(() -> new UsernameNotFoundException("User not authenticated"));
        return userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User with ID " + userId + " not found"));
    }

    public List<Recipient> getRecipientsForCurrentUser() {
        Integer userId = getCurrentUser().getId();
        return recipientRepository.findByUserId(userId).stream()
                .filter(Recipient::isActive)
                .collect(Collectors.toList());
    }

    public List<Recipient> getAllRecipients() {
        return recipientRepository.findAll();
    }

    public void deactivateRecipient(String ccp) {
        var recipient = recipientRepository.findByCcp(ccp)
                .orElseThrow(() -> new IllegalArgumentException("Recipient with CCP " + ccp + " not found."));
        recipient.setActive(false);
        recipientRepository.save(recipient);
    }

}
