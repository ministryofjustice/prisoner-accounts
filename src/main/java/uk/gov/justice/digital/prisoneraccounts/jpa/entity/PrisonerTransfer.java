package uk.gov.justice.digital.prisoneraccounts.jpa.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

@Entity
@Table(name = "PRISONER_TRANSFERS")
@Getter
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class PrisonerTransfer {
    @Id
    @GeneratedValue
    private Long transferId;
    @NotNull
    private String prisonerId;
    @NotNull
    private String fromEstablishmentId;
    @NotNull
    private String toEstablishmentId;
    @Builder.Default
    private ZonedDateTime accountsTransferDateTime = ZonedDateTime.now(ZoneOffset.UTC);
}
