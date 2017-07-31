package uk.gov.justice.digital.prisoneraccounts.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.justice.digital.prisoneraccounts.jpa.entity.Account;
import uk.gov.justice.digital.prisoneraccounts.jpa.entity.Transaction;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findAllByAccount(Account account);
}
