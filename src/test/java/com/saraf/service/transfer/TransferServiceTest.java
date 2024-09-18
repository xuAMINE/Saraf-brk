package com.saraf.service.transfer;

import com.saraf.security.exception.TransferNotFoundException;
import com.saraf.security.exception.TransferNotPendingException;
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
import org.springframework.data.domain.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;


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
        int page = 0;
        int size = 10;
        TransferDTO transferDTO = new TransferDTO(1, BigDecimal.valueOf(120), BigDecimal.valueOf(108), Status.PENDING,
                LocalDateTime.now(), "Jane Doe", "receipt1", PaymentMethod.VENMO, "1111");

        Page<TransferDTO> pageOfTransfers = new PageImpl<>(List.of(transferDTO));

        when(transferRepository.findTransfersByUserId(1, PageRequest.of(page, size))).thenReturn(pageOfTransfers);

        Page<TransferDTO> transfers = transferService.getTransfersForUser(page, size);

        assertThat(transfers).isNotNull();
        assertThat(transfers.getTotalElements()).isEqualTo(1);
        assertThat(transfers.getContent().get(0)).isEqualTo(transferDTO);
    }

    @Test
    @WithMockUser(roles = "USER")
    void getNonCancelledTransfersForUser() {
        int page = 0;
        int size = 10;
        TransferDTO transferDTO = new TransferDTO(1, BigDecimal.valueOf(120), BigDecimal.valueOf(108), Status.PENDING,
                LocalDateTime.now(), "Jane Doe", "receipt1", PaymentMethod.VENMO, "1111");

        Page<TransferDTO> pageOfTransfers = new PageImpl<>(List.of(transferDTO));

        when(transferRepository.findAllNotCancelled(1, PageRequest.of(page, size))).thenReturn(pageOfTransfers);

        Page<TransferDTO> transfers = transferService.getNonCancelledTransfersForUser(page, size);

        assertThat(transfers).isNotNull();
        assertThat(transfers.getTotalElements()).isEqualTo(1);
        assertThat(transfers.getContent().get(0)).isEqualTo(transferDTO);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getTransfersForAdmin() {
        int page = 0;
        int size = 10;
        TransferAdminDTO transferDTO = new TransferAdminDTO(1, BigDecimal.valueOf(120), BigDecimal.valueOf(108), Status.PENDING,
                LocalDateTime.now(), "Jane Doe", "receipt1", PaymentMethod.ZELLE,
                "2222", "test1", "testLast");

        Page<TransferAdminDTO> pageOfTransfers = new PageImpl<>(List.of(transferDTO));

        when(transferRepository.findAllForAdmin(PageRequest.of(page, size))).thenReturn(pageOfTransfers);

        Page<TransferAdminDTO> transfers = transferService.getTransfersForAdmin(page, size);

        assertThat(transfers).isNotNull();
        assertThat(transfers.getTotalElements()).isEqualTo(1);
        assertThat(transfers.getContent().get(0)).isEqualTo(transferDTO);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getPendingTransfersForAdmin() {
        int page = 0;
        int size = 10;
        TransferAdminDTO transferDTO = new TransferAdminDTO(1, BigDecimal.valueOf(120), BigDecimal.valueOf(108), Status.PENDING,
                LocalDateTime.now(), "Jane Doe", "receipt1", PaymentMethod.ZELLE, "2222", "test1", "testLast");

        Page<TransferAdminDTO> pageOfTransfers = new PageImpl<>(List.of(transferDTO));

        when(transferRepository.findAllPendingForAdmin(PageRequest.of(page, size))).thenReturn(pageOfTransfers);

        Page<TransferAdminDTO> transfers = transferService.getPendingTransfersForAdmin(page, size);

        assertThat(transfers).isNotNull();
        assertThat(transfers.getTotalElements()).isEqualTo(1);
        assertThat(transfers.getContent().get(0)).isEqualTo(transferDTO);
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
    void cancelTransfer() {
        Transfer transfer = new Transfer();
        transfer.setId(1);
        transfer.setStatus(Status.PENDING); // Ensure the status is PENDING for cancellation

        Mockito.when(transferRepository.findById(1)).thenReturn(Optional.of(transfer));

        Mockito.when(transferRepository.save(transfer)).thenReturn(transfer);

        Transfer canceledTransfer = transferService.cancelTransfer(1);

        assertThat(canceledTransfer.getStatus()).isEqualTo(Status.CANCELED);
        verify(transferRepository).save(transfer);
    }

    @Test
    void cancelTransfer_NotPending() {
        Transfer transfer = new Transfer();
        transfer.setId(1);
        transfer.setStatus(Status.RECEIVED); // not PENDING

        Mockito.when(transferRepository.findById(1)).thenReturn(Optional.of(transfer));

        assertThrows(TransferNotPendingException.class, () -> transferService.cancelTransfer(1));
        verify(transferRepository, never()).save(any(Transfer.class));
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

    @Test
    void getUserPhoneNumberByTransferId() {
        String expectedPhoneNumber = "555-1234";

        Mockito.when(transferRepository.findUserPhoneNumberByTransferId(1)).thenReturn(expectedPhoneNumber);

        String phoneNumber = transferService.getUserPhoneNumberByTransferId(1);

        assertThat(phoneNumber).isEqualTo(expectedPhoneNumber);
    }

}