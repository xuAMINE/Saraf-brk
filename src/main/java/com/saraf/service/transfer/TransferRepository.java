package com.saraf.service.transfer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransferRepository extends JpaRepository<Transfer, Integer> {

    @Query("SELECT new com.saraf.service.transfer.TransferDTO(t.id, t.amount, t.amountReceived, t.status, t.transferDate, " +
            "CASE WHEN r IS NULL THEN 'One Time Transfer' ELSE CONCAT(r.firstname, ' ', r.lastname) END) " +
            "FROM Transfer t LEFT JOIN t.recipient r WHERE t.user.id = :userId ORDER BY t.transferDate DESC")
    List<TransferDTO> findTransfersByUserId(@Param("userId") Integer userId);


}
