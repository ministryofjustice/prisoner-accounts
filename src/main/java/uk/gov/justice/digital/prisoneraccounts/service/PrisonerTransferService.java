package uk.gov.justice.digital.prisoneraccounts.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.prisoneraccounts.jpa.entity.Account;

import java.util.List;

@Service
public class PrisonerTransferService {

    private final AccountService accountService;
    private final TransactionService transactionService;

    @Autowired
    public PrisonerTransferService(AccountService accountService, TransactionService transactionService) {
        this.accountService = accountService;
        this.transactionService = transactionService;
    }

    @Transactional
    public void transferAccountsToInstitutionId(List<Account> accounts, String targetInstitutionId) throws DebitNotSupportedException, InsufficientFundsException, AccountClosedException {
        for (Account sourceAccount : accounts) {
            Account targetAccount = accountService.getOrCreateAccount(targetInstitutionId, sourceAccount.getPrisonerId(), sourceAccount.getAccountName());

            transactionService.transferFunds(sourceAccount, targetAccount, accountService.balanceOf(sourceAccount).getAmountPence(), "prisoner transfer");
            accountService.closeAccount(sourceAccount);
        }
    }


}
