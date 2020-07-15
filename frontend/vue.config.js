module.exports = {
  productionSourceMap: true,
  configureWebpack: {
    devtool: 'source-map'
  },
  devServer: {
    port: 8080,
    proxy: {
      ['^(?!/login)']: {
        target: 'http://localhost:8081',
        ws: true,
      }
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
    }
  }
};
