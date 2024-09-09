const path = require("path");
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
    port: 3000,
    client: {
      webSocketTransport: "sockjs",
      overlay: false,
    },
    webSocketServer: "sockjs",
    allowedHosts: "all",
    proxy: {
      ["^((?!/login)(?!/document))"]: {
        target: "http://localhost:8000",
        ws: false,
      },
      "/websocket": {
        target: "http://localhost:8000",
        ws: true,
      },
    },
  },
  configureWebpack: {
    devtool: "cheap-module-source-map",
    resolve: {
      alias: {
        "@": resolve("src"),
      },
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
          jspdf: {
            test: /[\\/]jspdf[\\/]/,
            name: "jspdf",
            priority: 3,
            chunks: "all",
          },
          jsencrypt: {
            test: /[\\/]jsencrypt[\\/]/,
            name: "jsencrypt",
            priority: 3,
            chunks: "all",
          },
          pinia: {
            test: /[\\/]pinia[\\/]/,
            name: "pinia",
            priority: 3,
            chunks: "all",
          },
        },
      },
    },
  },
  chainWebpack: (config) => {
    config.devtool("source-map");
    config.resolve.alias.set("@", resolve("./src"));
    config.output.library("MsFrontend");

    config.module.rule("svg").exclude.add(resolve("src/assets/module")).end();
    config.module
      .rule("icons")
      .test(/\.svg$/)
      .include.add(resolve("src/assets/module"))
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
