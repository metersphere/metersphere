// 登录信息
export interface LoginData {
  username: string;
  password: string;
  authenticate: string;
}

// 登录返回
export interface LoginRes {
  sessionId: string;
  csrfToken: string;
}
