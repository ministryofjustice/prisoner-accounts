package uk.gov.justice.digital.prisoneraccounts.service;

public class NoSuchAccountException extends Exception {
    public NoSuchAccountException(String message) {
        super(message);
    }
}
