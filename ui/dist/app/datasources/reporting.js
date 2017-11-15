var helpers = require('../helpers');
var Client = require('node-rest-client').Client;

var client = new Client();

const accountServiceDomain = () => 'http://' + process.env.ACCOUNT_SERVICE_HOST + ':' + process.env.ACCOUNT_SERVICE_PORT;
const accountServiceUri = (path) => accountServiceDomain() + path;
const accountReportingServiceUri = (path) => accountServiceUri('/reporting' + path);

client.registerMethod('getPrisonAccountsReport', accountReportingServiceUri('/establishments/${prison_id}/prisoners/accounts'), 'GET');
client.registerMethod('getPrisonTransfersReport', accountReportingServiceUri('/establishments/${prison_id}/prisonertransfers'), 'GET');

// private

const getPrisonAccountsReport = (prison_id,  atDateTime) =>
  new Promise((res, rej) => client.methods.getPrisonAccountsReport(
    {
      path: { prison_id: prison_id },
      parameters: { atDateTime: atDateTime },
    },
    (data) => (data.status < 200 || data.status > 299) ? rej(data) : res(helpers.inspect(data))
  ).on('error', (err) => rej(err)));

const getPrisonTransfersReport = (prison_id) =>
  new Promise((res, rej) => client.methods.getPrisonTransfersReport(
    {
      path: { prison_id: prison_id }
    },
    (data) => (data.status < 200 || data.status > 299) ? rej(data) : res(helpers.inspect(data))
  ).on('error', (err) => rej(err)));

// exports

module.exports.getPrisonAccountsReport = getPrisonAccountsReport;
module.exports.getPrisonTransfersReport = getPrisonTransfersReport;
