import type { ExecuteAssertionItem } from '@/models/apiTest/common';

export interface ValueObject {
  assertionType: string;
  [key: string]: any;
}
export interface MsAssertionItem {
  id: string;
  label: string;
  value: string;
  name: string;
  valueObj: ValueObject;
}

export interface ExecuteAssertion {
  assertionType: string;
  enable: boolean;
  name: string;
  id: string;
  assertions: ExecuteAssertionItem[];
  expectedValue: any;
  condition: string;
  variableAssertionItems: ExecuteAssertionItem[];
}
