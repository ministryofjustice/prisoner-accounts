package uk.gov.justice.digital.prisoneraccounts.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.prisoneraccounts.jpa.entity.Account;
import uk.gov.justice.digital.prisoneraccounts.jpa.entity.Transaction;
import uk.gov.justice.digital.prisoneraccounts.jpa.repository.TransactionRepository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TransactionService {

    private final AccountService accountService;
    private final TransactionRepository transactionRepository;

    @Autowired
    public TransactionService(AccountService accountService, TransactionRepository transactionRepository) {
        this.accountService = accountService;
        this.transactionRepository = transactionRepository;
    }

    public Transaction creditAccount(Account account, Long amountPence, String description, String clientRef) throws AccountClosedException {

        checkNotClosed(account);

        return transactionRepository.save(Transaction.builder()
                .account(account)
                .amountPence(amountPence)
                .description(description)
                .clientReference(clientRef)
                .transactionType(Transaction.TransactionTypes.CREDIT)
                .build());
    }


    public Transaction debitAccount(Account account, Long amountPence, String description, String clientRef) throws DebitNotSupportedException, InsufficientFundsException, AccountClosedException {
        checkSufficientFunds(account, amountPence);
        checkNotClosed(account);

        return transactionRepository.save(Transaction.builder()
                .account(account)
                .amountPence(amountPence)
                .description(description)
                .clientReference(clientRef)
                .transactionType(Transaction.TransactionTypes.DEBIT)
                .build());
    }

    private void checkNotClosed(Account account) throws AccountClosedException {
        if (account.getAccountStatus() == Account.AccountStatuses.CLOSED) {
            throw new AccountClosedException("Account " + account.getAccountId() + " is closed. New transactions not permitted.");
        }
    }

    private void checkSufficientFunds(Account acc, Long amountPence) throws InsufficientFundsException {
        if (accountService.currentBalanceOf(acc).getAmountPence() < amountPence) {
            throw new InsufficientFundsException("Insufficient funds.");
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

    @Transactional
    public void transferFunds(Account sourceAccount, Account targetAccount, long amountPence, String description) throws DebitNotSupportedException, InsufficientFundsException, AccountClosedException {
        String clientRef = UUID.randomUUID().toString();
        debitAccount(sourceAccount, amountPence, description, clientRef);
        creditAccount(targetAccount, amountPence, description, clientRef);
    }
}
