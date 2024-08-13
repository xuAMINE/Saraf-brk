package com.saraf.service.recipient;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RecipientRepository extends JpaRepository<Recipient, Integer> {

    List<Recipient> findByUserId(Integer id);

    Recipient findByUserIdAndAndCcp(Integer id, String ccp);

    Optional<Recipient> findByCcp(String ccp);

}
