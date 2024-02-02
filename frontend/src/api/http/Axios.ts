import { cloneDeep } from 'lodash-es';
import axios from 'axios';

import { isFunction } from '@/utils/is';

import { ContentTypeEnum } from '@/enums/httpEnum';

import { AxiosCanceler } from './axiosCancel';
import type { CreateAxiosOptions } from './axiosTransform';
import type { RequestOptions, Result, UploadFileParams } from '#/axios';
import type { AxiosError, AxiosInstance, AxiosRequestConfig, AxiosResponse, InternalAxiosRequestConfig } from 'axios';

export * from './axiosTransform';

/**
 * @description:  封装axios请求，返回重新封装的数据格式
 */
export class MSAxios {
  public axiosInstance: AxiosInstance;

  private readonly options: CreateAxiosOptions;

  constructor(options: CreateAxiosOptions) {
    this.options = options;
    this.axiosInstance = axios.create(options);
    this.setupInterceptors();
  }

  private getTransform() {
    const { transform } = this.options;
    return transform;
  }

  /**
   * @description: 拦截器配置
   */
  private setupInterceptors() {
    const transform = this.getTransform();
    if (!transform) {
      return;
    }
    const { requestInterceptors, responseInterceptors, responseInterceptorsCatch } = transform;

    const axiosCanceler = new AxiosCanceler();

    // TODO: 拦截配置升级了 请求拦截器
    this.axiosInstance.interceptors.request.use((config: CreateAxiosOptions) => {
      // 如果ignoreCancelToken为true，则不添加到pending中
      const ignoreCancelToken = config.requestOptions?.ignoreCancelToken;
      const ignoreCancel =
        ignoreCancelToken !== undefined ? ignoreCancelToken : this.options.requestOptions?.ignoreCancelToken;

      if (!ignoreCancel) {
        axiosCanceler.addPending(config);
      }
      if (requestInterceptors && isFunction(requestInterceptors)) {
        config = requestInterceptors(config, this.options);
      }
      // TODO: 拦截配置升级了，暂时 as 处理
      return config as InternalAxiosRequestConfig;
    }, undefined);

    // 响应拦截器
    this.axiosInstance.interceptors.response.use((res: AxiosResponse<any>) => {
      if (res) {
        axiosCanceler.removePending(res.config);
      }
      if (responseInterceptors && isFunction(responseInterceptors)) {
        res = responseInterceptors(res);
      }
      return res;
    }, undefined);

    // 响应错误处理
    if (responseInterceptorsCatch && isFunction(responseInterceptorsCatch)) {
      this.axiosInstance.interceptors.response.use(undefined, responseInterceptorsCatch);
    }
  }

  /**
   * @description:  文件上传
   */
  uploadFile<T = any>(
    config: AxiosRequestConfig & RequestOptions,
    params: UploadFileParams,
    customFileKey = '',
    isMultiple = false
  ): Promise<T> {
    const formData = new window.FormData();
    const fileName = isMultiple ? 'files' : 'file';
    if (customFileKey !== '') {
      params.fileList.forEach((file: File) => {
        formData.append(customFileKey, file);
      });
    } else if (!isMultiple && !customFileKey) {
      params.fileList.forEach((file: File) => {
        formData.append(fileName, file);
      });
    } else {
      params.fileList.forEach((item: any) => {
        formData.append(fileName, item.file, item.file.name);
      });
    }
    if (params.request) {
      const requestData = JSON.stringify(params.request);
      formData.append('request', new Blob([requestData], { type: ContentTypeEnum.JSON }));
    }
    const transform = this.getTransform();

    const { requestOptions } = this.options;

    const opt = { ...requestOptions, isTransformResponse: false };
    const { transformRequestHook } = transform || {};
    return new Promise((resolve, reject) => {
      this.axiosInstance
        .request<any, AxiosResponse<Result>>({
          ...config,
          method: 'POST',
          data: formData,
          headers: {
            'Content-type': ContentTypeEnum.FORM_DATA,
          },
          // @ts-ignore
          requestOptions: {
            ignoreCancelToken: true, // 文件上传请求不需要添加到pending中，以免路由切换导致文件上传请求被取消
          },
        })
        .then((res: AxiosResponse<Result>) => {
          // 请求成功后的处理
          if (transformRequestHook && isFunction(transformRequestHook)) {
            try {
              const ret = transformRequestHook(res, opt);
              resolve(ret);
            } catch (err) {
              reject(err || new Error('request error!'));
            }
            return;
          }
          resolve(res as unknown as Promise<T>);
        })
        .catch((e: Error | AxiosError) => {
          if (axios.isAxiosError(e)) {
            // 在这可重写axios错误消息
            // eslint-disable-next-line no-console
            console.log(e);
          }
          reject(e);
        });
    });
  }

  get<T = any>(config: AxiosRequestConfig, options?: RequestOptions): Promise<T> {
    return this.request({ ...config, method: 'GET' }, options);
  }

  post<T = any>(config: AxiosRequestConfig, options?: RequestOptions): Promise<T> {
    return this.request({ ...config, method: 'POST' }, options);
  }

  put<T = any>(config: AxiosRequestConfig, options?: RequestOptions): Promise<T> {
    return this.request({ ...config, method: 'PUT' }, options);
  }

  delete<T = any>(config: AxiosRequestConfig, options?: RequestOptions): Promise<T> {
    return this.request({ ...config, method: 'DELETE' }, options);
  }

  request<T = any>(config: AxiosRequestConfig, options?: RequestOptions): Promise<T> {
    let conf: CreateAxiosOptions = cloneDeep(config);
    const transform = this.getTransform();

    const { requestOptions } = this.options;

    const opt = { ...requestOptions, ...options };

    const { beforeRequestHook, transformRequestHook } = transform || {};
    // 请求之前处理config
    if (beforeRequestHook && isFunction(beforeRequestHook)) {
      conf = beforeRequestHook(conf, opt);
    }
    conf.requestOptions = opt;

    return new Promise((resolve, reject) => {
      this.axiosInstance
        .request<any, AxiosResponse<Result>>(conf)
        .then((res: AxiosResponse<Result>) => {
          // 请求成功后的处理
          if (transformRequestHook && isFunction(transformRequestHook)) {
            try {
              const ret = transformRequestHook(res, opt);
              resolve(ret);
            } catch (err) {
              reject(err || new Error('request error!'));
            }
            return;
          }
          resolve(res as unknown as Promise<T>);
        })
        .catch((e: Error | AxiosError) => {
          if (axios.isAxiosError(e)) {
            // 在这可重写axios错误消息
            // eslint-disable-next-line no-console
            console.log(e);
          }
          reject(e);
        });
    });
  }
}
