package uk.gov.justice.digital.prisoneraccounts.api;

import lombok.Builder;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
@Builder
public class TransactionDetail {

    private Long transactionId;
    private String description;
    private String clientReference;
    private TransactionTypes transactionType;
    private Long amountPence;
    private ZonedDateTime transactionDateTime;

    public enum TransactionTypes {CREDIT, DEBIT}

}
