var express = require('express');
var helpers = require('../../helpers');
var prisoner = require('../../datasources/prisoner');
var account = require('../../datasources/account');

var router = new express.Router();

const includeBalance = (balance) => (x) => ({
  amountPence: x.amountPence,
  clientReference: x.clientReference,
  description: x.description,
  transactionDateTime: x.transactionDateTime,
  transactionId: x.transactionId,
  transactionType: x.transactionType,
  balance: (balance = balance + x.amountPence),
});

// private

const renderAccount = (res) => helpers.format(res, 'account');

const createAccountViewModel = (req) => (data) =>
  ({
    prisoner_id: req.params.account_id,
    account_name: req.params.account_name,
    prisoner: data.prisoner,
    transactions: data.transactions.map(includeBalance(0)).reverse(),
  });

const displayAccount = (req, res, next) =>
  prisoner.getPrisoner(req.params.prisoner_id)
    .then((data) => Promise.all([
      data,
      account.getPrisonerTransactions(data.prisoner_id, req.params.account_name),
    ]))
    .then((data) => ({ prisoner: data[0], transactions: data[1] }))
    .then(createAccountViewModel(req))
    .then(renderAccount(res))
    .catch(helpers.failWithError(res, next));

// public

router.get('/:prisoner_id/:account_name', displayAccount);

// exports

module.exports = router;
