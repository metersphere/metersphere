import { defineStore } from 'pinia';

// 用于记录用户是否第一次访问某些功能，会持久化存储
const useVisitStore = defineStore('visit', {
  state: () => ({
    visitedKeys: [] as string[],
  }),
  actions: {
    addVisitedKey(key: string) {
      this.visitedKeys.push(key);
    },
    deleteVisitedKey(key: string) {
      this.visitedKeys = this.visitedKeys.filter((item) => item !== key);
    },
    getIsVisited(key: string): boolean {
      return this.visitedKeys.includes(key);
    },
  },
  persist: {
    paths: ['visitedKeys'],
  },
});

export default useVisitStore;
