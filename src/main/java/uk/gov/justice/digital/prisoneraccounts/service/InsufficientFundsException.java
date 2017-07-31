package uk.gov.justice.digital.prisoneraccounts.service;

public class InsufficientFundsException extends Exception {
    public InsufficientFundsException(String message) {
        super(message);
    }
}
