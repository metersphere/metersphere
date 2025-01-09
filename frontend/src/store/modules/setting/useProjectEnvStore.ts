import { defineStore } from 'pinia';
import { cloneDeep } from 'lodash-es';

import { getDetailEnv, getGlobalParamDetail } from '@/api/modules/project-management/envManagement';
import useLocalForage from '@/hooks/useLocalForage';
import { useAppStore } from '@/store';
import { isArraysEqualWithOrder } from '@/utils/equal';

import {
  ContentTabItem,
  ContentTabsMap,
  EnvConfig,
  EnvDetailItem,
  GlobalParams,
} from '@/models/projectManagement/environmental';

export const ALL_PARAM = 'allParam';
export const NEW_ENV_PARAM = 'newEnvParam';
export const NEW_ENV_PARAM_COPY = 'newEnvParamCopy';
export const NEW_ENV_GROUP = 'newEnvGroup';
// 环境默认配置项
const envParamsDefaultConfig: EnvConfig = {
  commonVariables: [],
  httpConfig: [],
  dataSources: [],
  hostConfig: {
    enable: false,
    hosts: [],
  },
  preProcessorConfig: {
    apiProcessorConfig: {
      scenarioProcessorConfig: {
        processors: [],
      },
      requestProcessorConfig: {
        processors: [],
      },
    },
  },
  postProcessorConfig: {
    apiProcessorConfig: {
      scenarioProcessorConfig: {
        processors: [],
      },
      requestProcessorConfig: {
        processors: [],
      },
    },
  },
  assertionConfig: { assertions: [] },
  pluginConfigMap: {},
  commonParams: {
    requestTimeout: 60000,
    responseTimeout: 60000,
  },
};

const defaultAllParams = {
  projectId: '',
  globalParams: {
    headers: [],
    commonVariables: [],
  },
};

const useProjectEnvStore = defineStore(
  'projectEnv',
  () => {
    // 项目中的key值
    const currentId = ref<string>('');
    // 项目组选中的key值
    const currentGroupId = ref<string>('');
    // 当前选中的环境详情
    const currentEnvDetailInfo = ref<EnvDetailItem>({
      projectId: '',
      name: '',
      description: '',
      config: envParamsDefaultConfig,
    });
    const backupEnvDetailInfo = ref<EnvDetailItem>({
      projectId: '',
      name: '',
      description: '',
      config: cloneDeep(envParamsDefaultConfig),
    });
    const allParamDetailInfo = ref<GlobalParams>(defaultAllParams); // 全局参数详情

    const backupAllParamDetailInfo = ref<GlobalParams>(defaultAllParams);
    const httpNoWarning = ref(true);
    const getHttpNoWarning = computed(() => httpNoWarning.value);

    // 设置选中项
    function setCurrentId(id: string) {
      currentId.value = id;
    }
    // 设置选中项目组
    function setCurrentGroupId(id: string) {
      currentGroupId.value = id;
    }
    // 设置http提醒
    function setHttpNoWarning(noWarning: boolean) {
      httpNoWarning.value = noWarning;
    }
    // 设置全局参数
    function setAllParamDetailInfo(item: GlobalParams) {
      allParamDetailInfo.value = item;
    }
    function setDetailInfo(detailInfo: EnvDetailItem) {
      const appStore = useAppStore();

      nextTick(() => {
        backupEnvDetailInfo.value = cloneDeep(detailInfo);
        appStore.currentEnvConfig = cloneDeep(detailInfo.config);
        appStore.currentEnvConfig.id = detailInfo.id;
      });
    }
    async function copyCurrentEnv(copyId: string) {
      try {
        const tmpObj = await getDetailEnv(copyId);
        currentEnvDetailInfo.value = { ...tmpObj };
        currentEnvDetailInfo.value.id = '';
        let copyName = `copy_${currentEnvDetailInfo.value.name}`;
        if (copyName.length > 255) {
          copyName = copyName.slice(0, 255);
        }
        currentEnvDetailInfo.value.name = copyName;
        setDetailInfo(currentEnvDetailInfo.value);
      } catch (error) {
        console.log(error);
      }
    }
    // 初始化环境详情
    async function initEnvDetail(copyId = '') {
      const id = currentId.value;
      const appStore = useAppStore();
      try {
        if (id === NEW_ENV_PARAM) {
          currentEnvDetailInfo.value = {
            projectId: appStore.currentProjectId,
            name: '',
            config: cloneDeep(envParamsDefaultConfig),
          };
          backupEnvDetailInfo.value = {
            projectId: appStore.currentProjectId,
            name: '',
            config: cloneDeep(envParamsDefaultConfig),
          };
        } else if (id === ALL_PARAM) {
          const res = await getGlobalParamDetail(appStore.currentProjectId);
          allParamDetailInfo.value = cloneDeep(res || defaultAllParams);
          nextTick(() => {
            backupAllParamDetailInfo.value = cloneDeep(allParamDetailInfo.value);
          });
        } else if (id === NEW_ENV_PARAM_COPY && copyId) {
          copyCurrentEnv(copyId);
        } else if (id && id !== ALL_PARAM && id !== NEW_ENV_PARAM_COPY) {
          const tmpObj = await getDetailEnv(id);
          currentEnvDetailInfo.value = { ...tmpObj };
          setDetailInfo(currentEnvDetailInfo.value);
        }
      } catch (e) {
        // eslint-disable-next-line no-console
        console.log(e);
      }
    }

    // 初始化内容tab列表
    async function initContentTabList(arr: ContentTabItem[]) {
      try {
        const { getItem, setItem } = useLocalForage();
        const tabsMap = await getItem<ContentTabsMap>('bugTabsMap');
        if (tabsMap) {
          // 初始化过了
          const { backupTabList } = tabsMap;
          const isEqual = isArraysEqualWithOrder<ContentTabItem>(backupTabList, arr);
          if (!isEqual) {
            tabsMap.tabList = arr;
            tabsMap.backupTabList = arr;
            await setItem('bugTabsMap', tabsMap);
          }
        } else {
          // 没初始化过
          await setItem('bugTabsMap', { tabList: arr, backupTabList: arr });
        }
      } catch (e) {
        // eslint-disable-next-line no-console
        console.log(e);
      }
    }
    // 获取Tab列表
    async function getContentTabList() {
      try {
        const { getItem } = useLocalForage();
        const tabsMap = await getItem<ContentTabsMap>('bugTabsMap');
        if (tabsMap) {
          return tabsMap.tabList;
        }
        return [];
      } catch (e) {
        // eslint-disable-next-line no-console
        console.log(e);
      }
    }

    // 设置Tab列表
    async function setContentTabList(arr: ContentTabItem[]) {
      const { getItem, setItem } = useLocalForage();
      const tabsMap = await getItem<ContentTabsMap>('bugTabsMap');
      if (tabsMap) {
        const tmpArrList = JSON.parse(JSON.stringify(arr));
        tabsMap.tabList = tmpArrList;
        await setItem('bugTabsMap', tabsMap);
      }
    }

    return {
      currentId,
      currentGroupId,
      getHttpNoWarning,
      httpNoWarning,
      allParamDetailInfo,
      currentEnvDetailInfo,
      backupEnvDetailInfo,
      setCurrentId,
      setCurrentGroupId,
      setHttpNoWarning,
      setAllParamDetailInfo,
      backupAllParamDetailInfo,
      initEnvDetail,
      initContentTabList,
      getContentTabList,
      setContentTabList,
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
