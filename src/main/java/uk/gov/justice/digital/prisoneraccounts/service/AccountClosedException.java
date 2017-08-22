package uk.gov.justice.digital.prisoneraccounts.service;

public class AccountClosedException extends Exception {
    public AccountClosedException(String message) {
        super(message);
    }
}
