package uk.gov.justice.digital.prisoneraccounts.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.justice.digital.prisoneraccounts.api.Operations;
import uk.gov.justice.digital.prisoneraccounts.jpa.entity.Account;
import uk.gov.justice.digital.prisoneraccounts.jpa.entity.Transaction;
import uk.gov.justice.digital.prisoneraccounts.jpa.repository.AccountRepository;
import uk.gov.justice.digital.prisoneraccounts.jpa.repository.TransactionRepository;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Optional;
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

    @Autowired
    private LedgerService ledgerService;

    @Autowired
    private PrisonerTransferService prisonerTransferService;


    @Test
    public void canCreateNewAccount() {
        String establishmentId = UUID.randomUUID().toString();
        String prisonerId = UUID.randomUUID().toString();

        String accountName = "my_account";

        Account newAccount = accountService.getOrCreateAccount(establishmentId, prisonerId, accountName, Optional.empty());

        Account resolvedAccount = accountRepository.findOne(newAccount.getAccountId());

        assertThat(resolvedAccount).isNotNull();
    }

    @Test
    public void canCreditCurrentAccount() throws AccountClosedException {
        String establishmentId = UUID.randomUUID().toString();
        String prisonerId = UUID.randomUUID().toString();

        String accountName = "my_account";

        Account account = accountService.getOrCreateAccount(establishmentId, prisonerId, accountName, Optional.empty());

        Transaction transaction = transactionService.creditAccount(account, 123l, UUID.randomUUID().toString(), "R186 Signal Box");

        assertThat(transaction).isNotNull();
        Transaction actual = transactionRepository.findOne(transaction.getTransactionId());
        assertThat(actual).isNotNull();
        assertThat(actual.getAmountPence()).isEqualTo(123l);
    }

    @Test
    public void canCreditExistingSavingsAccount() throws AccountClosedException {
        String establishmentId = UUID.randomUUID().toString();
        String prisonerId = UUID.randomUUID().toString();

        String accountName = "savings";

        Account account = accountService.getOrCreateAccount(establishmentId, prisonerId, accountName, Optional.empty());

        Transaction transaction = transactionService.creditAccount(account, 123l, UUID.randomUUID().toString(), "R186 Signal Box");

        assertThat(transaction).isNotNull();
        Transaction actual = transactionRepository.findOne(transaction.getTransactionId());
        assertThat(actual).isNotNull();
        assertThat(actual.getAmountPence()).isEqualTo(123l);
    }

    @Test
    public void canDebitCurrentAccountWithFunds() throws DebitNotSupportedException, InsufficientFundsException, AccountClosedException {
        String establishmentId = UUID.randomUUID().toString();
        String prisonerId = UUID.randomUUID().toString();
        String accountName = "my_account";
        Account newAccount = accountService.getOrCreateAccount(establishmentId, prisonerId, accountName, Optional.empty());
        transactionService.creditAccount(newAccount, 1000l, UUID.randomUUID().toString(), "Gift");
        Transaction transaction = transactionService.debitAccount(newAccount, 100l, UUID.randomUUID().toString(), "R186 Signal Box");

        assertThat(transaction).isNotNull();
    }

    @Test(expected = InsufficientFundsException.class)
    public void cannotDebitCurrentAccountWithInsufficientFunds() throws DebitNotSupportedException, InsufficientFundsException, AccountClosedException {
        String establishmentId = UUID.randomUUID().toString();
        String prisonerId = UUID.randomUUID().toString();
        String accountName = "my_account";
        Account account = accountService.getOrCreateAccount(establishmentId, prisonerId, accountName, Optional.empty());

        transactionService.debitAccount(account, 100l, UUID.randomUUID().toString(), "R186 Signal Box");
    }

    @Test(expected = DebitNotSupportedException.class)
    public void cannotDebitSavingsAccountThroughLedgerService() throws DebitNotSupportedException, InsufficientFundsException, AccountClosedException {
        String establishmentId = UUID.randomUUID().toString();
        String prisonerId = UUID.randomUUID().toString();
        String accountName = "savings";
        Account newAccount = accountService.getOrCreateAccount(establishmentId, prisonerId, accountName, Optional.empty());
        transactionService.creditAccount(newAccount, 1000l, UUID.randomUUID().toString(), "Gift");
        ledgerService.postTransaction(establishmentId, prisonerId, accountName, "try a debit", "my ref", 100l, Operations.DEBIT);
    }

    @Test
    public void canProvideBalanceOfExistingAccount() {
        String establishmentId = UUID.randomUUID().toString();
        String prisonerId = UUID.randomUUID().toString();
        String accountName = "my_account";
        Account newAccount = accountService.getOrCreateAccount(establishmentId, prisonerId, accountName, Optional.empty());
        assertThat(accountService.currentBalanceOf(newAccount).getAmountPence()).isEqualTo(0l);
    }

    @Test
    public void cannotProvideBalanceOfUnknownAccount() {
        String establishmentId = UUID.randomUUID().toString();
        String prisonerId = UUID.randomUUID().toString();
        String accountName = "my_account";
        assertThat(accountService.currentBalanceOf(establishmentId, prisonerId, accountName).isPresent()).isFalse();
    }

    @Test
    public void cannotCreateDuplicateAccount() {
        String establishmentId = UUID.randomUUID().toString();
        String prisonerId = UUID.randomUUID().toString();
        String accountName = "my_account";
        Account newAccount = accountService.getOrCreateAccount(establishmentId, prisonerId, accountName, Optional.empty());
        Account duplicateAccount = accountService.getOrCreateAccount(establishmentId, prisonerId, accountName, Optional.empty());

        assertThat(duplicateAccount.getAccountId()).isEqualTo(newAccount.getAccountId());
    }

    @Test
    public void canTransferFundsBetweenPrisonerAccounts() throws DebitNotSupportedException, InsufficientFundsException, AccountClosedException {
        String establishmentId = UUID.randomUUID().toString();
        String prisonerId = UUID.randomUUID().toString();

        Account sourceAccount = accountService.getOrCreateAccount(establishmentId, prisonerId, "cash", Optional.empty());
        Account targetAccount = accountService.getOrCreateAccount(establishmentId, prisonerId, "savings", Optional.empty());

        Transaction transaction = transactionService.creditAccount(sourceAccount, 123l, UUID.randomUUID().toString(), "R186 Signal Box");
        transactionService.transferFunds(sourceAccount, targetAccount, 100l, "balance transfer");

        assertThat(accountService.currentBalanceOf(sourceAccount).getAmountPence()).isEqualTo(23l);
        assertThat(accountService.currentBalanceOf(targetAccount).getAmountPence()).isEqualTo(100);

    }

    @Test
    public void canCloseAccount() {
        String establishmentId = UUID.randomUUID().toString();
        String prisonerId = UUID.randomUUID().toString();

        Account account = accountService.getOrCreateAccount(establishmentId, prisonerId, "cash", Optional.empty());

        accountService.closeAccount(account);

        assertThat(accountRepository.findOne(account.getAccountId())).extracting("accountStatus").containsExactly(Account.AccountStatuses.CLOSED);

    }

    @Test
    public void canTransferPrisonerAccountsBetweenEstablishments() throws InsufficientFundsException, AccountClosedException, DebitNotSupportedException {
        String sourceEstablishmentId = UUID.randomUUID().toString();
        String prisonerId = UUID.randomUUID().toString();

        Account cash = accountService.getOrCreateAccount(sourceEstablishmentId, prisonerId, "cash", Optional.empty());
        Account spend = accountService.getOrCreateAccount(sourceEstablishmentId, prisonerId, "spend", Optional.empty());
        Account savings = accountService.getOrCreateAccount(sourceEstablishmentId, prisonerId, "savings", Optional.empty());

        transactionService.creditAccount(cash, 1l, "cr1", "gift");
        transactionService.creditAccount(spend, 2l, "cr2", "gift");
        transactionService.creditAccount(savings, 3l, "cr3", "gift");

        String targetEstablishmentId = UUID.randomUUID().toString();

        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);

        prisonerTransferService.transferPrisonerAccounts(prisonerId, sourceEstablishmentId, targetEstablishmentId);

        Account updatedCash = accountRepository.findOne(cash.getAccountId());
        Account updatedSpend = accountRepository.findOne(cash.getAccountId());
        Account updatedSavings = accountRepository.findOne(cash.getAccountId());

        assertThat(updatedCash.getAccountStatus()).isEqualTo(Account.AccountStatuses.CLOSED);
        assertThat(updatedSpend.getAccountStatus()).isEqualTo(Account.AccountStatuses.CLOSED);
        assertThat(updatedSavings.getAccountStatus()).isEqualTo(Account.AccountStatuses.CLOSED);

        assertThat(updatedCash.getAccountClosedDateTime()).isGreaterThanOrEqualTo(now);
        assertThat(updatedSpend.getAccountClosedDateTime()).isGreaterThanOrEqualTo(now);
        assertThat(updatedSavings.getAccountClosedDateTime()).isGreaterThanOrEqualTo(now);

        assertThat(accountService.currentBalanceOf(updatedCash).getAmountPence()).isEqualTo(0l);
        assertThat(accountService.currentBalanceOf(updatedSpend).getAmountPence()).isEqualTo(0l);
        assertThat(accountService.currentBalanceOf(updatedSavings).getAmountPence()).isEqualTo(0l);

        Account transferredCash = accountService.accountFor(targetEstablishmentId, prisonerId, "cash").get();
        Account transferredSpend = accountService.accountFor(targetEstablishmentId, prisonerId, "spend").get();
        Account transferredSavings = accountService.accountFor(targetEstablishmentId, prisonerId, "savings").get();

        assertThat(accountService.currentBalanceOf(transferredCash).getAmountPence()).isEqualTo(1l);
        assertThat(accountService.currentBalanceOf(transferredSpend).getAmountPence()).isEqualTo(2l);
        assertThat(accountService.currentBalanceOf(transferredSavings).getAmountPence()).isEqualTo(3l);
    }

}