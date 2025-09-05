package com.cadt.devices.dto.admin;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class SalesChartData {
    private LocalDate date;
    private BigDecimal revenue;
    private Long orders;
}
