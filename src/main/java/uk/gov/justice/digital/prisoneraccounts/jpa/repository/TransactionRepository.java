package uk.gov.justice.digital.prisoneraccounts.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.justice.digital.prisoneraccounts.jpa.entity.Account;
import uk.gov.justice.digital.prisoneraccounts.jpa.entity.Transaction;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findAllByAccountOrderByTransactionDateTimeAsc(Account account);

    List<Transaction> findAllByAccountAndTransactionDateTimeBetweenOrderByTransactionDateTimeAsc(Account account, Optional<ZonedDateTime> from, Optional<ZonedDateTime> to);

    List<Transaction> findAllByAccountAndTransactionDateTimeLessThanEqualOrderByTransactionDateTimeAsc(Account account, Optional<ZonedDateTime> to);

    List<Transaction> findAllByAccountAndTransactionDateTimeGreaterThanEqualOrderByTransactionDateTimeAsc(Account account, Optional<ZonedDateTime> from);
}
