package uk.gov.justice.digital.prisoneraccounts.jpa.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.TimeZone;

@Entity
@Table(name = "ACCOUNTS")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    @Id
    @GeneratedValue
    private Long accountId;
    @NotNull
    private String establishmentId;
    @NotNull
    private String prisonerId;
    @NotNull
    private String accountName;
    @Enumerated(EnumType.STRING)
    @NotNull
    private AccountTypes accountType;
    @Enumerated(EnumType.STRING)
    @NotNull
    @Builder.Default
    private AccountStatuses accountStatus = AccountStatuses.OPEN;
    @NotNull
    @Builder.Default
    private ZonedDateTime accountCreatedDateTime = ZonedDateTime.now(ZoneOffset.UTC);
    private ZonedDateTime accountClosedDateTime;
    public enum AccountTypes {SAVINGS, FULL_ACCESS}
    public enum AccountStatuses {OPEN, CLOSED}

}
