package uk.gov.justice.digital.prisoneraccounts.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.prisoneraccounts.api.Operations;
import uk.gov.justice.digital.prisoneraccounts.jpa.entity.Account;
import uk.gov.justice.digital.prisoneraccounts.jpa.entity.Transaction;

@Service
public class LedgerService {

    private final AccountService accountService;
    private final TransactionService transactionService;

    @Autowired
    public LedgerService(AccountService accountService, TransactionService transactionService) {
        this.accountService = accountService;
        this.transactionService = transactionService;
    }

    public Transaction postTransaction(String establishmentId, String prisonerId, String accountName, String description, String clientRef, long amountPence, Account.AccountTypes accountType, Operations operation) throws DebitNotSupportedException, InsufficientFundsException {
        Account account = accountService.getOrCreateAccount(establishmentId, prisonerId, accountName, accountType);

        Transaction result = null;
        switch (operation) {
            case CREDIT:
                result = transactionService.creditAccount(account, amountPence, description, clientRef);
                break;
            case DEBIT:
                result = transactionService.debitAccount(account, amountPence, description, clientRef);
                break;
        }
        return result;
    }
}
