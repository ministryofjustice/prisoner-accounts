package uk.gov.justice.digital.prisoneraccounts.api;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class TransferOut {
    private Set<String> prisonerIds;
    private String toEstablishmentId;
    private long amountToTransferPence;
}
