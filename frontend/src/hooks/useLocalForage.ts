import localforage from 'localforage';

import useAppStore from '@/store/modules/app';

import { Recordable } from '#/global';

export default function useLocalForage() {
  const appStore = useAppStore();

  /**
   * 检测并序列化函数
   * @param val 要存储的值
   */
  const serializeValue = (val: any): any => {
    if (typeof val === 'function') {
      return `function:${val.toString()}`;
    }
    if (val && typeof val === 'object' && !Array.isArray(val)) {
      const newVal = { ...val };
      Object.keys(newVal).forEach((key) => {
        newVal[key] = serializeValue(newVal[key]);
      });
      return newVal;
    }
    if (Array.isArray(val)) {
      return val.map((item) => serializeValue(item));
    }
    return val;
  };

  const deserializeFunction = (funcStr: string) => {
    try {
      // eslint-disable-next-line no-eval
      const func = eval(`${funcStr}`);
      return func;
    } catch (e) {
      // eslint-disable-next-line no-console
      console.error(e);
      return null;
    }
  };

  /**
   * 反序列化值，将特殊格式字符串转换回函数
   * @param val 从存储中读取的值
   */
  const deserializeValue = <T>(val: any): T | null => {
    if (typeof val === 'string' && val.startsWith('function:')) {
      return deserializeFunction(val.slice(9)) as T;
    }
    if (val && typeof val === 'object' && !Array.isArray(val)) {
      const newVal = { ...val };
      Object.keys(newVal).forEach((key) => {
        newVal[key] = deserializeValue(newVal[key]);
      });
      return newVal as T;
    }
    if (Array.isArray(val)) {
      return val.map((item) => deserializeValue(item) as T) as T;
    }
    return val;
  };

  /**
   * 读取本地存储的数据
   * @param key 唯一 key
   * @param notIsolatedByProject 存储数据时是否不按项目隔离数据
   */
  const getItem = async <T>(key: string, notIsolatedByProject = false): Promise<T | null> => {
    const itemKey = notIsolatedByProject ? key : `${appStore.currentProjectId}-${key}`;
    try {
      const res = await localforage.getItem<T>(itemKey);
      if (!res) {
        return null;
      }
      return deserializeValue<T>(res);
    } catch (e) {
      // eslint-disable-next-line no-console
      console.log(e);
      return null;
    }
  };

  /**
   * 永久存储数据
   * @param key 唯一 key
   * @param val 存储的值
   * @param notIsolatedByProject 是否不按项目隔离数据
   */
  const setItem = async (key: string, val: string | number | boolean | Recordable, notIsolatedByProject = false) => {
    try {
      const itemKey = notIsolatedByProject ? key : `${appStore.currentProjectId}-${key}`;
      await localforage.setItem(itemKey, serializeValue(val));
    } catch (e) {
      // eslint-disable-next-line no-console
      console.log(e);
    }
  };

  return {
    getItem,
    setItem,
  };
}
