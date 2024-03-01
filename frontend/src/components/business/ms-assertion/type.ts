export interface ValueObject {
  assertionType: string;
  [key: string]: any;
}
export interface MsAssertionItem {
  id: string;
  label: string;
  value: string;
  valueObj: ValueObject;
}
