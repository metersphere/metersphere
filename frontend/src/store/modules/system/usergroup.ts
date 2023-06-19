import { defineStore } from 'pinia';
import { UserGroupState } from './types';

const useUserGroupStore = defineStore('userGroup', {
  state: (): UserGroupState => ({
    currentName: '',
    currentTitle: '',
    currentId: '',
  }),
  getters: {
    userGroupInfo(state: UserGroupState): UserGroupState {
      return state;
    },
  },
  actions: {
    // 设置当前用户组信息
    setInfo(partial: Partial<UserGroupState>) {
      this.$patch(partial);
    },
  },
});

export default useUserGroupStore;
