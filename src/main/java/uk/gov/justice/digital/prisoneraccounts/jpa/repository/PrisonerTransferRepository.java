package uk.gov.justice.digital.prisoneraccounts.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.justice.digital.prisoneraccounts.jpa.entity.PrisonerTransfer;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

public interface PrisonerTransferRepository extends JpaRepository<PrisonerTransfer, Long> {

    List<PrisonerTransfer> findAllByFromEstablishmentIdAndAccountsTransferDateTimeBetween(String establishmentId, Optional<ZonedDateTime> from, Optional<ZonedDateTime> to);

    List<PrisonerTransfer> findAllByFromEstablishmentIdAndAccountsTransferDateTimeLessThanEqual(String establishmentId, Optional<ZonedDateTime> to);

    List<PrisonerTransfer> findAllByFromEstablishmentIdAndAccountsTransferDateTimeGreaterThanEqual(String establishmentId, Optional<ZonedDateTime> from);

    List<PrisonerTransfer> findAllByToEstablishmentIdAndAccountsTransferDateTimeBetween(String establishmentId, Optional<ZonedDateTime> from, Optional<ZonedDateTime> to);

    List<PrisonerTransfer> findAllByToEstablishmentIdAndAccountsTransferDateTimeLessThanEqual(String establishmentId, Optional<ZonedDateTime> to);

    List<PrisonerTransfer> findAllByToEstablishmentIdAndAccountsTransferDateTimeGreaterThanEqual(String establishmentId, Optional<ZonedDateTime> from);

    List<PrisonerTransfer> findAllByToEstablishmentId(String establishmentId);

    List<PrisonerTransfer> findAllByFromEstablishmentId(String establishmentId);
}
