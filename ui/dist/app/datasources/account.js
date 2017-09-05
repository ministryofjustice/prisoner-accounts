var helpers = require('../helpers');
var Client = require('node-rest-client').Client;

var client = new Client();

const serviceDomain = () => 'http://' + process.env.ACCOUNT_SERVICE_HOST + ':' + process.env.ACCOUNT_SERVICE_PORT;
const serviceUri = (path) => serviceDomain() + path;
const prisonServiceUri = (path) => serviceUri('/prisoneraccounts/establishments/${prison_id}' + (path ? path: ''));
const prisonerServiceUri = (path) => serviceUri('/prisoneraccounts/establishments/${prison_id}/prisoners/${prisoner_id}' + (path ? path: ''));
const accountServiceUri = (path) => serviceUri('/prisoneraccounts/establishments/${prison_id}/prisoners/${prisoner_id}/accounts/${account_name}' + (path ? path: ''));

// prison_id
client.registerMethod('getPrisonAccounts', prisonServiceUri('/prisoners/accounts'), 'GET');

// prison_id, prisoner_id
client.registerMethod('getPrisonerAccountsSummary', prisonerServiceUri('/accounts'), 'GET');
client.registerMethod('transferBetweenPrisonerAccounts', prisonerServiceUri('/accounts/transfer'), 'POST');
client.registerMethod('transferPrisonerAccountsBetweenEstablishments', prisonerServiceUri('/transfer'), 'POST');

// prison_id, prisoner_id, account_name
client.registerMethod('getBalance', accountServiceUri('/balance'), 'GET');
client.registerMethod('getPrisonerTransactionsByEstablishment', accountServiceUri('/transactions'), 'GET');
client.registerMethod('ledgerEntryCashAccount', accountServiceUri(), 'PUT');

// prisoner_id, account_name
client.registerMethod('getPrisonerTransactions', serviceUri('/prisoneraccounts/prisoners/${prisoner_id}/accounts/${account_name}/transactions'), 'GET');


const callPrisonMethod = (method) => (prison_id) =>
  new Promise((res, rej) => method(
    {
      path: { prison_id: prison_id },
      headers: { 'Content-Type': 'application/json' },
    },
    (data) => (data.status < 200 || data.status > 299) ? rej(data) : res(data)
  ).on('error', (err) => rej(err)));

const callPrisonerMethod = (method) => (prison_id, prisoner_id) =>
  new Promise((res, rej) => method(
    {
      path: { prison_id: prison_id, prisoner_id: prisoner_id },
      headers: { 'Content-Type': 'application/json' },
    },
    (data) => (data.status < 200 || data.status > 299) ? rej(data) : res(data)
  ).on('error', (err) => rej(err)));

const callAccountMethod = (method) => (prison_id, prisoner_id, account_name) =>
  new Promise((res, rej) => method(
    {
      path: { prison_id: prison_id, prisoner_id: prisoner_id, account_name: account_name },
      headers: { 'Content-Type': 'application/json' },
    },
    (data) => (data.status < 200 || data.status > 299) ? rej(data) : res(data)
  ).on('error', (err) => rej(err)));

const callPrisonerAccountMethod = (method) => (prisoner_id, account_name) =>
  new Promise((res, rej) => method(
    {
      path: helpers.inspect({ prisoner_id: prisoner_id, account_name: account_name }),
      headers: { 'Content-Type': 'application/json' },
    },
    (data) => (data.status < 200 || data.status > 299) ? rej(data) : res(helpers.inspect(data))
  ).on('error', (err) => rej(err)));

// private

const getPrisonAccounts =
  callPrisonMethod(client.methods.getPrisonAccounts);

const getPrisonerAccountsSummary =
  callPrisonerMethod(client.methods.getPrisonerAccountsSummary);

const transferBetweenPrisonerAccounts = (prison_id, prisoner_id, body) =>
  new Promise((res, rej) => client.methods.transferBetweenPrisonerAccounts(
    {
      path: { prison_id: prison_id, prisoner_id: prisoner_id },
      data: helpers.inspect(body),
      headers: { 'Content-Type': 'application/json' },
    },
    (data) => (data.status < 200 || data.status > 299) ? rej(data) : res(helpers.inspect(data))
  ).on('error', (err) => rej(err)));

const transferPrisonerAccountsBetweenEstablishments = (prison_id, prisoner_id, body) =>
  new Promise((res, rej) => client.methods.transferPrisonerAccountsBetweenEstablishments(
    {
      path: { prison_id: prison_id, prisoner_id: prisoner_id },
      parameters: helpers.inspect(body),
      headers: { 'Content-Type': 'application/json' },
    },
    (data) => (data.status < 200 || data.status > 299) ? rej(data) : res(helpers.inspect(data))
  ).on('error', (err) => rej(err)));

const getBalance =
  callAccountMethod(client.methods.getBalance);

const getPrisonerTransactions =
  callPrisonerAccountMethod(client.methods.getPrisonerTransactions);

const ledgerEntryCashAccount = (prison_id, prisoner_id, account_name, body) =>
  new Promise((res, rej) => client.methods.ledgerEntryCashAccount(
    {
      path: { prison_id: prison_id, prisoner_id: prisoner_id, account_name: account_name },
      data: helpers.inspect(body),
      headers: { 'Content-Type': 'application/json' },
    },
    (data) => (data.status < 200 || data.status > 299) ? rej(data) : res(helpers.inspect(data))
  ).on('error', (err) => rej(err)));

// exports

module.exports.getPrisonAccounts = getPrisonAccounts;
module.exports.getPrisonerAccounts = getPrisonerAccountsSummary;
module.exports.transferBetweenPrisonerAccounts = transferBetweenPrisonerAccounts;
module.exports.transferPrisonerAccountsBetweenEstablishments = transferPrisonerAccountsBetweenEstablishments;
module.exports.getBalance = getBalance;
module.exports.getPrisonerTransactions = getPrisonerTransactions;
module.exports.recordTransaction = ledgerEntryCashAccount;
