<template>
  <div class="flex gap-[8px] px-[16px] pt-[16px]">
    <a-select
      v-model:model-value="currentTab"
      class="w-[80px] focus-within:!bg-[var(--color-text-n8)] hover:!bg-[var(--color-text-n8)]"
      :bordered="false"
      :options="tabOptions"
      @change="currentTabChange"
    />
    <MsEditableTab
      v-model:active-tab="activeApiTab"
      v-model:tabs="apiTabs"
      class="flex-1 overflow-hidden"
      :show-add="currentTab === 'api' && hasAnyPermission(['PROJECT_API_DEFINITION:READ+ADD'])"
      @add="newTab"
      @close="handleTabClose"
    >
      <template #label="{ tab }">
        <apiMethodName
          v-if="tab.id !== 'all' && tab.type === 'api'"
          :method="tab.protocol === 'HTTP' ? tab.method : tab.protocol"
          class="mr-[4px]"
        />
        <svg-icon
          v-if="tab.id !== 'all' && tab.type === 'case'"
          width="16px"
          height="16px"
          :name="'apiCase'"
          class="mr-[4px]"
        />
        <a-tooltip :content="tab.name || tab.label" :mouse-enter-delay="500">
          <div class="one-line-text max-w-[144px]">
            {{ tab.name || tab.label }}
          </div>
        </a-tooltip>
      </template>
    </MsEditableTab>
    <MsEnvironmentSelect
      v-show="activeApiTab.id !== 'all'"
      ref="environmentSelectRef"
      :env="activeApiTab.environmentId"
      size="mini"
    />
  </div>
  <api
    v-show="(activeApiTab.id === 'all' && currentTab === 'api') || activeApiTab.type === 'api'"
    ref="apiRef"
    v-model:active-api-tab="activeApiTab"
    v-model:api-tabs="apiTabs"
    :active-module="props.activeModule"
    :offspring-ids="props.offspringIds"
    :selected-protocols="props.selectedProtocols"
    :module-tree="props.moduleTree"
    :current-tab="currentTab"
    @import="emit('import')"
    @open-case-tab="(apiCaseDetail:ApiCaseDetail)=>newCaseTab(apiCaseDetail.id)"
    @delete-api="(id) => handleDeleteApiFromModuleTree(id)"
    @handle-adv-search="(val) => emit('handleAdvSearch', val)"
  />
  <apiCase
    v-show="(activeApiTab.id === 'all' && currentTab === 'case') || activeApiTab.type === 'case'"
    ref="caseRef"
    v-model:api-tabs="apiTabs"
    v-model:active-api-tab="activeApiTab"
    :active-module="props.activeModule"
    :selected-protocols="props.selectedProtocols"
    :module-tree="props.moduleTree"
    :current-tab="currentTab"
    :offspring-ids="props.offspringIds"
    @delete-case="(id) => handleDeleteApiFromModuleTree(id)"
    @handle-adv-search="(val) => emit('handleAdvSearch', val)"
  />
  <keep-alive :include="cacheStore.cacheViews">
    <MockTable
      v-if="activeApiTab.id === 'all' && currentTab === 'mock'"
      :key="CacheTabTypeEnum.API_TEST_MOCK_TABLE"
      :active-module="props.activeModule"
      :offspring-ids="props.offspringIds"
      :selected-protocols="props.selectedProtocols"
      :definition-detail="activeApiTab"
      :is-need-init="true"
      @debug="handleMockDebug"
      @handle-adv-search="(val) => emit('handleAdvSearch', val)"
    />
  </keep-alive>
</template>

<script setup lang="ts">
  import { useRoute } from 'vue-router';
  import { cloneDeep } from 'lodash-es';

  import MsEditableTab from '@/components/pure/ms-editable-tab/index.vue';
  import { TabItem } from '@/components/pure/ms-editable-tab/types';
  import MsEnvironmentSelect from '@/components/business/ms-environment-select/index.vue';
  import api from './api/index.vue';
  import apiCase from './case/index.vue';
  import apiMethodName from '@/views/api-test/components/apiMethodName.vue';
  import { RequestParam } from '@/views/api-test/components/requestComposition/index.vue';

  import { getProtocolList } from '@/api/modules/api-test/common';
  import { useI18n } from '@/hooks/useI18n';
  import useLeaveTabUnSaveCheck from '@/hooks/useLeaveTabUnSaveCheck';
  import useRequestCompositionStore from '@/store/modules/api/requestComposition';
  import useAppStore from '@/store/modules/app';
  import useCacheStore from '@/store/modules/cache/cache';
  import { hasAnyPermission } from '@/utils/permission';

  import { ProtocolItem } from '@/models/apiTest/common';
  import { ApiCaseDetail } from '@/models/apiTest/management';
  import { MockDetail } from '@/models/apiTest/mock';
  import { ModuleTreeNode } from '@/models/common';
  import {
    RequestAuthType,
    RequestComposition,
    RequestDefinitionStatus,
    RequestMethods,
    ResponseComposition,
  } from '@/enums/apiEnum';
  import { CacheTabTypeEnum } from '@/enums/cacheTabEnum';

  import { defaultBodyParams, defaultResponse, defaultResponseItem } from '@/views/api-test/components/config';

  const MockTable = defineAsyncComponent(
    () => import('@/views/api-test/management/components/management/mock/mockTable.vue')
  );

  const props = defineProps<{
    activeModule: string;
    offspringIds: string[];
    selectedProtocols: string[];
    moduleTree: ModuleTreeNode[]; // 模块树
  }>();
  const emit = defineEmits<{
    (e: 'import'): void;
    (e: 'handleAdvSearch', isStartAdvance: boolean): void;
  }>();
  const appStore = useAppStore();
  const route = useRoute();
  const { t } = useI18n();
  const requestCompositionStore = useRequestCompositionStore();
  const cacheStore = useCacheStore();

  const setActiveApi: ((params: RequestParam) => void) | undefined = inject('setActiveApi');
  const currentTab = ref('api');
  const tabOptions = [
    { label: 'API', value: 'api' },
    ...(hasAnyPermission(['PROJECT_API_DEFINITION_CASE:READ']) ? [{ label: 'CASE', value: 'case' }] : []),
    ...(hasAnyPermission(['PROJECT_API_DEFINITION_MOCK:READ']) ? [{ label: 'MOCK', value: 'mock' }] : []),
  ];

  const apiRef = ref<InstanceType<typeof api>>();
  const caseRef = ref<InstanceType<typeof apiCase>>();

  function newTab(apiInfo?: ModuleTreeNode | string, isCopy?: boolean, isExecute?: boolean) {
    if (apiInfo) {
      apiRef.value?.openApiTab({
        apiInfo,
        isCopy,
        isExecute,
      });
    } else {
      apiRef.value?.addApiTab();
    }
  }

  function newCaseTab(id: string) {
    caseRef.value?.openCaseTab(id);
  }

  const apiTabs = ref<RequestParam[]>([
    {
      id: 'all',
      label: t('apiTestManagement.allApi'),
      closable: false,
      moduleId: 'root',
    } as unknown as RequestParam,
  ]);
  const activeApiTab = ref<RequestParam>(apiTabs.value[0] as RequestParam);

  // api下的创建用例弹窗也用到了defaultCaseParams
  const initDefaultId = `case-${Date.now()}`;
  const defaultCaseParams: RequestParam = {
    id: initDefaultId,
    type: 'case',
    moduleId: props.activeModule === 'all' ? 'root' : props.activeModule,
    protocol: 'HTTP',
    tags: [],
    description: '',
    priority: 'P0',
    status: RequestDefinitionStatus.PROCESSING,
    url: '',
    activeTab: RequestComposition.HEADER,
    closable: true,
    method: RequestMethods.GET,
    headers: [],
    body: cloneDeep(defaultBodyParams),
    query: [],
    rest: [],
    polymorphicName: '',
    name: '',
    path: '',
    projectId: '',
    uploadFileIds: [],
    linkFileIds: [],
    authConfig: {
      authType: RequestAuthType.NONE,
      basicAuth: {
        userName: '',
        password: '',
      },
      digestAuth: {
        userName: '',
        password: '',
      },
    },
    children: [
      {
        polymorphicName: 'MsCommonElement', // 协议多态名称，写死MsCommonElement
        assertionConfig: {
          enableGlobal: true,
          assertions: [],
        },
        postProcessorConfig: {
          enableGlobal: true,
          processors: [],
        },
        preProcessorConfig: {
          enableGlobal: true,
          processors: [],
        },
      },
    ],
    otherConfig: {
      connectTimeout: 60000,
      responseTimeout: 60000,
      certificateAlias: '',
      followRedirects: true,
      autoRedirects: false,
    },
    responseActiveTab: ResponseComposition.BODY,
    response: cloneDeep(defaultResponse),
    responseDefinition: [cloneDeep(defaultResponseItem)],
    isNew: true,
    unSaved: false,
    executeLoading: false,
    preDependency: [], // 前置依赖
    postDependency: [], // 后置依赖
    errorMessageInfo: {},
  };

  // 监听模块树的激活节点变化，记录表格数据的模块 id
  watch(
    () => props.activeModule,
    (val) => {
      const defaultTab = apiTabs.value.find((item) => item.id === 'all');
      if (defaultTab) {
        defaultTab.moduleId = val;
      }
    }
  );

  // 切换到第一个tab
  function changeActiveApiTabToFirst() {
    activeApiTab.value = apiTabs.value[0] as RequestParam;
  }

  // 下拉框切换
  function currentTabChange(val: any) {
    if (val === 'api') {
      apiTabs.value[0].label = t('apiTestManagement.allApi');
    } else if (val === 'case') {
      apiTabs.value[0].label = t('case.allCase');
    } else {
      apiTabs.value[0].label = t('mockManagement.allMock');
    }
    changeActiveApiTabToFirst();
    emit('handleAdvSearch', false);
  }

  watch(
    () => activeApiTab.value.id,
    () => {
      if (typeof setActiveApi === 'function' && !activeApiTab.value.isNew && activeApiTab.value.type === 'api') {
        // 打开的 tab 是接口详情的 tab 才需要同步设置模块树的激活节点
        setActiveApi(activeApiTab.value);
      }
    }
  );

  function refreshApiTable() {
    apiRef.value?.refreshTable();
  }

  /**
   * 同步模块树的接口信息更新操作
   */
  function handleApiUpdateFromModuleTree(newInfo: { id: string; name: string; moduleId?: string; [key: string]: any }) {
    apiTabs.value = apiTabs.value.map((item) => {
      if (item.id === newInfo.id) {
        item.label = newInfo.name;
        item.name = newInfo.name;
        if (newInfo.moduleId) {
          item.moduleId = newInfo.moduleId;
        }
      }
      return item;
    });
    if (activeApiTab.value.id === newInfo.id) {
      activeApiTab.value.label = newInfo.name;
      activeApiTab.value.name = newInfo.name;
      if (newInfo.moduleId) {
        activeApiTab.value.moduleId = newInfo.moduleId;
      }
    }
  }

  /**
   * 同步模块树的接口信息删除操作
   * @param id 接口 id
   * @param isModule 是否是删除模块
   */
  function handleDeleteApiFromModuleTree(id: string, isModule = false) {
    if (activeApiTab.value.id === 'all') {
      apiRef.value?.refreshTable();
    } else if (isModule) {
      // 删除整个模块
      apiTabs.value = apiTabs.value.filter((item) => {
        if (activeApiTab.value.id === item.id) {
          // 删除的是当前激活的 tab, 切换到第一个 tab
          [activeApiTab.value] = apiTabs.value;
        }
        return item.moduleId !== id || item.id === 'all';
      });
    } else {
      // 删除单个 api
      apiTabs.value = apiTabs.value.filter((item) => item.id !== id);
      if (activeApiTab.value.id === id) {
        [activeApiTab.value] = apiTabs.value;
      }
    }
  }

  const protocols = ref<ProtocolItem[]>([]);
  async function initProtocolList() {
    try {
      protocols.value = await getProtocolList(appStore.currentOrgId);
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  function handleMockDebug(mock: MockDetail) {
    apiRef.value?.openApiTabAndDebugMock(mock);
  }

  function handleTabClose(item: TabItem) {
    requestCompositionStore.removePluginFormMapItem(item.id);
    const closingIndex = apiTabs.value.findIndex((e) => e.id === item.id);
    if (closingIndex > -1) {
      apiTabs.value.splice(closingIndex, 1);
    }
  }

  onBeforeMount(() => {
    initProtocolList();
    if ((route.query.tab as string) === 'case') {
      currentTab.value = 'case';
      currentTabChange('case');
    } else if ((route.query.tab as string) === 'mock') {
      currentTab.value = 'mock';
      currentTabChange('mock');
    }
  });

  useLeaveTabUnSaveCheck(apiTabs.value, [
    'PROJECT_API_DEFINITION:READ+ADD',
    'PROJECT_API_DEFINITION:READ+UPDATE',
    'PROJECT_API_DEFINITION_CASE:READ+ADD',
    'PROJECT_API_DEFINITION_CASE:READ+UPDATE',
  ]);

  const isAdvancedSearchMode = computed(() =>
    currentTab.value === 'api' ? apiRef.value?.isAdvancedSearchMode : caseRef.value?.isAdvancedSearchMode
  );

  /** 向孙组件提供属性 */
  provide('defaultCaseParams', readonly(defaultCaseParams));
  provide('protocols', readonly(protocols));

  defineExpose({
    newTab,
    newCaseTab,
    refreshApiTable,
    handleApiUpdateFromModuleTree,
    handleDeleteApiFromModuleTree,
    changeActiveApiTabToFirst,
    isAdvancedSearchMode,
  });
</script>

<style lang="less" scoped>
  .ms-input-group--prepend();
</style>
