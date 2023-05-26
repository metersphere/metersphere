export default interface CommonReponse<T> {
  code: number;
  message: string;
  data: T;
}
