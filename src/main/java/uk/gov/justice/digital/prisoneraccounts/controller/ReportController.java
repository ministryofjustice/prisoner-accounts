package uk.gov.justice.digital.prisoneraccounts.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.digital.prisoneraccounts.api.AccountState;
import uk.gov.justice.digital.prisoneraccounts.api.EstablishmentTransferSummary;
import uk.gov.justice.digital.prisoneraccounts.service.AccountService;
import uk.gov.justice.digital.prisoneraccounts.service.PrisonerTransferService;
import uk.gov.justice.digital.prisoneraccounts.service.TransactionService;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/reporting")
public class ReportController {

    private final AccountService accountService;
    private final TransactionService transactionService;
    private final PrisonerTransferService prisonerTransferService;

    @Autowired
    public ReportController(AccountService accountService, TransactionService transactionService, PrisonerTransferService prisonerTransferService) {
        this.accountService = accountService;
        this.transactionService = transactionService;
        this.prisonerTransferService = prisonerTransferService;
    }

    @RequestMapping(value = "/establishments/{establishmentId}/prisonertransfers", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public EstablishmentTransferSummary transfersReport(
            @PathVariable("establishmentId") String establishmentId,
            @RequestParam(name = "fromDateTime", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime fromDateTime,
            @RequestParam(name = "toDateTime", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime toDateTime) {

        return prisonerTransferService.prisonerTransferAccountsSummary(establishmentId, Optional.ofNullable(fromDateTime), Optional.ofNullable(toDateTime));

    }

    @RequestMapping(value = "/establishments/{establishmentId}/prisoners/accounts", method = RequestMethod.GET)
    public Map<String, List<AccountState>> getPrisonAccountsReport(
            @PathVariable("establishmentId") String establishmentId,
            @RequestParam(name = "atDateTime", required = true) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime atDateTime) {


        Map<String, List<AccountState>> balances = accountService.establishmentAccountsSummary(establishmentId, Optional.of(atDateTime));

        return balances;
    }

}
