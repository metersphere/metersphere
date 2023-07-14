import {registerMicroApps, start} from 'qiankun';
import {getApps} from './api/apps'
import Vue from "vue"

const getActiveRule = (hash) => (location) => location.hash.startsWith(hash);

// 添加全局事件总线
let eventBus = new Vue();
Vue.prototype.$EventBus = eventBus;

// 从网关查所有的服务
getApps()
  .then(res => {
    let apps = [], modules = {}, microPorts = {}
    res.data.forEach(svc => {
      let name = svc.serviceId;

      // 网关排除
      if (name === 'gateway') {
        return;
      }
      modules[name] = true;
      microPorts[name] = svc.port;
      apps.push({
        name,
        entry: '//127.0.0.1:' + (svc.port - 4000),
        container: '#micro-app',
        activeRule: getActiveRule('#/' + name),
        props: {
          eventBus
        }
      });
    });

    sessionStorage.setItem("micro_apps", JSON.stringify(modules));
    sessionStorage.setItem("micro_ports", JSON.stringify(microPorts));
    sessionStorage.setItem("MICRO_MODE", 'true');

    if (process.env.NODE_ENV !== 'development') {
      apps.forEach(app => {
        // 替换成后端的端口
        app.entry = app.entry.replace(/127\.0\.0\.1:\d+/g, window.location.host + "/" + app.name);
      });
    }
    //注册子应用
    registerMicroApps(apps);
    //启动
    start();
  })
  .catch(e => {
    console.error(e);
  });


