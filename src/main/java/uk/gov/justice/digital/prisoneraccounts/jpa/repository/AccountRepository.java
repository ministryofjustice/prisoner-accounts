package uk.gov.justice.digital.prisoneraccounts.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.justice.digital.prisoneraccounts.jpa.entity.Account;
import uk.gov.justice.digital.prisoneraccounts.jpa.entity.PrisonerTransfer;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByEstablishmentIdAndPrisonerIdAndAccountNameAndAccountStatus(
            String estId, String prisId, String accName, Account.AccountStatuses accStatus);

    List<Account> findByEstablishmentIdAndPrisonerIdAndAccountStatus(
            String estId, String prisId, Account.AccountStatuses accStatus);

    List<Account> findByPrisonerTransferIn(List<PrisonerTransfer> transfers);

    List<Account> findByEstablishmentIdAndAccountCreatedDateTimeBefore(String establishmentId, ZonedDateTime asOfDateTime);

    List<Account> findByEstablishmentIdAndAccountStatus(String establishmentId, Account.AccountStatuses accountStatuse);

    List<Account> findByPrisonerIdAndAccountNameOrderByAccountCreatedDateTimeAsc( String prisonerId, String accountName);

}
