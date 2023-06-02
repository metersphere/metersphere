/**
 * 单独监听路由会浪费渲染性能。使用发布订阅模式去进行分发管理。
 */
import mitt, { Handler } from 'mitt';
import type { RouteLocationNormalized } from 'vue-router';

const emitter = mitt();

const key = Symbol('ROUTE_CHANGE');

let latestRoute: RouteLocationNormalized;

/**
 * 设置路由监听
 * @param to 要跳转的路由信息
 */
export function setRouteEmitter(to: RouteLocationNormalized) {
  emitter.emit(key, to);
  latestRoute = to;
}

/**
 * 监听路由变化
 * @param handler 处理回调
 * @param immediate 是否立即执行
 */
export function listenerRouteChange(handler: (route: RouteLocationNormalized) => void, immediate = true) {
  emitter.on(key, handler as Handler);
  if (immediate && latestRoute) {
    handler(latestRoute);
  }
}

/**
 * 移除路由监听
 */
export function removeRouteListener() {
  emitter.off(key);
}
