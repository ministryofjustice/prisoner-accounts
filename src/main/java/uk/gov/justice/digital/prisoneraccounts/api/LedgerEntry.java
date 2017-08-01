package uk.gov.justice.digital.prisoneraccounts.api;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class LedgerEntry {

    private long amountPence;
    private Operations operation;
    private String clientRef;
    private String description;

}
