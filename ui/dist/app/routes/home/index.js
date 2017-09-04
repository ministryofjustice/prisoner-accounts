var express = require('express');
var helpers = require('../../helpers');

var router = new express.Router();

// private

const renderIndex = (res) => helpers.format(res, 'index');

const createIndexViewModel = (/* req */) => (data) =>
  ({
    page_title: 'Prisoner Accounts Prototype',
    page_description: 'This is a simple interactive Web UI to illustrate capabilities of an independant Prisoner Finance Service',
  });

const displayIndex = (req, res, next) =>
  (new Promise((res, rej) => { res({}); }))
    .then(createIndexViewModel(req))
    .then(renderIndex(res))
    .catch(helpers.failWithError(res, next));

// public

router.get('/', displayIndex);

// exports

module.exports = router;
