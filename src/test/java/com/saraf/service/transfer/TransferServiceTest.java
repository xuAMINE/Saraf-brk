package com.saraf.service.transfer;

import com.saraf.security.exception.TransferNotFoundException;
import com.saraf.security.user.Role;
import com.saraf.security.user.User;
import com.saraf.security.user.UserRepository;
import com.saraf.service.rate.ExchangeRate;
import com.saraf.service.rate.ExchangeRateRepository;
import com.saraf.service.recipient.Recipient;
import com.saraf.service.recipient.RecipientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class TransferServiceTest {

    @InjectMocks
    private TransferService transferService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RecipientRepository recipientRepository;

    @Mock
    private ExchangeRateRepository rateRepository;

    @Mock
    private TransferRepository transferRepository;

    @Mock
    private AuditorAware<Integer> auditorAware;


    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        // Create a UserDetails object with the user's details
        UserDetails userDetails = User.builder()
                .id(1) // Ensure the ID matches the one in your repository
                .email("user@test.com")
                .password("password")
                .role(Role.USER)
                .enabled(true)
                .build();

        // Create an Authentication object
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        // Set the authentication in the SecurityContext
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Mocking AuditorAware to return the user ID (if necessary)
        when(auditorAware.getCurrentAuditor()).thenReturn(Optional.of(1));

        // Setup mocks for other repositories
        User user = User.builder().id(1).email("user@test.com").enabled(true).build();
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));

        Recipient recipient = Recipient.builder().firstname("Jane").lastname("Doe").ccp("12345678989").user(user).build();
        when(recipientRepository.findByUserIdAndAndCcp(1, "12345678989")).thenReturn(recipient);

        ExchangeRate rate = new ExchangeRate();
        rate.setCurrentRate(0.9);
        when(rateRepository.findTopByOrderByIdDesc()).thenReturn(rate);
    }


    @Test
    void addTransfer() {
        TransferRequest request = TransferRequest.builder()
                .amount(BigDecimal.valueOf(120))
                .ccp("12345678989")
                .build();

        when(transferRepository.save(any(Transfer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Transfer transfer = transferService.addTransfer(request);

        assertThat(transfer.getAmountReceived()).isEqualTo(BigDecimal.valueOf(108.0));
        assertThat(transfer.getStatus()).isEqualTo(Status.PENDING);
        verify(transferRepository).save(any(Transfer.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getTransfersForUser() {
        TransferDTO transferDTO = new TransferDTO(1, BigDecimal.valueOf(120), BigDecimal.valueOf(108), Status.PENDING, LocalDate.now(), "Jane Doe", "receipt1");

        // Mock the repository to return a list with the expected TransferDTO
        when(transferRepository.findTransfersByUserId(1)).thenReturn(List.of(transferDTO));

        List<TransferDTO> transfers = transferService.getTransfersForUser();

        // Assert the returned list is not empty and contains the correct TransferDTO
        assertThat(transfers).isNotEmpty();
        assertThat(transfers.get(0)).isEqualTo(transferDTO);
    }

    @Test
    void updateStatus() {
        Transfer transfer = new Transfer();
        transfer.setId(1);
        transfer.setStatus(Status.PENDING);

        Mockito.when(transferRepository.findById(1)).thenReturn(Optional.of(transfer));
        transferService.updateStatus(1, Status.RECEIVED);

        assertThat(transfer.getStatus()).isEqualTo(Status.RECEIVED);
    }

    @Test
    void updateStatus_TransferNotFound() {
        Mockito.when(transferRepository.findById(1)).thenReturn(Optional.empty());
        assertThrows(TransferNotFoundException.class, () -> transferService.updateStatus(1, Status.RECEIVED));
    }

    @Test
    void getReceiptName() {
        Transfer transfer = new Transfer();
        transfer.setId(1);
        transfer.setReceipt("receipt1");

        Mockito.when(transferRepository.findById(1)).thenReturn(Optional.of(transfer));
        String receiptName = transferService.getReceiptName(1);

        assertThat(receiptName).isEqualTo("receipt1");
    }

    @Test
    void getReceiptName_TransferNotFound() {
        Mockito.when(transferRepository.findById(1)).thenReturn(Optional.empty());
        assertThrows(TransferNotFoundException.class, () -> transferService.getReceiptName(1));
    }
}