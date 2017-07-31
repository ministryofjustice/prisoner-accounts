package uk.gov.justice.digital.prisoneraccounts.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.prisoneraccounts.jpa.entity.Account;
import uk.gov.justice.digital.prisoneraccounts.jpa.entity.Transaction;
import uk.gov.justice.digital.prisoneraccounts.jpa.repository.TransactionRepository;

import java.util.Optional;

@Service
public class TransactionService {

    private final AccountService accountService;
    private final TransactionRepository transactionRepository;

    @Autowired
    public TransactionService(AccountService accountService, TransactionRepository transactionRepository) {
        this.accountService = accountService;
        this.transactionRepository = transactionRepository;
    }

    public Transaction creditAccount(Account account, Long amountPence, String description) {
        return transactionRepository.save(Transaction.builder()
                .account(account)
                .amountPence(amountPence)
                .description(description)
                .transactionType(Transaction.TransactionTypes.CREDIT)
                .build());
    }

    public Optional<Transaction> creditAccount(String establishmentId, String prisonerId, String accName, Long amountPence, String description) {
        return accountService.accountFor(establishmentId, prisonerId, accName)
                .map(account -> creditAccount(account, amountPence, description));

    }


    public Optional<Transaction> debitAccount(Account account, Long amountPence, String description) {
        return Optional.ofNullable(account)
                .filter(acc -> acc.getAccountType() != Account.AccountTypes.SAVINGS)
                .filter(acc -> accountService.balanceOf(acc) >= amountPence)
                .map(acc -> transactionRepository.save(Transaction.builder()
                        .account(account)
                        .amountPence(amountPence)
                        .description(description)
                        .transactionType(Transaction.TransactionTypes.DEBIT)
                        .build()));
    }

    public Optional<Transaction> debitAccount(String establishmentId, String prisonerId, String accountName, long amountPence, String description) {
        return accountService.accountFor(establishmentId, prisonerId, accountName)
                .map(acc -> debitAccount(acc, amountPence, description))
                .orElse(Optional.empty());
    }
}
