package uk.gov.justice.digital.prisoneraccounts.api;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class EstablishmentTransferSummary {
    private List<TransferIn> transferredIn;
    private List<TransferOut> transferredOut;
}
