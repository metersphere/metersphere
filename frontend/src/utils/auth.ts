const SESSION_ID = 'sessionId';
const CSRF_TOKEN = 'csrfToken';

const isLogin = () => {
  return !!localStorage.getItem(SESSION_ID);
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

export { isLogin, getToken, setToken, clearToken };
