package uk.gov.justice.digital.prisoneraccounts.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.digital.prisoneraccounts.api.Balance;
import uk.gov.justice.digital.prisoneraccounts.api.LedgerEntry;
import uk.gov.justice.digital.prisoneraccounts.jpa.entity.Account;
import uk.gov.justice.digital.prisoneraccounts.service.AccountService;
import uk.gov.justice.digital.prisoneraccounts.service.DebitNotSupportedException;
import uk.gov.justice.digital.prisoneraccounts.service.InsufficientFundsException;
import uk.gov.justice.digital.prisoneraccounts.service.LedgerService;

import java.util.Optional;

@RestController
@RequestMapping("/prisoneraccounts")
public class AccountController {

    private final LedgerService ledgerService;
    private final AccountService accountService;

    @Autowired
    public AccountController(LedgerService ledgerService, AccountService accountService) {
        this.ledgerService = ledgerService;
        this.accountService = accountService;
    }

    @RequestMapping(value = "/establishment/{establishmentId}/prisoner/{prisonerId}/cash", method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.OK)
    public void ledgerEntryCashAccount(
            @PathVariable("establishmentId") String establishmentId,
            @PathVariable("prisonerId") String prisonerId,
            @RequestBody LedgerEntry ledgerEntry) throws DebitNotSupportedException, InsufficientFundsException {
        ledgerService.postTransaction(
                establishmentId,
                prisonerId,
                "cash",
                ledgerEntry.getDescription(),
                ledgerEntry.getClientRef(),
                ledgerEntry.getAmountPence(),
                Account.AccountTypes.FULL_ACCESS,
                ledgerEntry.getOperation());
    }

    @RequestMapping(value = "/establishment/{establishmentId}/prisoner/{prisonerId}/spend", method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.OK)
    public void ledgerEntrySpendAccount(
            @PathVariable("establishmentId") String establishmentId,
            @PathVariable("prisonerId") String prisonerId,
            @RequestBody LedgerEntry ledgerEntry) throws DebitNotSupportedException, InsufficientFundsException {
        ledgerService.postTransaction(
                establishmentId,
                prisonerId,
                "spend",
                ledgerEntry.getDescription(),
                ledgerEntry.getClientRef(),
                ledgerEntry.getAmountPence(),
                Account.AccountTypes.FULL_ACCESS,
                ledgerEntry.getOperation());
    }

    @RequestMapping(value = "/establishment/{establishmentId}/prisoner/{prisonerId}/savings", method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.OK)
    public void ledgerEntrySavingsAccount(
            @PathVariable("establishmentId") String establishmentId,
            @PathVariable("prisonerId") String prisonerId,
            @RequestBody LedgerEntry ledgerEntry) throws DebitNotSupportedException, InsufficientFundsException {
        ledgerService.postTransaction(
                establishmentId,
                prisonerId,
                "savings",
                ledgerEntry.getDescription(),
                ledgerEntry.getClientRef(),
                ledgerEntry.getAmountPence(),
                Account.AccountTypes.SAVINGS,
                ledgerEntry.getOperation());
    }

    @RequestMapping(value = "/establishment/{establishmentId}/prisoner/{prisonerId}/{accName}/balance", method = RequestMethod.GET)
    public ResponseEntity<Balance> getBalance(
            @PathVariable("establishmentId") String establishmentId,
            @PathVariable("prisonerId") String prisonerId,
            @PathVariable("accName") String accName) {

        Optional<Balance> maybeBalance = accountService.balanceOf(establishmentId, prisonerId, accName);

        return maybeBalance
                .map(balance -> new ResponseEntity<>(balance, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @ExceptionHandler(DebitNotSupportedException.class)
    public ResponseEntity<String> debitNotSupported(DebitNotSupportedException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

}
