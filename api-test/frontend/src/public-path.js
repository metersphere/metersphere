// webpack打包公共文件路径
import { getApps } from 'metersphere-frontend/src/api/apps';

if (window.__POWERED_BY_QIANKUN__) {
  __webpack_public_path__ = window.__INJECTED_PUBLIC_PATH_BY_QIANKUN__;
}

if (!window.__POWERED_BY_QIANKUN__) {
  getApps().then((res) => {
    let modules = {},
      microPorts = {};
    res.data.forEach((svc) => {
      let name = svc.serviceId;
      modules[name] = true;
      microPorts[name] = svc.port;
    });
    sessionStorage.setItem('micro_apps', JSON.stringify(modules));
    sessionStorage.setItem('micro_ports', JSON.stringify(microPorts));
  });
}
