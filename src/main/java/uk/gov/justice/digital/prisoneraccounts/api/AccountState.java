package uk.gov.justice.digital.prisoneraccounts.api;

import lombok.Builder;
import lombok.Data;
import uk.gov.justice.digital.prisoneraccounts.jpa.entity.Account;

@Data
@Builder
public class AccountState {
    private String accountName;
    private long amountPence;
    private Account.AccountStatuses accountStatus;
}
