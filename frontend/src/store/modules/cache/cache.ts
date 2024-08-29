import { defineStore } from 'pinia';

import { CacheTabTypeEnum } from '@/enums/cacheTabEnum';
import { RouteEnum } from '@/enums/routeEnum';

export interface CachePath {
  cacheName: string; // 缓存名称
  toPathName?: string[]; // 跳转不被清空缓存的下一级
  type?: string;
}

const useCacheStore = defineStore('cache', {
  state: (): { cachePath: CachePath[]; cacheViews: string[] } => ({
    cachePath: [
      // 功能用例
      {
        cacheName: RouteEnum.CASE_MANAGEMENT_CASE,
        toPathName: [RouteEnum.CASE_MANAGEMENT_CASE_DETAIL, RouteEnum.CASE_MANAGEMENT_CASE_CREATE_SUCCESS],
        type: 'ROUTE',
      },
      // 用例评审
      {
        cacheName: RouteEnum.CASE_MANAGEMENT_REVIEW,
        toPathName: [
          RouteEnum.CASE_MANAGEMENT_REVIEW_CREATE,
          RouteEnum.CASE_MANAGEMENT_REVIEW_DETAIL,
          RouteEnum.CASE_MANAGEMENT_REVIEW_DETAIL_CASE_DETAIL,
        ],
        type: 'ROUTE',
      },
      // 用例评审详情
      {
        cacheName: RouteEnum.CASE_MANAGEMENT_REVIEW_DETAIL,
        toPathName: [RouteEnum.CASE_MANAGEMENT_REVIEW_DETAIL_CASE_DETAIL],
        type: 'ROUTE',
      },
      // 缺陷管理
      {
        cacheName: RouteEnum.BUG_MANAGEMENT_INDEX,
        toPathName: [RouteEnum.BUG_MANAGEMENT_DETAIL, RouteEnum.BUG_MANAGEMENT_CREATE_SUCCESS],
        type: 'ROUTE',
      },
      // 测试计划首页
      {
        cacheName: RouteEnum.TEST_PLAN_INDEX,
        toPathName: [RouteEnum.TEST_PLAN_INDEX_DETAIL, RouteEnum.TEST_PLAN_INDEX_DETAIL_FEATURE_CASE_DETAIL],
        type: 'ROUTE',
      },
      // 测试计划详情
      {
        cacheName: RouteEnum.TEST_PLAN_INDEX_DETAIL,
        toPathName: [RouteEnum.TEST_PLAN_INDEX_DETAIL_FEATURE_CASE_DETAIL],
        type: 'ROUTE',
      },
      // 测试计划报告
      {
        cacheName: RouteEnum.TEST_PLAN_REPORT,
        toPathName: [RouteEnum.TEST_PLAN_REPORT_DETAIL],
        type: 'ROUTE',
      },
      // 接口测试-定义-API
      {
        cacheName: CacheTabTypeEnum.API_TEST_API_TABLE,
        type: 'TAB',
      },
      // 接口测试-定义-CASE
      {
        cacheName: CacheTabTypeEnum.API_TEST_CASE_TABLE,
        type: 'TAB',
      },
      // 接口测试-定义-MOCK
      {
        cacheName: CacheTabTypeEnum.API_TEST_MOCK_TABLE,
        type: 'TAB',
      },
      // 接口测试-场景列表
      {
        cacheName: CacheTabTypeEnum.API_SCENARIO_TABLE,
        type: 'TAB',
      },
    ],
    cacheViews: [], // 缓存列表
  }),
  actions: {
    // 设置缓存
    setCache(name: string) {
      this.cacheViews.push(name);
    },
    // 移除缓存
    removeCache(name: string) {
      if (this.cacheViews.includes(name)) {
        this.cacheViews = this.cacheViews.filter((item) => item !== name);
      }
    },
    // 清空缓存
    clearCache() {
      this.cacheViews = [];
    },
  },
});

export default useCacheStore;
