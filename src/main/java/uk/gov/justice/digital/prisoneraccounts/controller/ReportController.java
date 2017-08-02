package uk.gov.justice.digital.prisoneraccounts.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.digital.prisoneraccounts.service.AccountService;
import uk.gov.justice.digital.prisoneraccounts.service.TransactionService;

@RestController
@RequestMapping("/prisoneraccounts")
public class ReportController {

    private final AccountService accountService;
    private final TransactionService transactionService;

    @Autowired
    public ReportController(AccountService accountService, TransactionService transactionService) {
        this.accountService = accountService;
        this.transactionService = transactionService;
    }

}
