const path = require('path');
const name = 'gateway';

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
    output: {
      // 把子应用打包成 umd 库格式(必须)
      library: `${name}-[name]`,
      libraryTarget: 'umd',
      chunkLoadingGlobal: `webpackJsonp_${name}`,
      // 打包后js的名称
      filename: `js/${name}-[name].[contenthash:8].js`,
      chunkFilename: `js/${name}-[name].[contenthash:8].js`,
    },
  },
  css: {
    // 将组件内的 CSS 提取到一个单独的 CSS 文件 (只用在生产环境中)
    // 也可以是一个传递给 `extract-text-webpack-plugin` 的选项对象
    // 修改打包后css文件名
    // extract: true,
    extract: {
      ignoreOrder: true,
      filename: `css/${name}-[name].[contenthash:8].css`,
      chunkFilename: `css/${name}-[name].[contenthash:8].css`
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
