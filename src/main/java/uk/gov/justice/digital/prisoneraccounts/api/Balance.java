package uk.gov.justice.digital.prisoneraccounts.api;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Balance {
    private String accountName;
    private long amountPence;
}
