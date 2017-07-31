package uk.gov.justice.digital.prisoneraccounts.service;

public class DebitNotSupportedException extends Exception {
    public DebitNotSupportedException(String message) {
        super(message);
    }
}
