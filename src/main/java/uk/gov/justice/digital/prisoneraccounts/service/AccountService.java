package uk.gov.justice.digital.prisoneraccounts.service;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.prisoneraccounts.api.AccountState;
import uk.gov.justice.digital.prisoneraccounts.api.Balance;
import uk.gov.justice.digital.prisoneraccounts.jpa.entity.Account;
import uk.gov.justice.digital.prisoneraccounts.jpa.entity.PrisonerTransfer;
import uk.gov.justice.digital.prisoneraccounts.jpa.entity.Transaction;
import uk.gov.justice.digital.prisoneraccounts.jpa.repository.AccountRepository;
import uk.gov.justice.digital.prisoneraccounts.jpa.repository.TransactionRepository;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    @Autowired
    public AccountService(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    public Account getOrCreateAccount(String establishmentId, String prisonerId, String accName, Optional<PrisonerTransfer> maybePrisonerTransfer) {

        Optional<Account> maybeExistingAccount = accountRepository.findByEstablishmentIdAndPrisonerIdAndAccountNameAndAccountStatus(establishmentId, prisonerId, accName, Account.AccountStatuses.OPEN);

        return maybeExistingAccount.orElseGet(() ->
                accountRepository.save(Account.builder()
                        .establishmentId(establishmentId)
                        .prisonerId(prisonerId)
                        .accountName(accName)
                        .accountType(accountTypeOf(accName))
                        .prisonerTransfer(maybePrisonerTransfer.orElse(null))
                        .build()));
    }

    private Account.AccountTypes accountTypeOf(String accountName) {
        return accountName.equals("savings") ? Account.AccountTypes.SAVINGS : Account.AccountTypes.FULL_ACCESS;
    }

    public Optional<Account> accountFor(String establishmentId, String prisonerId, String accName) {
        return accountRepository.findByEstablishmentIdAndPrisonerIdAndAccountNameAndAccountStatus(
                establishmentId, prisonerId, accName, Account.AccountStatuses.OPEN);
    }

    public Optional<Balance> currentBalanceOf(String establishmentId, String prisonerId, String accName) {
        return accountFor(establishmentId, prisonerId, accName)
                .map(acc -> balanceAsOf(acc, Optional.empty()));
    }

    public Balance currentBalanceOf(Account account) {
        return balanceAsOf(account, Optional.empty());
    }

    public Balance balanceAsOf(Account account, Optional<ZonedDateTime> maybeAsOfDateTime) {
        List<Transaction> transactions =
                maybeAsOfDateTime.map(asOfDateTime -> transactionRepository.findAllByAccountAndTransactionDateTimeLessThanEqualOrderByTransactionDateTimeAsc(account, maybeAsOfDateTime))
                        .orElse(transactionRepository.findAllByAccountOrderByTransactionDateTimeAsc(account));

        long sumCredits = transactions.stream()
                .filter(transaction -> transaction.getTransactionType() == Transaction.TransactionTypes.CREDIT)
                .mapToLong(Transaction::getAmountPence)
                .sum();

        long sumDebits = transactions.stream()
                .filter(transaction -> transaction.getTransactionType() == Transaction.TransactionTypes.DEBIT)
                .mapToLong(Transaction::getAmountPence)
                .sum();

        return Balance.builder()
                .accountName(account.getAccountName())
                .amountPence(sumCredits - sumDebits)
                .build();
    }


    public List<Account> prisonerOpenAccounts(String establishmentId, String prisonerId) {
        return accountRepository.findByEstablishmentIdAndPrisonerIdAndAccountStatus(establishmentId, prisonerId, Account.AccountStatuses.OPEN);
    }

    public void checkNotSavingsAccount(Account acc) throws DebitNotSupportedException {
        if (acc.getAccountType() == Account.AccountTypes.SAVINGS) {
            throw new DebitNotSupportedException("Cannot debit a savings account.");
        }
    }


    public void closeAccount(Account sourceAccount) {
        Account modifiedAccount = sourceAccount.toBuilder()
                .accountStatus(Account.AccountStatuses.CLOSED)
                .accountClosedDateTime(ZonedDateTime.now(ZoneOffset.UTC))
                .build();
        accountRepository.save(
                modifiedAccount);
    }

    public List<Account> accountsForPrisonerTransfers(List<PrisonerTransfer> transfersIn) {
        return accountRepository.findByPrisonerTransferIn(transfersIn);
    }

    public Map<String, List<AccountState>> establishmentAccountsSummary(String establishmentId, Optional<ZonedDateTime> maybeAtDateTime) {
        List<Account> accounts = maybeAtDateTime.map(asOfDateTime -> accountRepository.findByEstablishmentIdAndAccountCreatedDateTimeBefore(establishmentId, asOfDateTime))
                .orElse(accountRepository.findByEstablishmentIdAndAccountStatus(establishmentId, Account.AccountStatuses.OPEN));

        Map<String, List<Account>> prisonerAccounts = accounts.stream().collect(Collectors.groupingBy(Account::getPrisonerId));

        Map<String, List<AccountState>> prisonerBalances = Maps.transformValues(prisonerAccounts, input -> input.stream().map(acc -> xyz(acc,maybeAtDateTime)).collect(Collectors.toList()));
        
//                ImmutableMap.of(balanceDescriptor(maybeAtDateTime), balanceAsOf(acc, maybeAtDateTime), accountStatusDescriptorOf(acc, maybeAtDateTime), historicAccountStatusOf(acc, maybeAtDateTime))).collect(Collectors.toList()));
        return prisonerBalances;
    }

    private AccountState xyz(Account acc, Optional<ZonedDateTime> maybeAtDateTime) {
        return AccountState.builder()
                .accountName(acc.getAccountName())
                .amountPence(balanceAsOf(acc, maybeAtDateTime).getAmountPence())
                .accountStatus(historicAccountStatusOf(acc, maybeAtDateTime))
                .build();
    }

    private Account.AccountStatuses historicAccountStatusOf(Account acc, Optional<ZonedDateTime> maybeAtDateTime) {
        return maybeAtDateTime.map(atDateTime -> Optional.ofNullable(acc.getAccountClosedDateTime()).map(
                closedAtDateTime -> (closedAtDateTime.isAfter(atDateTime)) ? Account.AccountStatuses.OPEN : Account.AccountStatuses.CLOSED).orElse(Account.AccountStatuses.OPEN)
        ).orElse(acc.getAccountStatus());
    }

    private String accountStatusDescriptorOf(Account acc, Optional<ZonedDateTime> maybeAtDateTime) {
        return maybeAtDateTime.map(atDateTime -> "historicAccountStatus").orElse("currentAccountStatus");
    }

    private String balanceDescriptor(Optional<ZonedDateTime> maybeAtDateTime) {
        return maybeAtDateTime.map(atDateTime -> "historicBalance").orElse("currentBalance");
    }
}
