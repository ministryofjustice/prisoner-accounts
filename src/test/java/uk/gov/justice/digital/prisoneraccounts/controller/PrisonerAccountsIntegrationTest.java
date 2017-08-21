package uk.gov.justice.digital.prisoneraccounts.controller;

import io.restassured.RestAssured;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.justice.digital.prisoneraccounts.api.Balance;
import uk.gov.justice.digital.prisoneraccounts.api.LedgerEntry;
import uk.gov.justice.digital.prisoneraccounts.api.Operations;
import uk.gov.justice.digital.prisoneraccounts.api.TransactionDetail;
import uk.gov.justice.digital.prisoneraccounts.api.TransferRequest;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringJUnit4ClassRunner.class)
public class PrisonerAccountsIntegrationTest {

    @LocalServerPort
    int port;

    @Before
    public void setup() {
        RestAssured.port = port;
        RestAssured.basePath = "/prisoneraccounts";
    }

    @Test
    public void canPostCreditToSpendAccount() {
        String establishmentId = UUID.randomUUID().toString();
        String prisonerId = UUID.randomUUID().toString();

        LedgerEntry ledgerEntry = LedgerEntry.builder()
                .amountPence(82)
                .clientRef(UUID.randomUUID().toString())
                .description("wages")
                .operation(Operations.CREDIT)
                .build();

        given()
                .body(ledgerEntry).
                when()
                .contentType("application/json")
                .put("/establishments/{establishmentId}/prisoners/{prisonerId}/spend", establishmentId, prisonerId).
                then()
                .statusCode(200);

        given()
                .body(ledgerEntry.toBuilder().clientRef(UUID.randomUUID().toString()).description("wages 2").build()).
                when()
                .contentType("application/json")
                .put("/establishments/{establishmentId}/prisoners/{prisonerId}/spend", establishmentId, prisonerId).
                then()
                .statusCode(200);

        when()
                .get("/establishments/{establishmentId}/prisoners/{prisonerId}/spend/balance", establishmentId, prisonerId).
                then()
                .statusCode(200)
                .body("amountPence", equalTo(164));

    }

    @Test
    public void canPostCreditsToCashAccount() {
        String establishmentId = UUID.randomUUID().toString();
        String prisonerId = UUID.randomUUID().toString();

        LedgerEntry ledgerEntry = LedgerEntry.builder()
                .amountPence(1000)
                .clientRef(UUID.randomUUID().toString())
                .description("Gift")
                .operation(Operations.CREDIT)
                .build();

        given()
                .body(ledgerEntry).
                when()
                .contentType("application/json")
                .put("/establishments/{establishmentId}/prisoners/{prisonerId}/cash", establishmentId, prisonerId).
                then()
                .statusCode(200);

        given()
                .body(ledgerEntry.toBuilder().clientRef(UUID.randomUUID().toString()).description("Gift 2").build()).
                when()
                .contentType("application/json")
                .put("/establishments/{establishmentId}/prisoners/{prisonerId}/cash", establishmentId, prisonerId).
                then()
                .statusCode(200);

        when()
                .get("/establishments/{establishmentId}/prisoners/{prisonerId}/cash/balance", establishmentId, prisonerId).
                then()
                .statusCode(200)
                .body("amountPence", equalTo(2000));

    }

    @Test
    public void canPostCreditToSavingsAccount() {
        String establishmentId = UUID.randomUUID().toString();
        String prisonerId = UUID.randomUUID().toString();

        LedgerEntry ledgerEntry = LedgerEntry.builder()
                .amountPence(1000)
                .clientRef(UUID.randomUUID().toString())
                .description("rainy day")
                .operation(Operations.CREDIT)
                .build();

        given()
                .body(ledgerEntry).
                when()
                .contentType("application/json")
                .put("/establishments/{establishmentId}/prisoners/{prisonerId}/savings", establishmentId, prisonerId).
                then()
                .statusCode(200);

        given()
                .body(ledgerEntry.toBuilder().clientRef(UUID.randomUUID().toString()).description("another rainy day").build()).
                when()
                .contentType("application/json")
                .put("/establishments/{establishmentId}/prisoners/{prisonerId}/savings", establishmentId, prisonerId).
                then()
                .statusCode(200);

        when()
                .get("/establishments/{establishmentId}/prisoners/{prisonerId}/savings/balance", establishmentId, prisonerId).
                then()
                .statusCode(200)
                .body("amountPence", equalTo(2000));

    }

    @Test
    public void canPostDebitsToCashAccountWithSufficientCredit() {
        String establishmentId = UUID.randomUUID().toString();
        String prisonerId = UUID.randomUUID().toString();

        LedgerEntry ledgerEntry = LedgerEntry.builder()
                .amountPence(1000)
                .clientRef(UUID.randomUUID().toString())
                .description("Gift")
                .operation(Operations.CREDIT)
                .build();

        given()
                .body(ledgerEntry).
                when()
                .contentType("application/json")
                .put("/establishments/{establishmentId}/prisoners/{prisonerId}/cash", establishmentId, prisonerId).
                then()
                .statusCode(200);

        given()
                .body(LedgerEntry.builder()
                        .clientRef(UUID.randomUUID().toString())
                        .description("Mojo")
                        .operation(Operations.DEBIT)
                        .amountPence(1)
                        .build()).
                when()
                .contentType("application/json")
                .put("/establishments/{establishmentId}/prisoners/{prisonerId}/cash", establishmentId, prisonerId).
                then()
                .statusCode(200);

        when()
                .get("/establishments/{establishmentId}/prisoners/{prisonerId}/cash/balance", establishmentId, prisonerId).
                then()
                .statusCode(200)
                .body("amountPence", equalTo(999));

    }

    @Test
    public void canPostDebitsToSpendAccountWithSufficientCredit() {
        String establishmentId = UUID.randomUUID().toString();
        String prisonerId = UUID.randomUUID().toString();

        LedgerEntry ledgerEntry = LedgerEntry.builder()
                .amountPence(1000)
                .clientRef(UUID.randomUUID().toString())
                .description("Gift")
                .operation(Operations.CREDIT)
                .build();

        given()
                .body(ledgerEntry).
                when()
                .contentType("application/json")
                .put("/establishments/{establishmentId}/prisoners/{prisonerId}/spend", establishmentId, prisonerId).
                then()
                .statusCode(200);

        given()
                .body(LedgerEntry.builder()
                        .clientRef(UUID.randomUUID().toString())
                        .description("Mojo")
                        .operation(Operations.DEBIT)
                        .amountPence(1)
                        .build()).
                when()
                .contentType("application/json")
                .put("/establishments/{establishmentId}/prisoners/{prisonerId}/spend", establishmentId, prisonerId).
                then()
                .statusCode(200);

        when()
                .get("/establishments/{establishmentId}/prisoners/{prisonerId}/spend/balance", establishmentId, prisonerId).
                then()
                .statusCode(200)
                .body("amountPence", equalTo(999));

    }

    @Test
    public void cannotDebitSavingsAccount() {
        String establishmentId = UUID.randomUUID().toString();
        String prisonerId = UUID.randomUUID().toString();

        LedgerEntry ledgerEntry = LedgerEntry.builder()
                .amountPence(1000)
                .clientRef(UUID.randomUUID().toString())
                .description("Gift")
                .operation(Operations.CREDIT)
                .build();

        given()
                .body(ledgerEntry).
                when()
                .contentType("application/json")
                .put("/establishments/{establishmentId}/prisoners/{prisonerId}/savings", establishmentId, prisonerId).
                then()
                .statusCode(200);

        given()
                .body(LedgerEntry.builder()
                        .clientRef(UUID.randomUUID().toString())
                        .description("Mojo")
                        .operation(Operations.DEBIT)
                        .amountPence(1)
                        .build()).
                when()
                .contentType("application/json")
                .put("/establishments/{establishmentId}/prisoners/{prisonerId}/savings", establishmentId, prisonerId).
                then()
                .statusCode(400)
                .body(equalTo("Cannot debit a savings account."));
    }

    @Test
    public void cannotDebitAccountWithInsufficientFunds() {
        String establishmentId = UUID.randomUUID().toString();
        String prisonerId = UUID.randomUUID().toString();

        LedgerEntry ledgerEntry = newLedgerEntry();

        given()
                .body(ledgerEntry).
                when()
                .contentType("application/json")
                .put("/establishments/{establishmentId}/prisoners/{prisonerId}/cash", establishmentId, prisonerId).
                then()
                .statusCode(200);

        given()
                .body(LedgerEntry.builder()
                        .clientRef(UUID.randomUUID().toString())
                        .description("Mojo")
                        .operation(Operations.DEBIT)
                        .amountPence(10)
                        .build()).
                when()
                .contentType("application/json")
                .put("/establishments/{establishmentId}/prisoners/{prisonerId}/cash", establishmentId, prisonerId).
                then()
                .statusCode(400)
                .body(equalTo("Insufficient funds."));
    }

    private LedgerEntry newLedgerEntry() {
        return LedgerEntry.builder()
                .amountPence(1)
                .clientRef(UUID.randomUUID().toString())
                .description("Gift")
                .operation(Operations.CREDIT)
                .build();
    }

    @Test
    public void canQueryTransactionsWithAndWithoutDateRanges() throws InterruptedException {
        String establishmentId = UUID.randomUUID().toString();
        String prisonerId = UUID.randomUUID().toString();

        TransactionDetail tx1 = given()
                .body(newLedgerEntry()).
                        when()
                .contentType("application/json")
                .put("/establishments/{establishmentId}/prisoners/{prisonerId}/cash", establishmentId, prisonerId).
                        then()
                .statusCode(200)
                .extract().body().as(TransactionDetail.class);

        Thread.sleep(1000);

        TransactionDetail tx2 = given()
                .body(newLedgerEntry()).
                        when()
                .contentType("application/json")
                .put("/establishments/{establishmentId}/prisoners/{prisonerId}/cash", establishmentId, prisonerId).
                        then()
                .statusCode(200)
                .extract().body().as(TransactionDetail.class);

        Thread.sleep(1000);

        TransactionDetail tx3 = given()
                .body(newLedgerEntry()).
                        when()
                .contentType("application/json")
                .put("/establishments/{establishmentId}/prisoners/{prisonerId}/cash", establishmentId, prisonerId).
                        then()
                .statusCode(200)
                .extract().body().as(TransactionDetail.class);

        Thread.sleep(1000);

        TransactionDetail tx4 = given()
                .body(newLedgerEntry()).
                        when()
                .contentType("application/json")
                .put("/establishments/{establishmentId}/prisoners/{prisonerId}/cash", establishmentId, prisonerId).
                        then()
                .statusCode(200)
                .extract().body().as(TransactionDetail.class);

        TransactionDetail[] allTransactions = given()
                .body(newLedgerEntry()).
                        when()
                .contentType("application/json")
                .get("/establishments/{establishmentId}/prisoners/{prisonerId}/cash/transactions", establishmentId, prisonerId).
                        then()
                .statusCode(200)
                .extract().body().as(TransactionDetail[].class);

        assertThat(allTransactions).containsOnly(tx1, tx2, tx3, tx4);

        TransactionDetail[] allTransactionsSince = given()
                .body(newLedgerEntry()).
                        when()
                .contentType("application/json")
                .queryParam("fromDateTime", tx2.getTransactionDateTime().toString())
                .get("/establishments/{establishmentId}/prisoners/{prisonerId}/cash/transactions", establishmentId, prisonerId).
                        then()
                .statusCode(200)
                .extract().body().as(TransactionDetail[].class);

        assertThat(allTransactionsSince).containsOnly(tx2, tx3, tx4);

        TransactionDetail[] allTransactionsTo = given()
                .body(newLedgerEntry()).
                        when()
                .contentType("application/json")
                .queryParam("toDateTime", tx3.getTransactionDateTime().toString())
                .get("/establishments/{establishmentId}/prisoners/{prisonerId}/cash/transactions", establishmentId, prisonerId).
                        then()
                .statusCode(200)
                .extract().body().as(TransactionDetail[].class);

        assertThat(allTransactionsTo).containsOnly(tx1, tx2, tx3);

        TransactionDetail[] allTransactionsInRange = given()
                .body(newLedgerEntry()).
                        when()
                .contentType("application/json")
                .queryParam("fromDateTime", tx2.getTransactionDateTime().toString())
                .queryParam("toDateTime", tx3.getTransactionDateTime().toString())
                .get("/establishments/{establishmentId}/prisoners/{prisonerId}/cash/transactions", establishmentId, prisonerId).
                        then()
                .statusCode(200)
                .extract().body().as(TransactionDetail[].class);

        assertThat(allTransactionsInRange).containsOnly(tx2, tx3);


    }

    @Test
    public void canQueryPrisonerAccountsSummary() throws InterruptedException {
        String establishmentId = UUID.randomUUID().toString();
        String prisonerId = UUID.randomUUID().toString();

        given()
                .body(newLedgerEntry()).
                when()
                .contentType("application/json")
                .put("/establishments/{establishmentId}/prisoners/{prisonerId}/cash", establishmentId, prisonerId).
                then()
                .statusCode(200);

        given()
                .body(newLedgerEntry()).
                when()
                .contentType("application/json")
                .put("/establishments/{establishmentId}/prisoners/{prisonerId}/spend", establishmentId, prisonerId).
                then()
                .statusCode(200);

        given()
                .body(newLedgerEntry()).
                when()
                .contentType("application/json")
                .put("/establishments/{establishmentId}/prisoners/{prisonerId}/savings", establishmentId, prisonerId).
                then()
                .statusCode(200);

        Balance[] summary = given()
                .get("/establishments/{establishmentId}/prisoners/{prisonerId}/summary", establishmentId, prisonerId)
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(Balance[].class);

        assertThat(summary).extracting("accountName").containsExactlyInAnyOrder("cash", "spend", "savings");
    }

    @Test
    public void canTransferFundsBetweenPrisonerAccounts() {
        String establishmentId = UUID.randomUUID().toString();
        String prisonerId = UUID.randomUUID().toString();

        LedgerEntry ledgerEntry = LedgerEntry.builder()
                .amountPence(1000)
                .clientRef(UUID.randomUUID().toString())
                .description("Gift")
                .operation(Operations.CREDIT)
                .build();

        given()
                .body(ledgerEntry).
                when()
                .contentType("application/json")
                .put("/establishments/{establishmentId}/prisoners/{prisonerId}/cash", establishmentId, prisonerId).
                then()
                .statusCode(200);

        given()
                .body(TransferRequest.builder()
                        .fromAccountName("cash")
                        .toAccountName("savings")
                        .amountPence(100l)
                        .build())
                .contentType("application/json").
                when()
                .post("/establishments/{establishmentId}/prisoners/{prisonerId}/transfer", establishmentId, prisonerId)
                .then()
                .statusCode(200);
    }
}