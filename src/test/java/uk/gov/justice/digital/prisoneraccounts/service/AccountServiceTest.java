package uk.gov.justice.digital.prisoneraccounts.service;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.Assert.fail;

@SpringBootTest
@Ignore
public class AccountServiceTest {

    @Test
    public void creditingUnknownAccountCreatesNewAccountAndAppliesCredit() {
        fail();
    }

    @Test
    public void canCreditExistingCurrentAccount() {
        fail();
    }

    @Test
    public void canCreditExistingSavingsAccount() {
        fail();
    }

    @Test
    public void cannotDebitUnknownAccount() {
        fail();
    }

    @Test
    public void canDebitCurrentAccountWithFunds() {
        fail();
    }

    @Test
    public void cannotDebitCurrentAccountWithInsufficientFunds() {
        fail();
    }

    @Test
    public void cannotDebitSavingsAccount() {
        fail();
    }

    @Test
    public void canProvideBalanceOfExistingAccount() {
        fail();
    }

    @Test
    public void cannotProvideBalanceOfUnknownAccount() {
        fail();
    }


}