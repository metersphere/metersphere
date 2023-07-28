import MSR from '@/api/http';
import { RequestEnum } from '@/enums/httpEnum';
import MockAdapter from 'axios-mock-adapter';

const MOCK = new MockAdapter(MSR.axiosInstance, { onNoMatch: 'throwException' });

/**
 * mock- 成功返回结果结构体
 * @param data mock 返回结果
 * @returns
 */
export const successResponseWrap = (data: unknown) => {
  return {
    data,
    status: 'ok',
    message: '请求成功',
    code: 0,
  };
};

/**
 * mock- 表格接口成功返回结果结构体
 * @param data mock 返回结果
 * @returns
 */
export const successTableResponseWrap = (data: unknown) => {
  return {
    data: { list: data },
    status: 'ok',
    message: '请求成功',
    code: 100200,
  };
};

/**
 * mock- 失败返回结果结构体
 * @param data mock 返回结果
 * @param message 错误信息
 * @param code
 * @returns
 */
export const failResponseWrap = (data: unknown, message?: string, messageDetail?: string) => {
  return {
    data,
    message: message || '请求失败',
    messageDetail,
  };
};

export const mock = (
  method: RequestEnum,
  url: string | RegExp,
  data: unknown,
  code: number,
  isTable?: boolean,
  message?: string,
  messageDetail?: string
) => {
  const methodMap = {
    [RequestEnum.GET]: MOCK.onGet(url),
    [RequestEnum.POST]: MOCK.onPost(url),
    [RequestEnum.PUT]: MOCK.onPut(url),
    [RequestEnum.DELETE]: MOCK.onDelete(url),
  };
  if (code === 200) {
    return methodMap[method].reply(code, isTable ? successTableResponseWrap(data) : successResponseWrap(data));
  }
  return methodMap[method].reply(code, failResponseWrap(data, message, messageDetail));
};

/**
 *  拼接 mock 匹配的 url
 * @param url 不含前缀的接口地址
 * @returns 拼接后的完整 url
 */
export const makeMockUrl = (url: string) => {
  const mockOrigin = window.location.origin;
  return `${mockOrigin}/front${url}`;
};

export default MOCK;
