package com.saraf.service.recipient;

import com.saraf.security.user.Role;
import com.saraf.security.user.User;
import com.saraf.security.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RecipientServiceTest {

    @Mock
    private RecipientRepository recipientRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RecipientService recipientService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testAddRecipient_New() {
        // Manually setting up the security context (if needed)
        UserDetails userDetails = User.builder()
                .email("test@example.com")
                .password("password")
                .role(Role.USER)
                .build();
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Arrange: Set up User, Request, and Mocks
        User user = new User();
        user.setId(1);

        RecipientRequest request = new RecipientRequest("John", "Doe", "123", "1234567890", true, user);

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(recipientRepository.findByUserIdAndAndCcp(1, "123")).thenReturn(null);

        // Mock the save method to return the recipient that was passed to it
        when(recipientRepository.save(any(Recipient.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act: Call the service method
        Recipient recipient = recipientService.addRecipient(request);

        // Assert: Ensure the recipient is not null and correctly saved
        assertNotNull(recipient);
        verify(recipientRepository).save(any(Recipient.class));
    }


    @Test
    public void testDeactivateRecipient() {
        Recipient recipient = Recipient.builder().ccp("123").isActive(true).build();

        when(recipientRepository.findByCcp("123")).thenReturn(Optional.of(recipient));

        recipientService.deactivateRecipient("123");

        assertFalse(recipient.isActive());
        verify(recipientRepository).save(recipient);
    }

    @Test
    public void testGetRecipientsForCurrentUser() {
        // Manually setting up the security context
        UserDetails userDetails = User.builder()
                .email("test@example.com")
                .password("password")
                .role(Role.USER)
                .build();
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Arrange
        User user = new User();
        user.setId(1);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(recipientRepository.findByUserId(1)).thenReturn(Collections.singletonList(Recipient.builder().ccp("123").isActive(true).build()));

        // Act
        var recipients = recipientService.getRecipientsForCurrentUser();

        // Assert
        assertEquals(1, recipients.size());
        assertTrue(recipients.get(0).isActive());
    }

    @Test
    public void testEditRecipient() {
        Recipient recipient = new Recipient();
        recipient.setId(1);
        recipient.setFirstname("Abdou");
        recipient.setLastname("Zirek");
        recipient.setCcp("12345678901");

        when(recipientRepository.findByCcp("12345678901")).thenReturn(Optional.of(recipient));

        // Mock the repository's save method to return the saved recipient
        when(recipientRepository.save(any(Recipient.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Create an EditRecipientRequest object with new details
        EditRecipientRequest request = new EditRecipientRequest("John", "Doe", "1234567890", true);

        // Act - Call the service method
        Recipient updatedRecipient = recipientService.editRecipient("12345678901", request);

        // Assert - Check if the recipient's firstname and lastname were updated
        assertNotNull(updatedRecipient, "Updated recipient should not be null");
        assertEquals("John", updatedRecipient.getFirstname());
        assertEquals("Doe", updatedRecipient.getLastname());

        // Verify that save was called with the updated recipient
        verify(recipientRepository).save(updatedRecipient);
    }
}
