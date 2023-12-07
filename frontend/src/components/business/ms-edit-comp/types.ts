export type ModelValueType =
  | string
  | number
  | boolean
  | Record<string, any>
  | (string | number | boolean | Record<string, any>)[];
