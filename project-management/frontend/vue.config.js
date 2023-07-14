const path = require("path");
const { name } = require("./package");
const { defineConfig } = require("@vue/cli-service");
const BundleAnalyzerPlugin =
  require("webpack-bundle-analyzer").BundleAnalyzerPlugin;

function resolve(dir) {
  return path.join(__dirname, dir);
}

module.exports = defineConfig({
  publicPath: "/",
  productionSourceMap: false,
  devServer: {
    port: 4002,
    client: {
      webSocketTransport: "sockjs",
      overlay: false,
    },
    allowedHosts: "all",
    webSocketServer: "sockjs",
    proxy: {
      ["^((?!/login)(?!/document))"]: {
        target: "http://localhost:8002",
        ws: false,
      },
      "/websocket": {
        target: "http://localhost:8002",
        ws: true,
      },
    },
    // 跨域
    headers: {
      "Access-Control-Allow-Origin": "*",
    },
  },
  configureWebpack: {
    devtool: "cheap-module-source-map",
    resolve: {
      alias: {
        "@": resolve("src"),
        "vue-i18n": resolve("node_modules/vue-i18n"),
        jsondiffpatch: resolve("node_modules/jsondiffpatch"),
      },
    },
    output: {
      // 把子应用打包成 umd 库格式(必须)
      library: `${name}-[name]`,
      libraryTarget: "umd",
      chunkLoadingGlobal: `webpackJsonp_${name}`,
      // 打包后js的名称
      filename: `js/${name}-[name].[contenthash:8].js`,
      chunkFilename: `js/${name}-[name].[contenthash:8].js`,
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
            priority: 3,
            chunks: "all",
          },
          fortawesome: {
            test: /[\\/]@fortawesome[\\/]/,
            name: "fortawesome",
            priority: 3,
            chunks: "all",
          },
          "el-tree-transfer": {
            test: /[\\/]el-tree-transfer[\\/]/,
            name: "el-tree-transfer",
            priority: 3,
            chunks: "all",
          },
          jspdf: {
            test: /[\\/]jspdf[\\/]/,
            name: "jspdf",
            priority: 3,
            chunks: "all",
          },
          jsondiffpatch: {
            test: /[\\/]jsondiffpatch[\\/]/,
            name: "jsondiffpatch",
            priority: 3,
            chunks: "all",
          },
          jsencrypt: {
            test: /[\\/]jsencrypt[\\/]/,
            name: "jsencrypt",
            priority: 3,
            chunks: "all",
          },
          mockjs: {
            test: /[\\/]mockjs[\\/]/,
            name: "mockjs",
            priority: 3,
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
      chunkFilename: `css/${name}-[name].[contenthash:8].css`,
    },
  },
  chainWebpack: (config) => {
    config.module
      .rule("svg")
      .exclude.add(
        resolve("../../framework/sdk-parent/frontend/src/assets/module")
      )
      .end();
    config.module
      .rule("icons")
      .test(/\.svg$/)
      .include.add(
        resolve("../../framework/sdk-parent/frontend/src/assets/module")
      )
      .end()
      .use("svg-sprite-loader")
      .loader("svg-sprite-loader")
      .options({
        symbolId: "icon-[name]",
      });

    if (process.env.NODE_ENV === "analyze") {
      config.plugin("webpack-report").use(BundleAnalyzerPlugin, [
        {
          analyzerMode: "static",
          reportFilename: "./webpack-report.html",
          openAnalyzer: false,
        },
      ]);
    }
  },
});
