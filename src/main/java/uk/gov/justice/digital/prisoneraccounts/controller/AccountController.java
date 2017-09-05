package uk.gov.justice.digital.prisoneraccounts.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.digital.prisoneraccounts.api.AccountState;
import uk.gov.justice.digital.prisoneraccounts.api.Balance;
import uk.gov.justice.digital.prisoneraccounts.api.LedgerEntry;
import uk.gov.justice.digital.prisoneraccounts.api.TransactionDetail;
import uk.gov.justice.digital.prisoneraccounts.api.TransferRequest;
import uk.gov.justice.digital.prisoneraccounts.jpa.entity.Account;
import uk.gov.justice.digital.prisoneraccounts.jpa.entity.Transaction;
import uk.gov.justice.digital.prisoneraccounts.service.AccountClosedException;
import uk.gov.justice.digital.prisoneraccounts.service.AccountService;
import uk.gov.justice.digital.prisoneraccounts.service.DebitNotSupportedException;
import uk.gov.justice.digital.prisoneraccounts.service.InsufficientFundsException;
import uk.gov.justice.digital.prisoneraccounts.service.LedgerService;
import uk.gov.justice.digital.prisoneraccounts.service.NoSuchAccountException;
import uk.gov.justice.digital.prisoneraccounts.service.PrisonerTransferService;
import uk.gov.justice.digital.prisoneraccounts.service.TransactionService;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("/prisoneraccounts")
public class AccountController {

    private final LedgerService ledgerService;
    private final AccountService accountService;
    private final TransactionService transactionService;
    private final PrisonerTransferService prisonerTransferService;

    @Autowired
    public AccountController(LedgerService ledgerService, AccountService accountService, TransactionService transactionService, PrisonerTransferService prisonerTransferService) {
        this.ledgerService = ledgerService;
        this.accountService = accountService;
        this.transactionService = transactionService;
        this.prisonerTransferService = prisonerTransferService;
    }

    @RequestMapping(value = "/establishments/{establishmentId}/prisoners/{prisonerId}/accounts/{accountName}", method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<TransactionDetail> ledgerEntryCashAccount(
            @PathVariable("establishmentId") String establishmentId,
            @PathVariable("prisonerId") String prisonerId,
            @PathVariable("accountName") String accountName,
            @RequestBody LedgerEntry ledgerEntry) throws DebitNotSupportedException, InsufficientFundsException, AccountClosedException {
        Transaction transaction = ledgerService.postTransaction(
                establishmentId,
                prisonerId,
                accountName,
                ledgerEntry.getDescription(),
                ledgerEntry.getClientRef(),
                ledgerEntry.getAmountPence(),
                ledgerEntry.getOperation());

        return asResponseEntity(transaction);
    }

    private ResponseEntity<TransactionDetail> asResponseEntity(Transaction transaction) {
        return new ResponseEntity<TransactionDetail>(TransactionDetail.builder()
                .amountPence(transaction.getAmountPence())
                .clientReference(transaction.getClientReference())
                .description(transaction.getDescription())
                .transactionDateTime(transaction.getTransactionDateTime())
                .transactionId(transaction.getTransactionId())
                .transactionType(TransactionDetail.TransactionTypes.valueOf(transaction.getTransactionType().toString()))
                .build(), HttpStatus.OK);
    }

    @RequestMapping(value = "/establishments/{establishmentId}/prisoners/{prisonerId}/accounts/{accName}/balance", method = RequestMethod.GET)
    public ResponseEntity<Balance> getBalance(
            @PathVariable("establishmentId") String establishmentId,
            @PathVariable("prisonerId") String prisonerId,
            @PathVariable("accName") String accName) {

        Optional<Balance> maybeBalance = accountService.currentBalanceOf(establishmentId, prisonerId, accName);

        return maybeBalance
                .map(balance -> new ResponseEntity<>(balance, HttpStatus.OK))
                .orElse(new ResponseEntity<>(NOT_FOUND));
    }

    @RequestMapping(value = "/establishments/{establishmentId}/prisoners/{prisonerId}/accounts/{accName}/transactions", method = RequestMethod.GET)
    public ResponseEntity<List<TransactionDetail>> getPrisonerTransactions(
            @PathVariable("establishmentId") String establishmentId,
            @PathVariable("prisonerId") String prisonerId,
            @PathVariable("accName") String accName,
            @RequestParam(name = "fromDateTime", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime fromDateTime,
            @RequestParam(name = "toDateTime", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime toDateTime) {

        // Hack to work around springfox-swagger problem of ignoring Optional<ZonedDateTime>
        Optional<ZonedDateTime> maybeFromDateTime = Optional.ofNullable(fromDateTime);
        Optional<ZonedDateTime> maybeToDateTime = Optional.ofNullable(toDateTime);

        Optional<Account> maybeAccount = accountService.accountFor(establishmentId, prisonerId, accName);

        Optional<List<TransactionDetail>> maybeTransactionDetails = maybeAccount
                .map(account -> transactionService.getTransactions(account, maybeFromDateTime, maybeToDateTime))
                .map(transactions -> transactions.stream()
                        .map(transaction -> TransactionDetail.builder()
                                .amountPence(transaction.getAmountPence())
                                .clientReference(transaction.getClientReference())
                                .description(transaction.getDescription())
                                .transactionDateTime(transaction.getTransactionDateTime())
                                .transactionType(TransactionDetail.TransactionTypes.valueOf(transaction.getTransactionType().toString()))
                                .transactionId(transaction.getTransactionId())
                                .build())
                        .collect(Collectors.toList()));

        return maybeTransactionDetails.map(transactionDetails -> new ResponseEntity<>(transactionDetails, HttpStatus.OK))
                .orElse(notFound());

    }


    @RequestMapping(value = "/prisoners/{prisonerId}/accounts/{accName}/transactions", method = RequestMethod.GET)
    public ResponseEntity<List<TransactionDetail>> getPrisonerTransactions(
            @PathVariable("prisonerId") String prisonerId,
            @PathVariable("accName") String accName,
            @RequestParam(name = "fromDateTime", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime fromDateTime,
            @RequestParam(name = "toDateTime", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime toDateTime) {

        // Hack to work around springfox-swagger problem of ignoring Optional<ZonedDateTime>
        Optional<ZonedDateTime> maybeFromDateTime = Optional.ofNullable(fromDateTime);
        Optional<ZonedDateTime> maybeToDateTime = Optional.ofNullable(toDateTime);


        List<Account> prisonerAccounts = accountService.namedAccountsFor(prisonerId, accName);

        if (prisonerAccounts.isEmpty()) {
            return notFound();
        }

        List<TransactionDetail> transactionDetailList = prisonerAccounts.stream()
                .map(account -> transactionService.getTransactions(account, maybeFromDateTime, maybeToDateTime))
                .map(transactions -> transactions.stream()
                        .map(transaction -> TransactionDetail.builder()
                                .amountPence(transaction.getAmountPence())
                                .clientReference(transaction.getClientReference())
                                .description(transaction.getDescription())
                                .transactionDateTime(transaction.getTransactionDateTime())
                                .transactionType(TransactionDetail.TransactionTypes.valueOf(transaction.getTransactionType().toString()))
                                .transactionId(transaction.getTransactionId())
                                .build())
                        .collect(Collectors.toList()))
                .flatMap(Collection::stream).collect(Collectors.toList());

        return new ResponseEntity<>(transactionDetailList, HttpStatus.OK);
    }

    @RequestMapping(value = "/establishments/{establishmentId}/prisoners/{prisonerId}/accounts", method = RequestMethod.GET)
    public ResponseEntity<List<Balance>> getPrisonerAccountsSummary(
            @PathVariable("establishmentId") String establishmentId,
            @PathVariable("prisonerId") String prisonerId) {

        List<Balance> balanceList = accountService.prisonerOpenAccounts(establishmentId, prisonerId)
                .stream()
                .map(acc -> accountService.currentBalanceOf(acc))
                .collect(Collectors.toList());

        return accountSummaryFor(balanceList);

    }

    @RequestMapping(value = "/establishments/{establishmentId}/prisoners/accounts", method = RequestMethod.GET)
    public Map<String, List<AccountState>> getPrisonAccounts(
            @PathVariable("establishmentId") String establishmentId) {

        Map<String, List<AccountState>> balances = accountService.establishmentAccountsSummary(establishmentId, Optional.empty());

        return balances;
    }

    private ResponseEntity<List<Balance>> accountSummaryFor(List<Balance> balanceList) {
        if (balanceList.isEmpty()) {
            return new ResponseEntity(NOT_FOUND);
        } else {
            return new ResponseEntity<>(balanceList, OK);
        }
    }

    private ResponseEntity<List<TransactionDetail>> notFound() {
        return new ResponseEntity<>(NOT_FOUND);
    }


    @RequestMapping(value = "/establishments/{establishmentId}/prisoners/{prisonerId}/accounts/transfer", method = RequestMethod.POST)
    public void transferBetweenPrisonerAccounts(
            @RequestBody TransferRequest transferRequest,
            @PathVariable("establishmentId") String establishmentId,
            @PathVariable("prisonerId") String prisonerId) throws NoSuchAccountException, DebitNotSupportedException, InsufficientFundsException, AccountClosedException {

        Account sourceAccount = accountService.accountFor(establishmentId, prisonerId, transferRequest.getFromAccountName())
                .orElseThrow(() -> new NoSuchAccountException("No account found named: " + transferRequest.getFromAccountName()));

        Account targetAccount = accountService.getOrCreateAccount(establishmentId, prisonerId, transferRequest.getToAccountName(), Optional.empty());

        transactionService.transferFunds(sourceAccount, targetAccount, transferRequest.getAmountPence(), "balance transfer");
    }

    @RequestMapping(value = "/establishments/{establishmentId}/prisoners/{prisonerId}/transfer", method = RequestMethod.POST)
    public void transferPrisonerAccountsBetweenEstablishments(
            @RequestParam("toEstablishmentId") String toEstablishmentId,
            @PathVariable("establishmentId") String fromEstablishmentId,
            @PathVariable("prisonerId") String prisonerId) throws NoSuchAccountException, DebitNotSupportedException, InsufficientFundsException, AccountClosedException {


        prisonerTransferService.transferPrisonerAccounts(prisonerId, fromEstablishmentId, toEstablishmentId);


    }

    @ExceptionHandler(DebitNotSupportedException.class)
    public ResponseEntity<String> debitNotSupported(DebitNotSupportedException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<String> insufficientFunds(InsufficientFundsException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }
}
