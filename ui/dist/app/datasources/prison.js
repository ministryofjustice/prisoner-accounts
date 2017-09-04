var helpers = require('../helpers');

const prisons = {
  LPI: {
    prison_id: 'LPI',
    name: 'HM Prison Liverpool',
  },
  NMI: {
    prison_id: 'NMI',
    name: 'HM Prison Nottingham',
  },
  BWI: {
    prison_id: 'BWI',
    name: 'HM Prison Berwyn',
  },
  EWI: {
    prison_id: 'EWI',
    name: 'HM Prison Eastwood Park',
  },
};

const listPrisons = () =>
  new Promise((res /*, reject */) => res(prisons)).then(helpers.objToList);

const getPrison = (id) =>
  new Promise((res /*, reject */) => res(helpers.pick(prisons, id)));

// exports

module.exports.listPrisons = listPrisons;
module.exports.getPrison = getPrison;
