import debug from './env';

export default ({ mock, setup }: { mock?: boolean; setup: () => void }) => {
  if (mock !== false && debug) setup();
};

export const successResponseWrap = (data: unknown) => {
  return {
    data,
    status: 'ok',
    message: '请求成功',
    code: 0,
  };
};

export const failResponseWrap = (data: unknown, message: string, code = 50000) => {
  return {
    data,
    status: 'fail',
    message,
    code,
  };
};
