var express = require('express');
var helpers = require('../../helpers');
var prison = require('../../datasources/prison');
var prisoner = require('../../datasources/prisoner');
var account = require('../../datasources/account');

var router = new express.Router();

// private

const renderPrisoner = (res) => helpers.format(res, 'prisoner');
const renderPrisonerList = (res) => helpers.format(res, 'prisoners');

const createPrisonerViewModel = (req) => (data) =>
  ({
    prisoner_id: req.params.prisoner_id,
    prisoner: data.prisoner,
    prison: data.prison,
    accounts: data.accounts,
    prisons: data.prisons,
  });

const createPrisonerListViewModel = (/* req */) => (data) =>
  ({
    prisoners: data,
  });

const listPrisoners = (req, res, next) =>
  prisoner.listPrisoners()
    .then(createPrisonerListViewModel(req))
    .then(renderPrisonerList(res))
    .catch(helpers.failWithError(res, next));

const displayPrisoner = (req, res, next) =>
  prisoner.getPrisoner(req.params.prisoner_id)
    .then((data) => Promise.all([
      data,
      prison.getPrison(data.prison),
      account.getPrisonerAccounts(data.prison, data.prisoner_id),
      prison.listPrisons(),
    ]))
    .then((data) => ({ prisoner: data[0], prison: data[1], accounts: data[2], prisons: data[3] }))
    .then(createPrisonerViewModel(req))
    .then(renderPrisoner(res))
    .catch(helpers.failWithError(res, next));

const processTransaction = (op) => (req, res, next) =>
  prisoner.getPrisoner(req.params.prisoner_id)
    .then((data) => {
      return account.recordTransaction(data.prison, data.prisoner_id, req.body.account_name, {
        amountPence: req.body.amount,
        operation: op,
        clientRef: req.body.reference,
        description: req.body.description,
      });
    })
    .then(helpers.redirect(res, '/prisoner/' + req.params.prisoner_id))
    .catch(helpers.failWithError(res, next));

const processTransfer = (req, res, next) =>
  prisoner.getPrisoner(req.params.prisoner_id)
    .then((data) => {
      return account.transferBetweenPrisonerAccounts(data.prison, data.prisoner_id, {
        amountPence: req.body.amount,
        fromAccountName: req.body.from,
        toAccountName: req.body.to,
      });
    })
    .then(helpers.redirect(res, '/prisoner/' + req.params.prisoner_id))
    .catch(helpers.failWithError(res, next));

const processPrisonerMovement = (req, res, next) =>
  prisoner.getPrisoner(req.params.prisoner_id)
    .then((data) => Promise.all([
      account.transferPrisonerAccountsBetweenEstablishments(data.prison, data.prisoner_id, {
        toEstablishmentId: req.body.to,
      }),
      prisoner.movePrisoner(data.prisoner_id, req.body.to)
    ]))
    .then(helpers.redirect(res, '/prisoner/' + req.params.prisoner_id))
    .catch(helpers.failWithError(res, next));

// public

router.get('/', listPrisoners);
router.get('/:prisoner_id', displayPrisoner);
router.post('/:prisoner_id/credit', processTransaction('CREDIT'));
router.post('/:prisoner_id/debit', processTransaction('DEBIT'));
router.post('/:prisoner_id/transfer', processTransfer);
router.post('/:prisoner_id/move', processPrisonerMovement);

// exports

module.exports = router;
