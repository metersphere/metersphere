import { defineStore } from 'pinia';

// 验证分享的文档是否校验过
const useDocShareCheckStore = defineStore('shareCheckStore', {
  state: (): { verifiedDocs: string[] } => ({
    verifiedDocs: [],
  }),
  actions: {
    // 检查该 docShareId 和 userId 组合是否已经验证过
    isDocVerified(docShareId: string, userId: string) {
      const key: string = `verified_${docShareId}_${userId}`;
      return this.verifiedDocs.includes(key) || localStorage.getItem(key) === 'true';
    },
    // 将 docShareId 和 userId 组合标记为已验证
    markDocAsVerified(docShareId: string, userId: string) {
      const key: string = `verified_${docShareId}_${userId}`;
      if (!this.verifiedDocs.includes(key)) {
        this.verifiedDocs.push(key);
        localStorage.setItem(key, 'true');
      }
    },
    // 将 docShareId 和 userId  已经验证更新密码后移除验证重新验证
    removeDocAsVerified(docShareId: string, userId: string) {
      const key: string = `verified_${docShareId}_${userId}`;
      if (!this.verifiedDocs.includes(key)) {
        this.verifiedDocs = this.verifiedDocs.filter((e) => e === key);
        localStorage.removeItem(key);
      }
    },
  },
});

export default useDocShareCheckStore;
