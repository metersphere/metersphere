const path = require('path');
const {name} = require('./package');

function resolve(dir) {
  return path.join(__dirname, dir);
}

module.exports = {
  productionSourceMap: false,
  devServer: {
    port: 3000,
    client: {
      webSocketTransport: 'sockjs',
    },
    webSocketServer: 'sockjs',
    allowedHosts: 'all',
    proxy: {
      ['^((?!/login)(?!/document))']: {
        target: 'http://localhost:8000',
        ws: false
      },
      '/websocket': {
        target: 'http://localhost:8000',
        ws: true
      },
    },
  },
  configureWebpack: {
    devtool: 'cheap-module-source-map',
    resolve: {
      alias: {
        '@': resolve('src')
      }
    },
  },
  chainWebpack: config => {
    config.devtool('source-map')
    config.resolve.alias.set('@', resolve('./src'))
    config.output.library("MsFrontend")

    config.module
      .rule('svg')
      .exclude.add(resolve('src/assets/module'))
      .end()
    config.module
      .rule('icons')
      .test(/\.svg$/)
      .include.add(resolve('src/assets/module'))
      .end()
      .use('svg-sprite-loader')
      .loader('svg-sprite-loader')
      .options({
        symbolId: 'icon-[name]'
      })
  }
};
