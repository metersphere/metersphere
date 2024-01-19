import { defineStore } from 'pinia';

import { getDetailEnv, getGlobalParamDetail } from '@/api/modules/project-management/envManagement';
import { useAppStore } from '@/store';

import { EnvDetailItem, GlobalParams } from '@/models/projectManagement/environmental';

export const ALL_PARAM = 'allParam';
export const NEW_ENV_PARAM = 'newEnvParam';

const useProjectEnvStore = defineStore(
  'projectEnv',
  () => {
    const currentId = ref<string>(ALL_PARAM); // 当前选中的key值
    const currentEnvDetailInfo = ref<EnvDetailItem>(); // 当前选中的环境详情
    const allParamDetailInfo = ref<GlobalParams>(); // 全局参数详情
    const httpNoWarning = ref(true);
    const getHttpNoWarning = computed(() => httpNoWarning.value);
    const groupLength = ref(0); // 环境分组数据
    // 设置选中项
    function setCurrentId(id: string) {
      currentId.value = id;
    }
    // 设置http提醒
    function setHttpNoWarning(noWarning: boolean) {
      httpNoWarning.value = noWarning;
    }
    // 设置环境详情
    function setEnvDetailInfo(item: EnvDetailItem) {
      currentEnvDetailInfo.value = item;
    }
    // 设置全局参数
    function setAllParamDetailInfo(item: GlobalParams) {
      allParamDetailInfo.value = item;
    }
    // 初始化环境详情
    async function initEnvDetail() {
      const id = currentId.value;
      const appStore = useAppStore();
      try {
        if (id === NEW_ENV_PARAM) {
          currentEnvDetailInfo.value = undefined;
        } else if (id === ALL_PARAM) {
          allParamDetailInfo.value = await getGlobalParamDetail(appStore.currentProjectId);
        } else if (id !== ALL_PARAM && id) {
          currentEnvDetailInfo.value = await getDetailEnv(id);
        }
      } catch (e) {
        // eslint-disable-next-line no-console
        console.log(e);
      }
    }

    return {
      currentId,
      getHttpNoWarning,
      httpNoWarning,
      allParamDetailInfo,
      groupLength,
      setCurrentId,
      setHttpNoWarning,
      setEnvDetailInfo,
      setAllParamDetailInfo,
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
