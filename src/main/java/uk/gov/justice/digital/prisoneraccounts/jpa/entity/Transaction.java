package uk.gov.justice.digital.prisoneraccounts.jpa.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

@Entity
@Table(name = "TRANSACTIONS")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue
    private Long transactionId;
    @ManyToOne
    @NotNull
    private Account account;
    private String description;
    @Enumerated(EnumType.STRING)
    @NotNull
    private TransactionTypes transactionType;
    @NotNull
    private Long amountPence;
    @NotNull
    @Builder.Default
    private ZonedDateTime transactionDateTime = ZonedDateTime.now(ZoneOffset.UTC);

    public enum TransactionTypes {CREDIT, DEBIT}
}
