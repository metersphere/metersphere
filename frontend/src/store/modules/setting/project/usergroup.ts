import { defineStore } from 'pinia';
import { UserGroupState } from '../types';

const useProjectUserGroupStore = defineStore('projectUserGroup', {
  state: (): UserGroupState => ({
    currentName: '',
    currentTitle: '',
    currentId: '',
    currentType: '',
    currentInternal: false,
    collapse: true,
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
    // 设置用户组菜单开启关闭
    setCollapse(collapse: boolean) {
      this.collapse = collapse;
    },
  },
});

export default useProjectUserGroupStore;
