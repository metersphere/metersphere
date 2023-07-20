import { isLogin as isLoginFun } from '@/api/modules/user';

const SESSION_ID = 'sessionId';
const CSRF_TOKEN = 'csrfToken';

const isLogin = async () => {
  try {
    await isLoginFun();
    return true;
  } catch (err) {
    return false;
  }
};
// 获取token
const getToken = () => {
  return { [SESSION_ID]: localStorage.getItem(SESSION_ID), [CSRF_TOKEN]: localStorage.getItem(CSRF_TOKEN) || '' };
};

const setToken = (sessionId: string, csrfToken: string) => {
  localStorage.setItem(SESSION_ID, sessionId);
  localStorage.setItem(CSRF_TOKEN, csrfToken);
};

const clearToken = () => {
  localStorage.removeItem(SESSION_ID);
  localStorage.removeItem(CSRF_TOKEN);
};

const hasToken = () => {
  return !!localStorage.getItem(SESSION_ID) && !!localStorage.getItem(CSRF_TOKEN);
};

const setLoginExpires = () => {
  localStorage.setItem('loginExpires', Date.now().toString());
};

const isLoginExpires = () => {
  const lastLoginTime = Number(localStorage.getItem('loginExpires'));
  const now = Date.now();
  const diff = now - lastLoginTime;
  const thirtyDay = 24 * 60 * 60 * 1000 * 30;
  return diff > thirtyDay;
};

export { isLogin, getToken, setToken, clearToken, hasToken, setLoginExpires, isLoginExpires };
