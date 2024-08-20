package com.saraf.service.transfer;

import lombok.Data;

@Data
public class UpdateTransferStatusDTO {
    private Integer id;
    private Status status;
}
