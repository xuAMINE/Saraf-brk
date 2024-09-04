package com.saraf.service.transfer;

import com.saraf.security.user.User;
import com.saraf.security.user.UserRepository;
import com.saraf.service.recipient.Recipient;
import com.saraf.service.recipient.RecipientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class TransferRepositoryTest {

    @Autowired
    private TransferRepository transferRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RecipientRepository recipientRepository;

    @BeforeEach
    void setup() {
        // Setup necessary entities for tests (e.g., Users, Recipients, Transfers)
        User user = User.builder().email("user@test.com").firstname("John").lastname("Doe").enabled(true).build();
        userRepository.save(user);

        Recipient recipient = Recipient.builder().firstname("Jane").lastname("Doe").ccp("123").user(user).build();
        recipientRepository.save(recipient);

        Transfer transfer = Transfer.builder()
                .amount(BigDecimal.valueOf(100))
                .amountReceived(BigDecimal.valueOf(90))
                .status(Status.PENDING)
                .transferDate(LocalDateTime.now())
                .user(user)
                .recipient(recipient)
                .receipt("receipt1")
                .build();
        transferRepository.save(transfer);
    }

    @Test
    void findTransfersByUserId() {
        Page<TransferDTO> transfers = transferRepository.findTransfersByUserId(1, PageRequest.of(0, 10));
        assertThat(transfers.getContent()).hasSize(1);
        assertThat(transfers.getContent().get(0).getRecipientFullName()).isEqualTo("Jane Doe");
    }

    @Test
    void findAllForAdmin() {
        Page<TransferDTO> transfers = transferRepository.findAllForAdmin(PageRequest.of(0, 10));
        assertThat(transfers.getContent()).hasSize(1);
    }

    @Test
    void findAllPendingForAdmin() {
        Page<TransferDTO> transfers = transferRepository.findAllPendingForAdmin(PageRequest.of(0, 10));
        assertThat(transfers.getContent()).hasSize(1);
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        public AuditorAware<String> auditorAware() {
            return () -> Optional.of("test-auditor");
        }
    }
}