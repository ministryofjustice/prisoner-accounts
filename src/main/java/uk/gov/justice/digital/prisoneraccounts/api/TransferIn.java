package uk.gov.justice.digital.prisoneraccounts.api;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class TransferIn {
    private Set<String> prisonerIds;
    private String fromEstablishmentId;
    private long amountToRequestPence;
}
