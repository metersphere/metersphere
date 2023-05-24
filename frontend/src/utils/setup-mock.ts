import debug from './env';

export default ({ mock, setup }: { mock?: boolean; setup: () => void }) => {
  if (mock !== false && debug) setup();
};

export const successResponseWrap = (result: unknown) => {
  return {
    result,
    status: 'ok',
    message: '请求成功',
    code: 0,
  };
};

export const failResponseWrap = (result: unknown, message: string, code = 50000) => {
  return {
    result,
    status: 'fail',
    message,
    code,
  };
};
