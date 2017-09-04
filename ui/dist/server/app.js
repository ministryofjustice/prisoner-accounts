const express = require('express');
const bunyanMiddleware = require('bunyan-middleware');
const expressNunjucks = require('express-nunjucks');
const helmet = require('helmet');
const bodyParser = require('body-parser');
const path = require('path');
const favicon = require('serve-favicon');

const healthController = require('./health');
const requireAuth = require('./auth');
// const serveDocs = require('./docs');

const errors = require('./errors');

// eslint-disable-next-line no-unused-vars
const formatDate = (str, format) => str /* moment(str).format(format) */;
const slugify = (str) => str.replace(/[.,-\/#!$%\^&\*;:{}=\-_`~()’]/g,"").replace(/ +/g,'_').toLowerCase();
const htmlLog = (nunjucksSafe) => (a) => nunjucksSafe('<script>console.log(' + JSON.stringify(a, null, '\t') + ');</script>');

function setupBaseMiddleware(app, logger, db) {
  app.use(bunyanMiddleware({
    logger: logger,
    obscureHeaders: ['Authorization'],
  }));

  app.use(function detectAzureSSL(req, res, next) {
    if (!req.get('x-forwarded-proto') && req.get('x-arr-ssl')) {
      req.headers['x-forwarded-proto'] = 'https';
    }
    return next();
  });

  app.set('json spaces', 2);
  app.set('trust proxy', true);

  app.use(helmet());
  app.use(bodyParser.urlencoded({ extended: true }));

  app.use(function injectDatabase(req, res, next) {
    req.db = db;
    return next();
  });
}

function setupViewEngine (app) {
  let config = app.locals.config;

  app.set('view engine', 'html');
  app.set('views', [
    path.join(__dirname, '../app/views/'),
    path.join(__dirname, '../lib/')
  ]);

  var nunjucks = expressNunjucks(app, {
      autoescape: true,
      watch: config.dev
  });

  nunjucks.env.addFilter('slugify', slugify);
  nunjucks.env.addFilter('formatDate', formatDate);
  nunjucks.env.addFilter('log', htmlLog(nunjucks.env.getFilter('safe')));

  return app;
}

function setupStaticAssets (app, logger) {
  let config = app.locals.config;

  app.use(favicon(path.join(__dirname, '../node_modules/govuk_template_mustache/assets/images/favicon.ico')));

  if (config.dev) {
    var webpack = require('webpack');
    var webpackDevMiddleware = require('webpack-dev-middleware');
    var webpackConfig = require('../webpack.config');

    var compiler = webpack(webpackConfig);
    app.use(webpackDevMiddleware(compiler, {
      publicPath: webpackConfig.output.publicPath
    }));
    logger.info('Webpack compilation enabled');

    var chokidar = require('chokidar');
    // eslint-disable-next-line no-unused-vars
    chokidar.watch('./app', { ignoreInitial: true }).on('all', (event , path) => {
      logger.info("Clearing /app/ module cache from server");
      Object.keys(require.cache).forEach(function(id) {
        if (/[\/\\]app[\/\\]/.test(id)) delete require.cache[id];
      });
    });
  }

  // Middleware to serve static assets
  [
    '/public',
    '/app/assets',
    '/node_modules/govuk_template_mustache/assets',
    '/node_modules/govuk_frontend_toolkit'
  ].forEach((folder) => {
    app.use('/public', express.static(path.join(__dirname, '../', folder)));
  });

  // send assetPath to all views
  app.use(function (req, res, next) {
    res.locals.asset_path = "/public/";
    next();
  });

  return app;
}

function setupAppRoutes(app, log) {
  let config = app.locals.config;

  app.get('/health', healthController.health);

  const authMiddleware = requireAuth(config.auth, log);
  if (authMiddleware) app.use(authMiddleware);

//app.use(serveDocs());

  app.use('/', require('../app/routes'));

  app.use(function notFoundHandler(req, res) {
    errors.notFound(res, 'No handler exists for this url');
  });

  // eslint-disable-next-line no-unused-vars
  app.use(function errorHandler(err, req, res, next) {
    req.log.warn(err);
    errors.unexpected(res, err);
  });
}

module.exports = (config, log, db) => {
  const app = express();
  app.locals.config = config;

  setupBaseMiddleware(app, log, db);

  setupViewEngine(app, log);
  setupStaticAssets(app, log);

  setupAppRoutes(app, log);

  return app;
};
