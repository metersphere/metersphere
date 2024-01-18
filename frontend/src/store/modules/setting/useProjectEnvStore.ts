import { defineStore } from 'pinia';

import { getDetailEnv } from '@/api/modules/project-management/envManagement';

import { EnvDetailItem, EnvGroupListItem } from '@/models/projectManagement/environmental';

export const ALL_PARAM = 'allParam';
export const NEW_ENV_PARAM = 'newEnvParam';

const useProjectEnvStore = defineStore(
  'projectEnv',
  () => {
    const currentId = ref<string>('');
    const currentEnvDetailInfo = ref<EnvDetailItem>();
    const httpNoWarning = ref(true);
    const envGroupList = ref<EnvGroupListItem[]>([]);

    const getCurrentId = computed(() => currentId.value);
    const getHttpNoWarning = computed(() => httpNoWarning.value);
    const getGroupLength = computed(() => 1);

    const getDatabaseList = computed(() => [{ id: 1, name: 'test' }]);
    function setCurrentId(id: string) {
      currentId.value = id;
    }
    function setHttpNoWarning(noWarning: boolean) {
      httpNoWarning.value = noWarning;
    }
    function setEnvDetailInfo(item: EnvDetailItem) {
      currentEnvDetailInfo.value = item;
    }
    async function initEnvDetail() {
      const id = currentId.value;
      try {
        if (id === NEW_ENV_PARAM) {
          currentEnvDetailInfo.value = undefined;
        } else if (id !== ALL_PARAM && id) {
          currentEnvDetailInfo.value = await getDetailEnv(id);
        }
      } catch (e) {
        // eslint-disable-next-line no-console
        console.log(e);
      }
    }

    return {
      getCurrentId,
      currentId,
      httpNoWarning,
      setCurrentId,
      setHttpNoWarning,
      setEnvDetailInfo,
      getHttpNoWarning,
      getDatabaseList,
      envGroupList,
      getGroupLength,
      initEnvDetail,
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
