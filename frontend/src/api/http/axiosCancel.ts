import axios from 'axios';

import { isFunction } from '@/utils/is';

import type { AxiosRequestConfig, Canceler } from 'axios';

let pendingMap = new Map<string, Canceler>();

export const getPendingUrl = (config: AxiosRequestConfig) => [config.method, config.url].join('&');

export class AxiosCanceler {
  /**
   * 添加请求
   * @param {Object} config
   */
  addPending(config: AxiosRequestConfig) {
    this.removePending(config);
    const url = getPendingUrl(config);
    config.cancelToken =
      config.cancelToken ||
      new axios.CancelToken((cancel) => {
        if (!pendingMap.has(url)) {
          // 非重复请求，存入pending中
          pendingMap.set(url, cancel);
        }
      });
  }

  /**
   * @description: 清理全部pending中的请求
   */
  removeAllPending() {
    pendingMap.forEach((cancel) => {
      if (cancel && isFunction(cancel)) {
        cancel();
      }
    });
    pendingMap.clear();
  }

  /**
   * 取消并移除指定请求
   * @param {Object} config
   */
  removePending(config: AxiosRequestConfig) {
    const url = getPendingUrl(config);

    if (pendingMap.has(url)) {
      // 根据标识找到pending中对应的请求并取消
      const cancel = pendingMap.get(url);
      if (cancel && isFunction(cancel)) {
        cancel(url);
      }
      pendingMap.delete(url);
    }
  }

  /**
   * @description: 重置pending列表
   */
  static reset(): void {
    pendingMap = new Map<string, Canceler>();
  }
}
