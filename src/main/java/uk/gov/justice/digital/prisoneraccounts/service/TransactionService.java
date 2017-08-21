package uk.gov.justice.digital.prisoneraccounts.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.prisoneraccounts.jpa.entity.Account;
import uk.gov.justice.digital.prisoneraccounts.jpa.entity.Transaction;
import uk.gov.justice.digital.prisoneraccounts.jpa.repository.TransactionRepository;

import java.time.ZonedDateTime;
import java.util.List;
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

    public Transaction creditAccount(Account account, Long amountPence, String clientRef, String description) {
        return transactionRepository.save(Transaction.builder()
                .account(account)
                .amountPence(amountPence)
                .description(description)
                .clientReference(clientRef)
                .transactionType(Transaction.TransactionTypes.CREDIT)
                .build());
    }


    public Transaction debitAccount(Account account, Long amountPence, String clientRef, String description) throws DebitNotSupportedException, InsufficientFundsException {

        checkNotSavingsAccount(account);
        checkSufficientFunds(account, amountPence);

        return transactionRepository.save(Transaction.builder()
                .account(account)
                .amountPence(amountPence)
                .description(description)
                .clientReference(clientRef)
                .transactionType(Transaction.TransactionTypes.DEBIT)
                .build());
    }

    private void checkSufficientFunds(Account acc, Long amountPence) throws InsufficientFundsException {
        if (accountService.balanceOf(acc).getAmountPence() < amountPence) {
            throw new InsufficientFundsException("Insufficient funds.");
        }
    }

    private void checkNotSavingsAccount(Account acc) throws DebitNotSupportedException {
        if (acc.getAccountType() == Account.AccountTypes.SAVINGS) {
            throw new DebitNotSupportedException("Cannot debit a savings account.");
        }
    }

    public List<Transaction> getTransactions(Account account, Optional<ZonedDateTime> from, Optional<ZonedDateTime> to) {

        if (from.isPresent() && to.isPresent()) {
            return transactionRepository.findAllByAccountAndTransactionDateTimeBetweenOrderByTransactionDateTimeAsc(account, from, to);
        } else if (from.isPresent() && !to.isPresent()) {
            return transactionRepository.findAllByAccountAndTransactionDateTimeGreaterThanEqualOrderByTransactionDateTimeAsc(account, from);
        } else if (!from.isPresent() && to.isPresent()) {
            return transactionRepository.findAllByAccountAndTransactionDateTimeLessThanEqualOrderByTransactionDateTimeAsc(account, to);
        } else {
            return transactionRepository.findAllByAccountOrderByTransactionDateTimeAsc(account);
        }
    }
}
