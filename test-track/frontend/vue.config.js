const path = require('path');
const {name} = require('./package');

const HtmlWebpackInlineSourcePlugin = require('html-webpack-inline-source-plugin')
const HtmlWebpackPlugin = require('html-webpack-plugin');

function resolve(dir) {
  return path.join(__dirname, dir);
}

module.exports = {
  productionSourceMap: false,
  devServer: {
    port: 4005,
    client: {
      webSocketTransport: 'sockjs',
    },
    webSocketServer: 'sockjs',
    proxy: {
      ['^((?!/login)(?!/document))']: {
        target: 'http://localhost:8005',
        ws: false
      },
      '/websocket': {
        target: 'http://localhost:8005',
        ws: true
      },
    },
    // 跨域
    headers: {
      'Access-Control-Allow-Origin': '*',
    },
  },
  pages: {
    business: {
      entry: "src/main.js",
      template: "public/index.html",
      filename: "index.html"
    },
    sharePlanReport: {
      entry: "src/business/template/report/plan/share/share-plan-report.js",
      template: "src/business/template/report/plan/share/share-plan-report.html",
      filename: "share-plan-report.html",
    },
    planReport: { //这个配置要放最后，不然会导致测试计划导出报告没有将css和js引入html，原因没具体研究
      entry: "src/business/template/report/plan/plan-report.js",
      template: "src/business/template/report/plan/plan-report.html",
      filename: "plan-report.html",
      inlineSource: '.*'
    }
  },
  configureWebpack: {
    devtool: 'cheap-module-source-map',
    resolve: {
      alias: {
        '@': resolve('src')
      },
      fallback: {"stream": require.resolve("stream-browserify")}
    },
    output: {
      // 把子应用打包成 umd 库格式(必须)
      library: `${name}-[name]`,
      libraryTarget: 'umd',
      chunkLoadingGlobal: `webpackJsonp_${name}`,
      // 打包后js的名称
      filename: `js/${name}-[name].[contenthash:8].js`,
      chunkFilename: `js/${name}-[name].[contenthash:8].js`,
    }
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
    config.module
      .rule('svg')
      .exclude.add(resolve('../../framework/sdk-parent/frontend/src/assets/module'))
      .end()
    config.module
      .rule('icons')
      .test(/\.svg$/)
      .include.add(resolve('../../framework/sdk-parent/frontend/src/assets/module'))
      .end()
      .use('svg-sprite-loader')
      .loader('svg-sprite-loader')
      .options({
        symbolId: 'icon-[name]'
      })

    // 报告模板打包成一个html
    config.plugin('inline-source-html-planReport')
      .after('html-planReport')
      .use(HtmlWebpackInlineSourcePlugin, [HtmlWebpackPlugin]);

    config.plugins.delete('prefetch');
  }
};
