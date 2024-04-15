import localforage from 'localforage';

import useAppStore from '@/store/modules/app';

import { Recordable } from '#/global';

export default function useLocalForage() {
  const appStore = useAppStore();

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
      return res;
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
      await localforage.setItem(itemKey, val);
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
