import { Message, Modal } from '@arco-design/web-vue';

import { useI18n } from '@/hooks/useI18n';
import useLocale from '@/locale/useLocale';
import useAppStore from '@/store/modules/app';
import { deepMerge, setObjToUrlParams } from '@/utils';
import { getToken } from '@/utils/auth';
import { isString } from '@/utils/is';

import type CommonResponse from '@/models/common';
import { ContentTypeEnum, RequestEnum } from '@/enums/httpEnum';

import { MSAxios } from './Axios';
import type { AxiosTransform, CreateAxiosOptions } from './axiosTransform';
import checkStatus from './checkStatus';
import { joinTimestamp } from './helper';
import type { RequestOptions, Result } from '#/axios';
import type { Recordable } from '#/global';
import type { AxiosResponse } from 'axios';

/**
 * @description: 数据处理，方便区分多种处理方式
 */
const transform: AxiosTransform = {
  /**
   * @description 请求之前处理config
   */
  beforeRequestHook: (config, options) => {
    const { joinParamsToUrl, joinTime = true } = options;

    const params = config.params || {};
    const data = config.data || false;
    if (config.method?.toUpperCase() === RequestEnum.GET) {
      if (!isString(params)) {
        // 给 get 请求加上时间戳参数，避免从缓存中拿数据。
        config.params = Object.assign(params || {}, joinTimestamp(joinTime, false));
      } else {
        // 兼容restful风格
        config.url = `${config.url}/${params}${joinTimestamp(joinTime, true)}`;
        config.params = undefined;
      }
    } else if (isString(params)) {
      // 兼容restful风格
      config.url += params;
      config.params = undefined;
    } else {
      if (
        Reflect.has(config, 'data') &&
        config.data &&
        (Object.keys(config.data).length > 0 || Array.isArray(config.data))
      ) {
        config.data = data;
        config.params = params;
      } else {
        // 非GET请求如果没有提供data，则将params视为data
        config.data = { ...params };
        config.params = undefined;
      }
      if (joinParamsToUrl) {
        config.url = setObjToUrlParams(config.url as string, { ...config.params, ...config.data });
      }
    }
    return config;
  },

  /**
   * @description: 处理请求数据。如果数据不是预期格式，可直接抛出错误
   */
  transformRequestHook: (res: AxiosResponse<Result>, options: RequestOptions) => {
    const { t } = useI18n();
    const { isTransformResponse, isReturnNativeResponse } = options;
    // 是否返回原生响应头 比如：需要获取响应头时使用该属性
    if (isReturnNativeResponse) {
      return res;
    }
    // 不进行任何处理，直接返回
    // 用于页面代码可能需要直接获取code，data，message这些信息时开启
    if (!isTransformResponse) {
      return res.data;
    }
    // 错误的时候返回

    const { data } = res;
    if (!data) {
      throw new Error(t('api.apiRequestFailed'));
    }
    //  这里 code，result，message为 后台统一的字段
    const { data: dataResult } = data;

    // 这里直接返回正常结果，因为拦截器已经拦截了非 200 的请求
    return dataResult;
    // const hasSuccess = data && Reflect.has(data, 'code') && Number(code) === ResultEnum.SUCCESS;
    // if (hasSuccess) {
    //   return dataResult;
    // }

    // // 在此处根据自己项目的实际情况对不同的code执行不同的操作
    // // 如果不希望中断当前请求，请return数据，否则直接抛出异常即可
    // let timeoutMsg = '';
    // if (Number(code) === ResultEnum.TIMEOUT) {
    //   timeoutMsg = t('api.timeoutMessage');
    // } else if (message) {
    //   timeoutMsg = message;
    // }

    // // errorMessageMode=‘modal’的时候会显示modal错误弹窗，而不是消息提示，用于一些比较重要的错误
    // // errorMessageMode='none' 一般是调用时明确表示不希望自动弹出错误提示
    // if (options.errorMessageMode === 'modal') {
    //   Modal.error({ title: t('api.errorTip'), content: timeoutMsg });
    // } else if (options.errorMessageMode === 'message') {
    //   Message.error(timeoutMsg);
    // }

    // throw new Error(timeoutMsg || t('api.apiRequestFailed'));
  },

  /**
   * @description: 请求拦截器处理
   */
  requestInterceptors: (config) => {
    // 请求之前处理config
    const { currentLocale } = useLocale();
    const appStore = useAppStore();
    const token = getToken();
    if (token && (config as Recordable)?.requestOptions?.withToken !== false) {
      const { sessionId, csrfToken } = token;

      (config as Recordable).headers = {
        ...config.headers,
        'X-AUTH-TOKEN': sessionId,
        'CSRF-TOKEN': csrfToken,
        'Accept-Language': currentLocale.value,
        'ORGANIZATION': appStore.currentOrgId,
        'PROJECT': appStore.currentProjectId,
      };
    }
    return config;
  },

  /**
   * @description: 响应拦截器处理
   */
  responseInterceptors: (res: AxiosResponse<CommonResponse<any>>) => {
    return res;
  },

  /**
   * @description: 响应错误处理
   */
  responseInterceptorsCatch: (error: any) => {
    const { t } = useI18n();
    const { response, code, message, config } = error || {};
    const errorMessageMode = config?.requestOptions?.errorMessageMode || 'none';
    const msg: string = response?.data?.message ?? '';
    const err: string = error?.toString?.() ?? '';
    let errMessage = '';

    try {
      if (code === 'ECONNABORTED' && message.indexOf('timeout') !== -1) {
        errMessage = t('api.apiTimeoutMessage');
      }
      if (err?.includes('Network Error')) {
        errMessage = t('api.networkExceptionMsg');
      }

      if (errMessage) {
        if (errorMessageMode === 'modal') {
          Modal.error({ title: t('api.errorTip'), content: errMessage });
        } else if (errorMessageMode === 'message') {
          Message.error({ content: errMessage, duration: 5000 });
        }
        return Promise.reject(error);
      }
    } catch (e) {
      throw new Error(e as unknown as string);
    }
    checkStatus(response?.status, msg, response?.data?.code, errorMessageMode);
    return Promise.reject(
      response?.config?.requestOptions?.isReturnNativeResponse ? response?.data : response?.data?.message || error
    );
  },
};

function createAxios(opt?: Partial<CreateAxiosOptions>) {
  return new MSAxios(
    deepMerge(
      {
        // See https://developer.mozilla.org/en-US/docs/Web/HTTP/Authentication#authentication_schemes
        // authentication schemes，e.g: Bearer
        // authenticationScheme: 'Bearer',
        authenticationScheme: '',
        baseURL: `${window.location.origin}/${import.meta.env.VITE_API_BASE_URL as string}`,
        timeout: 300 * 1000,
        headers: { 'Content-Type': ContentTypeEnum.JSON },
        // 如果是form-data格式
        // headers: { 'Content-Type': ContentTypeEnum.FORM_URLENCODED },
        // 数据处理方式
        transform,
        // 配置项，下面的选项都可以在独立的接口请求中覆盖
        requestOptions: {
          // 默认将prefix 添加到url
          joinPrefix: true,
          // 是否返回原生响应头 比如：需要获取响应头时使用该属性
          isReturnNativeResponse: false,
          // 需要对返回数据进行处理
          isTransformResponse: true,
          // post请求的时候添加参数到url
          joinParamsToUrl: false,
          // 格式化提交参数时间
          formatDate: true,
          // 消息提示类型
          errorMessageMode: 'message',
          //  是否加入时间戳
          joinTime: true,
          // 忽略取消请求的token
          ignoreCancelToken: false,
          // 是否携带token
          withToken: true,
        },
      },
      opt || {}
    )
  );
}
const MSR = createAxios();
export default MSR;
