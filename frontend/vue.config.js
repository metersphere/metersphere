const path = require('path');
const HtmlWebpackInlineSourcePlugin = require('html-webpack-inline-source-plugin')

function resolve(dir) {
  return path.join(__dirname, dir);
}

module.exports = {
  productionSourceMap: false,
  devServer: {
    port: 8080,
    proxy: {
      //1.8需求：增加分享功能，不登陆即可看到文档页面。所以代理设置增加了(?!/document)文档页面的相关信息
      // ['^(?!/login)']: {
      ['^((?!/login)(?!/document))']: {
        target: 'https://localhost:8443',
        ws: true,
      },
    }
  },
  pages: {
    business: {
      entry: "src/business/main.js",
      template: "src/business/index.html",
      filename: "index.html"
    },
    login: {
      entry: "src/login/login.js",
      template: "src/login/login.html",
      filename: "login.html"
    },
    document: {
      entry: "src/document/document.js",
      template: "src/document/document.html",
      filename: "document.html",
    },
    sharePlanReport: {
      entry: "src/template/report/plan/share/share-plan-report.js",
      template: "src/template/report/plan/share/share-plan-report.html",
      filename: "share-plan-report.html",
    },
    planReport: {
      entry: "src/template/report/plan/plan-report.js",
      template: "src/template/report/plan/plan-report.html",
      filename: "plan-report.html",
    },
  },
  configureWebpack: {
    devtool: 'source-map',
    resolve: {
      alias: {
        '@': resolve('src')
      }
    },
  },
  chainWebpack: config => {
    // 报告模板打包成一个html
    config.plugin('html-planReport')
      .tap(args => {
        args[0].inlineSource = '.(js|css)$';
        return args;
      });
    config.plugin('inline-source-html-planReport')
        .after('html-planReport')
        .use(HtmlWebpackInlineSourcePlugin);

    config.plugins.delete('prefetch');
  }
};
