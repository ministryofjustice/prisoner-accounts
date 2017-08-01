package uk.gov.justice.digital.prisoneraccounts.controller;

import io.restassured.RestAssured;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.justice.digital.prisoneraccounts.api.LedgerEntry;
import uk.gov.justice.digital.prisoneraccounts.api.Operations;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.equalTo;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringJUnit4ClassRunner.class)
public class AccountControllerTest {

    @LocalServerPort
    int port;

    @Before
    public void setup() {
        RestAssured.port = port;
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
                .put("/establishment/{establishmentId}/prisoner/{prisonerId}/spend", establishmentId, prisonerId).
                then()
                .statusCode(200);

        given()
                .body(ledgerEntry.toBuilder().clientRef(UUID.randomUUID().toString()).description("wages 2").build()).
                when()
                .contentType("application/json")
                .put("/establishment/{establishmentId}/prisoner/{prisonerId}/spend", establishmentId, prisonerId).
                then()
                .statusCode(200);

        when()
                .get("/establishment/{establishmentId}/prisoner/{prisonerId}/spend/balance", establishmentId, prisonerId).
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
                .put("/establishment/{establishmentId}/prisoner/{prisonerId}/cash", establishmentId, prisonerId).
                then()
                .statusCode(200);

        given()
                .body(ledgerEntry.toBuilder().clientRef(UUID.randomUUID().toString()).description("Gift 2").build()).
                when()
                .contentType("application/json")
                .put("/establishment/{establishmentId}/prisoner/{prisonerId}/cash", establishmentId, prisonerId).
                then()
                .statusCode(200);

        when()
                .get("/establishment/{establishmentId}/prisoner/{prisonerId}/cash/balance", establishmentId, prisonerId).
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
                .put("/establishment/{establishmentId}/prisoner/{prisonerId}/savings", establishmentId, prisonerId).
                then()
                .statusCode(200);

        given()
                .body(ledgerEntry.toBuilder().clientRef(UUID.randomUUID().toString()).description("another rainy day").build()).
                when()
                .contentType("application/json")
                .put("/establishment/{establishmentId}/prisoner/{prisonerId}/savings", establishmentId, prisonerId).
                then()
                .statusCode(200);

        when()
                .get("/establishment/{establishmentId}/prisoner/{prisonerId}/savings/balance", establishmentId, prisonerId).
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
                .put("/establishment/{establishmentId}/prisoner/{prisonerId}/cash", establishmentId, prisonerId).
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
                .put("/establishment/{establishmentId}/prisoner/{prisonerId}/cash", establishmentId, prisonerId).
                then()
                .statusCode(200);

        when()
                .get("/establishment/{establishmentId}/prisoner/{prisonerId}/cash/balance", establishmentId, prisonerId).
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
                .put("/establishment/{establishmentId}/prisoner/{prisonerId}/spend", establishmentId, prisonerId).
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
                .put("/establishment/{establishmentId}/prisoner/{prisonerId}/spend", establishmentId, prisonerId).
                then()
                .statusCode(200);

        when()
                .get("/establishment/{establishmentId}/prisoner/{prisonerId}/spend/balance", establishmentId, prisonerId).
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
                .put("/establishment/{establishmentId}/prisoner/{prisonerId}/savings", establishmentId, prisonerId).
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
                .put("/establishment/{establishmentId}/prisoner/{prisonerId}/savings", establishmentId, prisonerId).
                then()
                .statusCode(400)
                .body(equalTo("Cannot debit a savings account."));


    }

}