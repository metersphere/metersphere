import { defineStore } from 'pinia';
import localforage from 'localforage';

import { getDetailEnv, getGlobalParamDetail } from '@/api/modules/project-management/envManagement';
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
export const NEW_ENV_GROUP = 'newEnvGroup';
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
    },

    // TODO 环境参数问题
    // apiProcessorConfig: [],
  },
  postProcessorConfig: {
    // TODO 环境参数问题
    // apiProcessorConfig: [],
    apiProcessorConfig: {
      scenarioProcessorConfig: {
        processors: [],
      },
    },
  },
  assertionConfig: { assertions: [] },
  pluginConfigMap: {},
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
      config: envParamsDefaultConfig,
    });
    const allParamDetailInfo = ref<GlobalParams>(); // 全局参数详情
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
    // 初始化环境详情
    async function initEnvDetail() {
      const id = currentId.value;
      const appStore = useAppStore();
      try {
        if (id === NEW_ENV_PARAM) {
          // TODO 数据参数问题
          currentEnvDetailInfo.value = {
            projectId: appStore.currentProjectId,
            name: '',
            config: envParamsDefaultConfig,
          };
          backupEnvDetailInfo.value = {
            projectId: appStore.currentProjectId,
            name: '',
            config: envParamsDefaultConfig,
          };
        } else if (id === ALL_PARAM) {
          allParamDetailInfo.value = await getGlobalParamDetail(appStore.currentProjectId);
        } else if (id !== ALL_PARAM && id) {
          const tmpObj = await getDetailEnv(id);
          currentEnvDetailInfo.value = tmpObj;
          backupEnvDetailInfo.value = tmpObj;
        }
      } catch (e) {
        // eslint-disable-next-line no-console
        console.log(e);
      }
    }

    // 初始化内容tab列表
    async function initContentTabList(arr: ContentTabItem[]) {
      try {
        const tabsMap = await localforage.getItem<ContentTabsMap>('bugTabsMap');
        if (tabsMap) {
          // 初始化过了
          const { backupTabList } = tabsMap;
          const isEqual = isArraysEqualWithOrder<ContentTabItem>(backupTabList, arr);
          if (!isEqual) {
            tabsMap.tabList = arr;
            tabsMap.backupTabList = arr;
            await localforage.setItem('bugTabsMap', tabsMap);
          }
        } else {
          // 没初始化过
          await localforage.setItem('bugTabsMap', { tabList: arr, backupTabList: arr });
        }
      } catch (e) {
        // eslint-disable-next-line no-console
        console.log(e);
      }
    }
    // 获取Tab列表
    async function getContentTabList() {
      try {
        const tabsMap = await localforage.getItem<ContentTabsMap>('bugTabsMap');
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
      const tabsMap = await localforage.getItem<ContentTabsMap>('bugTabsMap');
      if (tabsMap) {
        const tmpArrList = JSON.parse(JSON.stringify(arr));
        tabsMap.tabList = tmpArrList;
        await localforage.setItem('bugTabsMap', tabsMap);
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
