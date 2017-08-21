package uk.gov.justice.digital.prisoneraccounts.api;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TransferRequest {
    private String fromAccountName;
    private String toAccountName;
    private long amountPence;
}
