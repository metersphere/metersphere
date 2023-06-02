import type { RouteLocationNormalized } from 'vue-router';
import { defineStore } from 'pinia';
import { DEFAULT_ROUTE, DEFAULT_ROUTE_NAME, REDIRECT_ROUTE_NAME } from '@/router/constants';
import { isString } from '@/utils/is';
import { TabBarState, TabProps } from './types';

const formatTag = (route: RouteLocationNormalized): TabProps => {
  const { name, meta, fullPath, query } = route;
  return {
    title: meta.locale || '',
    name: String(name),
    fullPath,
    query,
    ignoreCache: meta.ignoreCache,
  };
};

const BAN_LIST = [REDIRECT_ROUTE_NAME];

const useAppStore = defineStore('tabBar', {
  state: (): TabBarState => ({
    cacheTabList: new Set([DEFAULT_ROUTE_NAME]),
    tabList: [DEFAULT_ROUTE],
  }),

  getters: {
    getTabList(): TabProps[] {
      return this.tabList;
    },
    getCacheList(): string[] {
      return Array.from(this.cacheTabList);
    },
  },

  actions: {
    /**
     * 更新 tabs 页面队列
     * @param route 当前路由信息
     * @returns void
     */
    updateTabList(route: RouteLocationNormalized) {
      if (BAN_LIST.includes(route.name as string)) return;
      this.tabList.push(formatTag(route));
      if (!route.meta.ignoreCache) {
        this.cacheTabList.add(route.name as string);
      }
    },
    /**
     * 移除 tab
     * @param idx 需要移除的下标
     * @param tag 标签名
     */
    deleteTag(idx: number, tag: TabProps) {
      this.tabList.splice(idx, 1);
      this.cacheTabList.delete(tag.name);
    },
    /**
     * 添加缓存
     * @param name 添加缓存的标签名
     */
    addCache(name: string) {
      if (isString(name) && name !== '') this.cacheTabList.add(name);
    },
    /**
     * 删除缓存
     * @param tag 删除缓存的标签名
     */
    deleteCache(tag: TabProps) {
      this.cacheTabList.delete(tag.name);
    },
    /**
     * 刷新 tabs 页面队列
     * @param tabs 重置后的 tabs 页面队列
     */
    freshTabList(tabs: TabProps[]) {
      this.tabList = tabs;
      this.cacheTabList.clear();
      // 要先判断ignoreCache
      this.tabList
        .filter((el) => !el.ignoreCache)
        .map((el) => el.name)
        .forEach((x) => this.cacheTabList.add(x));
    },
    /**
     * 重置 tabs 页面队列
     */
    resetTabList() {
      this.tabList = [DEFAULT_ROUTE];
      this.cacheTabList.clear();
      this.cacheTabList.add(DEFAULT_ROUTE_NAME);
    },
  },
});

export default useAppStore;
