const path = require('path');

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
        target: 'http://localhost:8081',
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
      filename: "document.html"
    }
  },
  configureWebpack: {
    devtool: 'source-map',
    resolve: {
      alias: {
        '@': resolve('src')
      }
    }
  },
  chainWebpack(config) {
    config.plugins.delete('prefetch');
  }
};
