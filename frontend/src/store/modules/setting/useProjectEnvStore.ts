import { defineStore } from 'pinia';

import { EnvGroupListItem } from '@/models/projectManagement/environmental';

export const ALL_PARAM = 'allParam';

const useProjectEnvStore = defineStore(
  'projectEnv',
  () => {
    const currentId = ref<string | number>(1);
    const httpNoWarning = ref(true);
    const envGroupList = ref<EnvGroupListItem[]>([]);

    const getCurrentId = computed(() => currentId.value);
    const getHttpNoWarning = computed(() => httpNoWarning.value);
    const getGroupLength = computed(() => 1);

    const getDatabaseList = computed(() => [{ id: 1, name: 'test' }]);
    function setCurrentId(id: string | number) {
      currentId.value = id;
    }
    function setHttpNoWarning(noWarning: boolean) {
      httpNoWarning.value = noWarning;
    }
    return {
      getCurrentId,
      currentId,
      httpNoWarning,
      setCurrentId,
      setHttpNoWarning,
      getHttpNoWarning,
      getDatabaseList,
      envGroupList,
      getGroupLength,
    };
  },
  {
    persist: {
      key: 'projectEnv',
      paths: ['httpNoWarning'],
    },
  }
);

export default useProjectEnvStore;
