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
import uk.gov.justice.digital.prisoneraccounts.api.Balance;
import uk.gov.justice.digital.prisoneraccounts.api.LedgerEntry;
import uk.gov.justice.digital.prisoneraccounts.api.TransactionDetail;
import uk.gov.justice.digital.prisoneraccounts.jpa.entity.Account;
import uk.gov.justice.digital.prisoneraccounts.jpa.entity.Transaction;
import uk.gov.justice.digital.prisoneraccounts.service.AccountService;
import uk.gov.justice.digital.prisoneraccounts.service.DebitNotSupportedException;
import uk.gov.justice.digital.prisoneraccounts.service.InsufficientFundsException;
import uk.gov.justice.digital.prisoneraccounts.service.LedgerService;
import uk.gov.justice.digital.prisoneraccounts.service.TransactionService;

import java.time.ZonedDateTime;
import java.util.List;
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

    @Autowired
    public AccountController(LedgerService ledgerService, AccountService accountService, TransactionService transactionService) {
        this.ledgerService = ledgerService;
        this.accountService = accountService;
        this.transactionService = transactionService;
    }

    @RequestMapping(value = "/establishments/{establishmentId}/prisoners/{prisonerId}/cash", method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<TransactionDetail> ledgerEntryCashAccount(
            @PathVariable("establishmentId") String establishmentId,
            @PathVariable("prisonerId") String prisonerId,
            @RequestBody LedgerEntry ledgerEntry) throws DebitNotSupportedException, InsufficientFundsException {
        Transaction transaction = ledgerService.postTransaction(
                establishmentId,
                prisonerId,
                "cash",
                ledgerEntry.getDescription(),
                ledgerEntry.getClientRef(),
                ledgerEntry.getAmountPence(),
                Account.AccountTypes.FULL_ACCESS,
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

    @RequestMapping(value = "/establishments/{establishmentId}/prisoners/{prisonerId}/spend", method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<TransactionDetail> ledgerEntrySpendAccount(
            @PathVariable("establishmentId") String establishmentId,
            @PathVariable("prisonerId") String prisonerId,
            @RequestBody LedgerEntry ledgerEntry) throws DebitNotSupportedException, InsufficientFundsException {
        Transaction transaction = ledgerService.postTransaction(
                establishmentId,
                prisonerId,
                "spend",
                ledgerEntry.getDescription(),
                ledgerEntry.getClientRef(),
                ledgerEntry.getAmountPence(),
                Account.AccountTypes.FULL_ACCESS,
                ledgerEntry.getOperation());

        return asResponseEntity(transaction);
    }

    @RequestMapping(value = "/establishments/{establishmentId}/prisoners/{prisonerId}/savings", method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<TransactionDetail> ledgerEntrySavingsAccount(
            @PathVariable("establishmentId") String establishmentId,
            @PathVariable("prisonerId") String prisonerId,
            @RequestBody LedgerEntry ledgerEntry) throws DebitNotSupportedException, InsufficientFundsException {
        Transaction transaction = ledgerService.postTransaction(
                establishmentId,
                prisonerId,
                "savings",
                ledgerEntry.getDescription(),
                ledgerEntry.getClientRef(),
                ledgerEntry.getAmountPence(),
                Account.AccountTypes.SAVINGS,
                ledgerEntry.getOperation());

        return asResponseEntity(transaction);
    }

    @RequestMapping(value = "/establishments/{establishmentId}/prisoners/{prisonerId}/{accName}/balance", method = RequestMethod.GET)
    public ResponseEntity<Balance> getBalance(
            @PathVariable("establishmentId") String establishmentId,
            @PathVariable("prisonerId") String prisonerId,
            @PathVariable("accName") String accName) {

        Optional<Balance> maybeBalance = accountService.balanceOf(establishmentId, prisonerId, accName);

        return maybeBalance
                .map(balance -> new ResponseEntity<>(balance, HttpStatus.OK))
                .orElse(new ResponseEntity<>(NOT_FOUND));
    }

    @RequestMapping(value = "/establishments/{establishmentId}/prisoners/{prisonerId}/{accName}/transactions", method = RequestMethod.GET)
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

    @RequestMapping(value = "/establishments/{establishmentId}/prisoners/{prisonerId}/summary", method = RequestMethod.GET)
    public ResponseEntity<List<Balance>> getPrisonerAccountsSummary(
            @PathVariable("establishmentId") String establishmentId,
            @PathVariable("prisonerId") String prisonerId) {

        List<Balance> balanceList = accountService.prisonerOpenAccounts(establishmentId, prisonerId)
                .stream()
                .map(acc -> accountService.balanceOf(acc))
                .collect(Collectors.toList());

        return accountSummaryFor(balanceList);

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


    @ExceptionHandler(DebitNotSupportedException.class)
    public ResponseEntity<String> debitNotSupported(DebitNotSupportedException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<String> insufficientFunds(InsufficientFundsException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }
}
