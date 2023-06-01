import { describe, expect, test } from 'vitest';
import {
  isArray,
  isObject,
  isEmptyObject,
  isExist,
  isFunction,
  isNull,
  isUndefined,
  isNumber,
  isString,
  isRegExp,
  isFile,
  isBlob,
} from '@/utils/is';
import logo from '@/assets/svg/logo.svg';

describe('Is tool', () => {
  test('isArray', () => {
    const res = isArray([]);
    expect(res).toBe(true);

    const res2 = isArray('');
    expect(res2).toBe(false);
  });

  test('isObject', () => {
    const res = isObject({ a: 'a' });
    expect(res).toBe(true);

    const res2 = isObject([]);
    expect(res2).toBe(false);
  });

  test('isEmptyObject', () => {
    const res = isEmptyObject({});
    expect(res).toBe(true);

    const res2 = isEmptyObject({ a: 'a' });
    expect(res2).toBe(false);

    const res3 = isEmptyObject([]);
    expect(res3).toBe(false);
  });

  test('isExist', () => {
    const res = isExist(0);
    expect(res).toBe(true);

    const res2 = isExist(null);
    expect(res2).toBe(false);
  });

  test('isFunction', () => {
    const res = isFunction(() => ({}));
    expect(res).toBe(true);

    const res2 = isFunction({});
    expect(res2).toBe(false);
  });

  test('isNull', () => {
    const res = isNull(null);
    expect(res).toBe(true);

    const res2 = isNull(undefined);
    expect(res2).toBe(false);
  });

  test('isUndefined', () => {
    const res = isUndefined(undefined);
    expect(res).toBe(true);

    const res2 = isUndefined(null);
    expect(res2).toBe(false);
  });

  test('isNumber', () => {
    const res = isNumber(0);
    expect(res).toBe(true);

    const res2 = isNumber(null);
    expect(res2).toBe(false);
  });

  test('isString', () => {
    const res = isString('');
    expect(res).toBe(true);

    const res2 = isString(0);
    expect(res2).toBe(false);
  });

  test('isRegExp', () => {
    const res = isRegExp(/^a/);
    expect(res).toBe(true);

    const res2 = isRegExp('');
    expect(res2).toBe(false);
  });

  test('isFile', () => {
    const file = new File([logo], 'logo.svg');
    const res = isFile(file);
    expect(res).toBe(true);

    const res2 = isFile({});
    expect(res2).toBe(false);
  });

  test('isBlob', () => {
    const blob = new Blob();
    const res = isBlob(blob);
    expect(res).toBe(true);

    const res2 = isBlob(logo);
    expect(res2).toBe(false);
  });
});
