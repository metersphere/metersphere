import debug from './env';

export default ({ mock, setup }: { mock?: boolean; setup: () => void }) => {
  if (mock !== false && debug) setup();
};

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
 * mock- 失败返回结果结构体
 * @param data mock 返回结果
 * @param message 错误信息
 * @param code
 * @returns
 */
export const failResponseWrap = (data: unknown, message: string, code = 50000) => {
  return {
    data,
    status: 'fail',
    message,
    code,
  };
};
