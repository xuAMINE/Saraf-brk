package com.saraf.service.recipient;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecipientRepository extends JpaRepository<Recipient, Integer> {

    List<Recipient> findByUserId(Integer id);

    Recipient findByUserIdAndAndCcp(Integer id, String ccp);

    Optional<Recipient> findByCcp(String ccp);

}
