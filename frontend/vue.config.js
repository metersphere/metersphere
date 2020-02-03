module.exports = {
    productionSourceMap: true,
    configureWebpack: {
        devtool: 'source-map'
    },
    devServer: {
        proxy: {
            ['^(?!/login)']: {
                target: 'http://localhost:8081',
                ws: true,
            }
        }
    },
    pages: {
        performance: {
            entry: "src/performance/main.js",
            template: "src/performance/index.html",
            filename: "index.html"
        },
        login: {
            entry: "src/login/login.js",
            template: "src/login/login.html",
            filename: "login.html"
        }
    }
};
