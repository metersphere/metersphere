// 请求返回结构
export default interface CommonReponse<T> {
  code: number;
  message: string;
  messageDetail: string;
  data: T;
}
