package com.security.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class SettlementGenerateDTO {

    private UUID customerId;

    private String settlementMonth;
}
