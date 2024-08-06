package com.saraf.service.transfer;

import com.saraf.security.user.User;
import com.saraf.security.user.UserRepository;
import com.saraf.service.rate.ExchangeRate;
import com.saraf.service.rate.ExchangeRateRepository;
import com.saraf.service.rate.Rate;
import com.saraf.service.recipient.RecipientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransferService {

    private final TransferRepository transferRepository;
    private final UserRepository userRepository;
    private final RecipientRepository recipientRepository;
    private final ExchangeRateRepository rateRepository;

    

    public Transfer addTransfer(TransferRequest request) {

        var user = getCurrentUser();

        ExchangeRate rate = rateRepository.findTopByOrderByIdDesc();
        double DZDtoUSD = rate.getCurrentRate();

        Transfer transfer = new Transfer();
        transfer.setAmount(request.getAmount());
        transfer.setAmountReceived(request.getAmount().multiply(BigDecimal.valueOf(DZDtoUSD)));
        transfer.setStatus(Status.PROCESSING);
        transfer.setTransferDate(LocalDate.now());
        transfer.setUser(user);
        transfer.setRecipient(recipientRepository.findByCcp(request.getCcp()));

        return transferRepository.save(transfer);
    }

    public List<TransferDTO> getTransfersForUser() {
        Integer userId = getCurrentUser().getId();
        return transferRepository.findTransfersByUserId(userId);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(email));
    }
}
