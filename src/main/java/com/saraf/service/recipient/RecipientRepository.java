package com.saraf.service.recipient;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecipientRepository extends JpaRepository<Recipient, Integer> {

    List<Recipient> findByUserId(Integer id);

    Recipient findByCcp(String ccp);

}
