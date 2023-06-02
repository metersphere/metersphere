export default interface CommonReponse<T> {
  code: number;
  message: string;
  messageDetail: string;
  data: T;
}
