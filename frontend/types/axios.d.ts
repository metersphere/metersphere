export type ErrorMessageMode = 'none' | 'modal' | 'message' | undefined;

export interface RequestOptions {
  isTransformResponse?: boolean; // 是否需要处理请求结果
  isReturnNativeResponse?: boolean; // 是否需要返回原生响应头
  handleError?: boolean;
  joinParamsToUrl?: boolean; // post请求时，是否使用URLSearchParams
  noErrorTip?: boolean;
  errorMessageMode?: ErrorMessageMode; // 错误信息提示模式,none不提示
  joinTime?: boolean; // 是否加入时间戳
  ignoreCancelToken?: boolean; // 是否不记录取消请求的token，不记录则请求不会被取消；默认为记录，在路由切换时会清除上个页面未完成的请求
  withToken?: boolean; // 是否携带token
}

export interface Result<T = any> {
  code: number;
  type: 'success' | 'error' | 'warning';
  message: string;
  messageDetail?: string;
  data: T;
}

// multipart/form-data: upload file
export interface UploadFileParams {
  // Other parameters
  data?: Recordable;
  // File parameter interface field name
  name?: string;
  // file name
  file?: File | Blob;
  // file name
  filename?: string;
  [key: string]: any;
}
