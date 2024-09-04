package com.saraf.service.transfer;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransferRepository extends JpaRepository<Transfer, Integer> {

    @Query("SELECT new com.saraf.service.transfer.TransferDTO(t.id, t.amount, t.amountReceived, t.status, t.transferDate, " +
            "CASE WHEN r IS NULL THEN 'One Time Transfer' ELSE CONCAT(r.firstname, ' ', r.lastname) END, t.receipt) " +
            "FROM Transfer t LEFT JOIN t.recipient r WHERE t.user.id = :userId ORDER BY t.transferDate DESC")
    Page<TransferDTO> findTransfersByUserId(@Param("userId") Integer userId, Pageable pageable);

    @Query("SELECT new com.saraf.service.transfer.TransferDTO(t.id, t.amount, t.amountReceived, t.status, t.transferDate, " +
            "CASE WHEN r IS NULL THEN 'One Time Transfer' ELSE CONCAT(r.firstname, ' ', r.lastname) END, t.receipt) " +
            "FROM Transfer t LEFT JOIN t.recipient r ORDER BY t.transferDate DESC")
    Page<TransferDTO> findAllForAdmin(Pageable pageable);

    @Query("SELECT new com.saraf.service.transfer.TransferDTO(t.id, t.amount, t.amountReceived, t.status, t.transferDate, " +
            "CASE WHEN r IS NULL THEN 'One Time Transfer' ELSE CONCAT(r.firstname, ' ', r.lastname) END, t.receipt) " +
            "FROM Transfer t LEFT JOIN t.recipient r WHERE t.status NOT IN (com.saraf.service.transfer.Status.RECEIVED, com.saraf.service.transfer.Status.CANCELED) ORDER BY t.transferDate DESC")
    Page<TransferDTO> findAllPendingForAdmin(Pageable pageable);

}
