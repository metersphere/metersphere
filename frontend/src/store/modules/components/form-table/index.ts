import { defineStore } from 'pinia';

const useFormTableStore = defineStore('formTable', {
  persist: true,
  state: (): {
    errorMessageList: string[];
  } => ({
    errorMessageList: [],
  }),
  actions: {
    setErrorMessageList(list: string[]) {
      this.errorMessageList = list;
    },
  },
});

export default useFormTableStore;
