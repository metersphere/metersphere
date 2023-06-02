const path = require('path');
const {name} = require('./package');

const HtmlWebpackInlineSourcePlugin = require('html-webpack-inline-source-plugin')
const HtmlWebpackPlugin = require('html-webpack-plugin');
const BundleAnalyzerPlugin = require('webpack-bundle-analyzer').BundleAnalyzerPlugin;
const InlineSourceWebpackPlugin = require('inline-source-webpack-plugin');

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
    allowedHosts: 'all',
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
        '@': resolve('src'),
        'vue-i18n': resolve('node_modules/vue-i18n'),
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
    },
    externals: {
      vue: "Vue",
      "vue-router": "VueRouter",
      // 'echarts': 'echarts',
      // 'echarts/core': 'echarts', // TODO:外链使用的话需要改造导入及 vue-echarts 的源码
      // brace: 'brace', // TODO:暂时未发现能外链的方法，本体包未提供cdn 外链形式的包
      "element-ui": "ELEMENT",
      "mavon-editor": "MavonEditor",
      "vue-shepherd": "VueShepherd",
    },
    optimization: {
      splitChunks: {
        cacheGroups: {
          "chunk-vendors": {
            test: /[\\/]node_modules[\\/]/,
            name: "chunk-vendors",
            priority: 1,
            minChunks: 3,
            chunks: "all",
          },
          "chunk-common": {
            test: /[\\/]src[\\/]/,
            name: "chunk-common",
            priority: 1,
            minChunks: 5,
            chunks: "all",
          },
          html2canvas: {
            test: /[\\/]html2canvas[\\/]/,
            name: "html2canvas",
            priority: 2,
            chunks: "all",
          },
          fortawesome: {
            test: /[\\/]@fortawesome[\\/]/,
            name: "fortawesome",
            priority: 2,
            chunks: "all",
          },
          "el-tree-transfer": {
            test: /[\\/]el-tree-transfer[\\/]/,
            name: "el-tree-transfer",
            priority: 2,
            chunks: "all",
          },
          pinia: {
            test: /[\\/]pinia[\\/]/,
            name: "pinia",
            priority: 3,
            chunks: "all",
          },
          brace: {
            test: /[\\/]brace[\\/]/,
            name: "brace",
            priority: 3,
            chunks: "all",
          },
          echarts: {
            test: /[\\/](echarts|zrender)[\\/]/,
            name: "echarts",
            priority: 3,
            chunks: "all",
          },
          jspdf: {
            test: /[\\/]jspdf[\\/]/,
            name: "jspdf",
            priority: 2,
            chunks: "all",
          },
          jsondiffpatch: {
            test: /[\\/]jsondiffpatch[\\/]/,
            name: "jsondiffpatch",
            priority: 2,
            chunks: "all",
          },
          jsencrypt: {
            test: /[\\/]jsencrypt[\\/]/,
            name: "jsencrypt",
            priority: 2,
            chunks: "all",
          },
          mockjs: {
            test: /[\\/]mockjs[\\/]/,
            name: "mockjs",
            priority: 2,
            chunks: "all",
          },
          pdfjs: {
            test: /[\\/]pdfjs-dist[\\/]/,
            name: "pdfjs",
            priority: 2,
            chunks: "all",
          },
          ckeditor: {
            test: /[\\/]@ckeditor[\\/]/,
            name: "ckeditor",
            priority: 2,
            chunks: "all",
          },
          minderEditor: {
            test: /[\\/]vue-minder-editor-plus[\\/]/,
            name: "minderEditor",
            priority: 2,
            chunks: "all",
          },
          lodash: {
            test: /[\\/]lodash[\\/]/,
            name: "lodash",
            priority: 2,
            chunks: "all",
          },
        },
      },
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
    config.plugin('inline-source-html')
      .after('html-planReport')
      .use(new InlineSourceWebpackPlugin({
        compress: true,
        rootpath: '../../framework/sdk-parent/frontend/public/js',
        noAssetMatch: 'warn'
      }), [HtmlWebpackPlugin]);

    config.plugin('inline-source-html-planReport')
      .after('html-planReport')
      .use(HtmlWebpackInlineSourcePlugin, [HtmlWebpackPlugin]);

    if (process.env.NODE_ENV === 'analyze') {
      config.plugin('webpack-report').use(BundleAnalyzerPlugin, [
        {
          analyzerMode: 'static',
          reportFilename: './webpack-report.html',
          openAnalyzer: false,
        },
      ]);
    }
  }
};
