var helpers = require('../helpers');

const prisoners = {
  A1234BC: {
    prisoner_id: 'A1234BC',
    full_name: 'Matt Smith',
    prison: 'LPI',
  },
  X7890YZ: {
    prisoner_id: 'X7890YZ',
    full_name: 'Michael Jackson',
    prison: 'LPI',
  },
  C4321BA: {
    prisoner_id: 'C4321BA',
    full_name: 'Glen Mailer',
    prison: 'BWI',
  },
  Z0987YX: {
    prisoner_id: 'Z0987YX',
    full_name: 'Himal Mandalia',
    prison: 'NMI',
  },
};

// exports

const listPrisoners = () =>
  new Promise((res, rej) => res(prisoners))
        .then(helpers.objToList);

const listPrisonersByPrison = (id) =>
  new Promise((res, rej) => res(prisoners))
      .then(helpers.objToFilteredList((x) => x.prison === id));

const getPrisoner = (id) =>
  new Promise((res, rej) => res(helpers.pick(prisoners, id)));

const movePrisoner = (id, prison) =>
  new Promise((res, rej) => {
    prisoners[id].prison = prison;
    return res(helpers.pick(prisoners, id));
  });

// exports

module.exports.listPrisoners = listPrisoners;
module.exports.findPrisoners = listPrisonersByPrison;
module.exports.getPrisoner = getPrisoner;
module.exports.movePrisoner = movePrisoner;
