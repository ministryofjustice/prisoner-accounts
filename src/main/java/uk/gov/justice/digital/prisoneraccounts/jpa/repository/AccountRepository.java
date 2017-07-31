package uk.gov.justice.digital.prisoneraccounts.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.justice.digital.prisoneraccounts.jpa.entity.Account;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByEstablishmentIdAndPrisonerIdAndAccountNameAndAccountStatus(
            String estId, String prisId, String accName, Account.AccountStatuses accStatus);
}
