package uk.gov.justice.digital.prisoneraccounts.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.prisoneraccounts.api.EstablishmentTransferSummary;
import uk.gov.justice.digital.prisoneraccounts.api.TransferIn;
import uk.gov.justice.digital.prisoneraccounts.api.TransferOut;
import uk.gov.justice.digital.prisoneraccounts.jpa.entity.Account;
import uk.gov.justice.digital.prisoneraccounts.jpa.entity.PrisonerTransfer;
import uk.gov.justice.digital.prisoneraccounts.jpa.repository.PrisonerTransferRepository;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PrisonerTransferService {

    private final AccountService accountService;
    private final TransactionService transactionService;
    private final PrisonerTransferRepository prisonerTransferRepository;

    @Autowired
    public PrisonerTransferService(AccountService accountService, TransactionService transactionService, PrisonerTransferRepository prisonerTransferRepository) {
        this.accountService = accountService;
        this.transactionService = transactionService;
        this.prisonerTransferRepository = prisonerTransferRepository;
    }

    @Transactional
    private void transferAccountsToEstablishmentId(List<Account> accounts, String toEstablishmentId, PrisonerTransfer prisonerTransfer) throws DebitNotSupportedException, InsufficientFundsException, AccountClosedException {
        for (Account sourceAccount : accounts) {
            Account targetAccount = accountService.getOrCreateAccount(toEstablishmentId, sourceAccount.getPrisonerId(), sourceAccount.getAccountName(), Optional.of(prisonerTransfer));

            transactionService.transferFunds(sourceAccount, targetAccount, accountService.currentBalanceOf(sourceAccount).getAmountPence(), "prisoner transfer");
            accountService.closeAccount(sourceAccount);
        }
    }


    @Transactional
    public void transferPrisonerAccounts(String prisonerId, String fromEstablishmentId, String toEstablishmentId) throws InsufficientFundsException, AccountClosedException, DebitNotSupportedException {
        List<Account> accounts = accountService.prisonerOpenAccounts(fromEstablishmentId, prisonerId);
        PrisonerTransfer prisonerTransfer = prisonerTransferRepository.save(PrisonerTransfer.builder()
                .fromEstablishmentId(fromEstablishmentId)
                .toEstablishmentId(toEstablishmentId)
                .prisonerId(prisonerId)
                .build());
        transferAccountsToEstablishmentId(accounts, toEstablishmentId, prisonerTransfer);
        prisonerTransferRepository.save(prisonerTransfer.toBuilder().accountsTransferDateTime(ZonedDateTime.now(ZoneOffset.UTC)).build());

    }

    private List<PrisonerTransfer> getPrisonerTransfersToEstablishmentId(String establishmentId, Optional<ZonedDateTime> from, Optional<ZonedDateTime> to) {

        if (from.isPresent() && to.isPresent()) {
            return prisonerTransferRepository.findAllByToEstablishmentIdAndAccountsTransferDateTimeBetween(establishmentId, from, to);
        } else if (from.isPresent() && !to.isPresent()) {
            return prisonerTransferRepository.findAllByToEstablishmentIdAndAccountsTransferDateTimeGreaterThanEqual(establishmentId, from);
        } else if (!from.isPresent() && to.isPresent()) {
            return prisonerTransferRepository.findAllByToEstablishmentIdAndAccountsTransferDateTimeLessThanEqual(establishmentId, to);
        } else {
            return prisonerTransferRepository.findAllByToEstablishmentId(establishmentId);
        }
    }

    private List<PrisonerTransfer> getPrisonerTransfersFromEstablishmentId(String establishmentId, Optional<ZonedDateTime> from, Optional<ZonedDateTime> to) {

        if (from.isPresent() && to.isPresent()) {
            return prisonerTransferRepository.findAllByFromEstablishmentIdAndAccountsTransferDateTimeBetween(establishmentId, from, to);
        } else if (from.isPresent() && !to.isPresent()) {
            return prisonerTransferRepository.findAllByFromEstablishmentIdAndAccountsTransferDateTimeGreaterThanEqual(establishmentId, from);
        } else if (!from.isPresent() && to.isPresent()) {
            return prisonerTransferRepository.findAllByFromEstablishmentIdAndAccountsTransferDateTimeLessThanEqual(establishmentId, to);
        } else {
            return prisonerTransferRepository.findAllByFromEstablishmentId(establishmentId);
        }
    }

    public EstablishmentTransferSummary prisonerTransferAccountsSummary(String establishmentId, Optional<ZonedDateTime> from, Optional<ZonedDateTime> to) {
        List<PrisonerTransfer> transfersIn = getPrisonerTransfersToEstablishmentId(establishmentId, from, to);
        List<PrisonerTransfer> transfersOut = getPrisonerTransfersFromEstablishmentId(establishmentId, from, to);

        List<Account> accountsIn = accountService.accountsForPrisonerTransfers(transfersIn);
        List<Account> accountsOut = accountService.accountsForPrisonerTransfers(transfersOut);

        Map<String, List<Account>> accountsInGroupedByEstablishment = accountsIn.stream().collect(Collectors.groupingBy(acc -> acc.getPrisonerTransfer().getFromEstablishmentId(), Collectors.toList()));
        Map<String, List<Account>> accountsOutGroupedByEstablishment = accountsOut.stream().collect(Collectors.groupingBy(acc -> acc.getPrisonerTransfer().getToEstablishmentId(), Collectors.toList()));

        List<TransferIn> transferInList = accountsInGroupedByEstablishment.keySet()
                .stream()
                .map(estId ->
                        TransferIn.builder()
                                .prisonerIds(accountsInGroupedByEstablishment.get(estId).stream().map(Account::getPrisonerId).collect(Collectors.toSet()))
                                .amountToRequestPence(accountsInGroupedByEstablishment.get(estId)
                                        .stream()
                                        .mapToLong(
                                                account -> accountService.balanceAsOf(account, Optional.of(account.getPrisonerTransfer().getAccountsTransferDateTime())).getAmountPence())
                                        .sum())
                                .fromEstablishmentId(estId)
                                .build())
                .collect(Collectors.toList());


        List<TransferOut> transferOutList = accountsOutGroupedByEstablishment.keySet()
                .stream()
                .map(estId ->
                        TransferOut.builder()
                                .prisonerIds(accountsOutGroupedByEstablishment.get(estId).stream().map(Account::getPrisonerId).collect(Collectors.toSet()))
                                .amountToTransferPence(accountsOutGroupedByEstablishment.get(estId)
                                        .stream()
                                        .mapToLong(
                                                account -> accountService.balanceAsOf(account, Optional.of(account.getPrisonerTransfer().getAccountsTransferDateTime())).getAmountPence())
                                        .sum())
                                .toEstablishmentId(estId)
                                .build())
                .collect(Collectors.toList());

        return EstablishmentTransferSummary.builder()
                .transferredIn(transferInList)
                .transferredOut(transferOutList)
                .build();


    }

}
