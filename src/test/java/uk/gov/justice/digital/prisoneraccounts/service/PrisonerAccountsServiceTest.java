package uk.gov.justice.digital.prisoneraccounts.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.justice.digital.prisoneraccounts.jpa.entity.Account;
import uk.gov.justice.digital.prisoneraccounts.jpa.entity.Transaction;
import uk.gov.justice.digital.prisoneraccounts.jpa.repository.AccountRepository;
import uk.gov.justice.digital.prisoneraccounts.jpa.repository.TransactionRepository;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
public class PrisonerAccountsServiceTest {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private AccountService accountService;


    @Test
    public void canCreateNewAccount() {
        String establishmentId = UUID.randomUUID().toString();
        String prisonerId = UUID.randomUUID().toString();

        String accountName = "my_account";

        Account newAccount = accountService.getOrCreateAccount(establishmentId, prisonerId, accountName, Account.AccountTypes.FULL_ACCESS);

        Account resolvedAccount = accountRepository.findOne(newAccount.getAccountId());

        assertThat(resolvedAccount).isNotNull();
    }

    @Test
    public void canCreditCurrentAccount() {
        String establishmentId = UUID.randomUUID().toString();
        String prisonerId = UUID.randomUUID().toString();

        String accountName = "my_account";

        Account account = accountService.getOrCreateAccount(establishmentId, prisonerId, accountName, Account.AccountTypes.FULL_ACCESS);

        Transaction transaction = transactionService.creditAccount(account, 123l, UUID.randomUUID().toString(), "R186 Signal Box");

        assertThat(transaction).isNotNull();
        Transaction actual = transactionRepository.findOne(transaction.getTransactionId());
        assertThat(actual).isNotNull();
        assertThat(actual.getAmountPence()).isEqualTo(123l);
    }

    @Test
    public void canCreditExistingSavingsAccount() {
        String establishmentId = UUID.randomUUID().toString();
        String prisonerId = UUID.randomUUID().toString();

        String accountName = "my_account";

        Account account = accountService.getOrCreateAccount(establishmentId, prisonerId, accountName, Account.AccountTypes.SAVINGS);

        Transaction transaction = transactionService.creditAccount(account, 123l, UUID.randomUUID().toString(), "R186 Signal Box");

        assertThat(transaction).isNotNull();
        Transaction actual = transactionRepository.findOne(transaction.getTransactionId());
        assertThat(actual).isNotNull();
        assertThat(actual.getAmountPence()).isEqualTo(123l);
    }

    @Test
    public void canDebitCurrentAccountWithFunds() throws DebitNotSupportedException, InsufficientFundsException {
        String establishmentId = UUID.randomUUID().toString();
        String prisonerId = UUID.randomUUID().toString();
        String accountName = "my_account";
        Account newAccount = accountService.getOrCreateAccount(establishmentId, prisonerId, accountName, Account.AccountTypes.FULL_ACCESS);
        transactionService.creditAccount(newAccount, 1000l, UUID.randomUUID().toString(), "Gift");
        Transaction transaction = transactionService.debitAccount(newAccount, 100l, UUID.randomUUID().toString(), "R186 Signal Box");

        assertThat(transaction).isNotNull();
    }

    @Test(expected = InsufficientFundsException.class)
    public void cannotDebitCurrentAccountWithInsufficientFunds() throws DebitNotSupportedException, InsufficientFundsException {
        String establishmentId = UUID.randomUUID().toString();
        String prisonerId = UUID.randomUUID().toString();
        String accountName = "my_account";
        Account account = accountService.getOrCreateAccount(establishmentId, prisonerId, accountName, Account.AccountTypes.FULL_ACCESS);

        transactionService.debitAccount(account, 100l, UUID.randomUUID().toString(), "R186 Signal Box");
    }

    @Test(expected = DebitNotSupportedException.class)
    public void cannotDebitSavingsAccount() throws DebitNotSupportedException, InsufficientFundsException {
        String establishmentId = UUID.randomUUID().toString();
        String prisonerId = UUID.randomUUID().toString();
        String accountName = "my_account";
        Account newAccount = accountService.getOrCreateAccount(establishmentId, prisonerId, accountName, Account.AccountTypes.SAVINGS);
        transactionService.creditAccount(newAccount, 1000l, UUID.randomUUID().toString(), "Gift");
        transactionService.debitAccount(newAccount, 100l, UUID.randomUUID().toString(), "R186 Signal Box");
    }

    @Test
    public void canProvideBalanceOfExistingAccount() {
        String establishmentId = UUID.randomUUID().toString();
        String prisonerId = UUID.randomUUID().toString();
        String accountName = "my_account";
        Account newAccount = accountService.getOrCreateAccount(establishmentId, prisonerId, accountName, Account.AccountTypes.SAVINGS);
        assertThat(accountService.balanceOf(newAccount).getAmountPence()).isEqualTo(0l);
    }

    @Test
    public void cannotProvideBalanceOfUnknownAccount() {
        String establishmentId = UUID.randomUUID().toString();
        String prisonerId = UUID.randomUUID().toString();
        String accountName = "my_account";
        assertThat(accountService.balanceOf(establishmentId, prisonerId, accountName).isPresent()).isFalse();
    }

    @Test
    public void cannotCreateDuplicateAccount() {
        String establishmentId = UUID.randomUUID().toString();
        String prisonerId = UUID.randomUUID().toString();
        String accountName = "my_account";
        Account newAccount = accountService.getOrCreateAccount(establishmentId, prisonerId, accountName, Account.AccountTypes.SAVINGS);
        Account duplicateAccount = accountService.getOrCreateAccount(establishmentId, prisonerId, accountName, Account.AccountTypes.SAVINGS);

        assertThat(duplicateAccount.getAccountId()).isEqualTo(newAccount.getAccountId());
    }

}