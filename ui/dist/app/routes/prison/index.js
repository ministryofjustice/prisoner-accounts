var express = require('express');
var helpers = require('../../helpers');
var prison = require('../../datasources/prison');
var prisoner = require('../../datasources/prisoner');
var reporting = require('../../datasources/reporting');
var account = require('../../datasources/account');

var router = new express.Router();

// private

const renderPrison = (res) => helpers.format(res, 'prison');
const renderPrisonList = (res) => helpers.format(res, 'prisons');

const createPrisonViewModel = (req) => (data) =>
  ({
    prison_id: req.params.prison_id,
    prison: data.prison,
    prisoners: data.prisoners,
    transfers: data.transfers,
    accounts: data.accounts,
  });

const createPrisonListViewModel = (/* req */) => (data) =>
  ({
    prisons: data,
  });

const listPrisons = (req, res, next) =>
  prison.listPrisons()
    .then(createPrisonListViewModel(req))
    .then(renderPrisonList(res))
    .catch(helpers.failWithError(res, next));

const displayPrison = (req, res, next) =>
  Promise.all([
    prison.getPrison(req.params.prison_id),
    prisoner.findPrisoners(req.params.prison_id),
    reporting.getPrisonTransfersReport(req.params.prison_id),
    account.getPrisonAccounts(req.params.prison_id),
  ])
    .then((data) => ({ prison: data[0], prisoners: data[1], transfers: data[2], accounts: data[3] }))
    .then(createPrisonViewModel(req))
    .then(renderPrison(res))
    .catch(helpers.failWithError(res, next));

// public

router.get('/', listPrisons);
router.get('/:prison_id', displayPrison);

// exports

module.exports = router;
