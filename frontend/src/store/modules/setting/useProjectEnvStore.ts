import { defineStore } from 'pinia';

export const ALL_PARAM = 'allParam';

const useProjectEnvStore = defineStore('projectEnv', () => {
  const currentId = ref<string | number>(ALL_PARAM);
  const getCurrentId = computed(() => currentId.value);
  function setCurrentId(id: string | number) {
    currentId.value = id;
  }
  return { currentId, getCurrentId, setCurrentId };
});

export default useProjectEnvStore;
