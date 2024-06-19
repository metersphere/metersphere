<template>
  <div class="flex flex-1 flex-col overflow-hidden">
    <div v-if="activeApiTab.id === 'all' && currentTab === 'api'" class="flex-1 pt-[8px]">
      <apiTable
        :active-module="props.activeModule"
        :offspring-ids="props.offspringIds"
        :selected-protocols="props.selectedProtocols"
        :refresh-time-stamp="refreshTableTimeStamp"
        :member-options="memberOptions"
        @open-api-tab="(record, isExecute) => openApiTab({ apiInfo: record, isCopy: false, isExecute })"
        @open-copy-api-tab="openApiTab({ apiInfo: $event, isCopy: true })"
        @add-api-tab="addApiTab"
        @import="() => emit('import')"
        @open-edit-api-tab="openApiTab"
      />
    </div>
    <div v-if="activeApiTab.id !== 'all'" class="flex-1 overflow-hidden">
      <a-tabs
        v-model:active-key="activeApiTab.definitionActiveKey"
        animation
        lazy-load
        class="ms-api-tab-nav"
        @change="changeDefinitionActiveKey"
      >
        <template v-if="activeApiTab.definitionActiveKey === 'preview'" #extra>
          <div class="flex gap-[12px] pr-[16px]">
            <executeButton v-permission="['PROJECT_API_DEFINITION:READ+EXECUTE']" @execute="toExecuteDefinition" />
            <a-dropdown-button
              v-permission="['PROJECT_API_DEFINITION:READ+UPDATE']"
              type="outline"
              @click="toEditDefinition"
            >
              {{ t('common.edit') }}
              <template #icon>
                <icon-down />
              </template>
              <template #content>
                <a-doption
                  v-permission="['PROJECT_API_DEFINITION:READ+DELETE']"
                  value="delete"
                  class="error-6 text-[rgb(var(--danger-6))]"
                  @click="handleDelete"
                >
                  <MsIcon type="icon-icon_delete-trash_outlined" class="text-[rgb(var(--danger-6))]" />
                  {{ t('common.delete') }}
                </a-doption>
              </template>
            </a-dropdown-button>
          </div>
        </template>
        <a-tab-pane
          v-if="!activeApiTab.isNew"
          key="preview"
          :title="t('apiTestManagement.preview')"
          class="ms-api-tab-pane"
        >
          <preview
            v-if="activeApiTab.definitionActiveKey === 'preview'"
            :detail="activeApiTab"
            :protocols="protocols"
            @update-follow="activeApiTab.follow = !activeApiTab.follow"
          />
        </a-tab-pane>
        <a-tab-pane
          v-if="hasAnyPermission(['PROJECT_API_DEFINITION:READ+UPDATE', 'PROJECT_API_DEFINITION:READ+ADD'])"
          key="definition"
          :title="t('apiTestManagement.definition')"
          class="ms-api-tab-pane"
        >
          <requestComposition
            ref="requestCompositionRef"
            v-model:detail-loading="loading"
            v-model:request="activeApiTab"
            :module-tree="props.moduleTree"
            :create-api="addDefinition"
            :update-api="updateDefinition"
            :execute-api="debugDefinition"
            :local-execute-api="localExecuteApiDebug"
            :permission-map="{
              execute: 'PROJECT_API_DEFINITION:READ+EXECUTE',
              update: 'PROJECT_API_DEFINITION:READ+UPDATE',
              create: 'PROJECT_API_DEFINITION:READ+ADD',
            }"
            :upload-temp-file-api="uploadTempFile"
            :file-save-as-source-id="activeApiTab.id"
            :file-module-options-api="getTransferOptions"
            :file-save-as-api="transferFile"
            is-definition
            @add-done="handleAddDone"
          />
        </a-tab-pane>
        <a-tab-pane
          v-if="!activeApiTab.isNew && hasAnyPermission(['PROJECT_API_DEFINITION_CASE:READ'])"
          key="case"
          :title="t('apiTestManagement.case')"
          class="ms-api-tab-pane"
        >
          <caseTable
            ref="caseTableRef"
            :is-api="true"
            :active-module="props.activeModule"
            :selected-protocols="[activeApiTab.protocol]"
            :api-detail="activeApiTab"
            :offspring-ids="props.offspringIds"
            :member-options="memberOptions"
            :height-used="48"
          />
        </a-tab-pane>
        <a-tab-pane
          v-if="!activeApiTab.isNew && activeApiTab.protocol === 'HTTP'"
          key="mock"
          title="MOCK"
          class="ms-api-tab-pane"
        >
          <mockTable
            :active-module="props.activeModule"
            :offspring-ids="props.offspringIds"
            :definition-detail="activeApiTab"
            :selected-protocols="[activeApiTab.protocol]"
            :height-used="48"
            is-api
            @debug="openApiTabAndDebugMock"
          />
        </a-tab-pane>
      </a-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { cloneDeep } from 'lodash-es';

  import { TabItem } from '@/components/pure/ms-editable-tab/types';
  // import MsFormCreate from '@/components/pure/ms-form-create/formCreate.vue';
  import apiTable from './apiTable.vue';
  import executeButton from '@/views/api-test/components/executeButton.vue';

  import { getProtocolList, localExecuteApiDebug } from '@/api/modules/api-test/common';
  import {
    addDefinition,
    debugDefinition,
    deleteDefinition,
    getDefinitionDetail,
    getTransferOptions,
    transferFile,
    updateDefinition,
    uploadTempFile,
  } from '@/api/modules/api-test/management';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import useAppStore from '@/store/modules/app';
  import useUserStore from '@/store/modules/user';
  import { hasAnyPermission } from '@/utils/permission';

  import { ProtocolItem } from '@/models/apiTest/common';
  import { ApiDefinitionDetail } from '@/models/apiTest/management';
  import { MockDetail } from '@/models/apiTest/mock';
  import { ModuleTreeNode } from '@/models/common';
  import {
    RequestAuthType,
    RequestBodyFormat,
    RequestComposition,
    RequestDefinitionStatus,
    RequestMethods,
    RequestParamsType,
    ResponseComposition,
  } from '@/enums/apiEnum';

  import {
    defaultBodyParams,
    defaultBodyParamsItem,
    defaultHeaderParamsItem,
    defaultRequestParamsItem,
    defaultResponse,
    defaultResponseItem,
  } from '@/views/api-test/components/config';
  import type { RequestParam } from '@/views/api-test/components/requestComposition/index.vue';
  import { parseRequestBodyFiles } from '@/views/api-test/components/utils';
  // 懒加载requestComposition组件
  const requestComposition = defineAsyncComponent(
    () => import('@/views/api-test/components/requestComposition/index.vue')
  );
  const preview = defineAsyncComponent(() => import('./preview/index.vue'));
  const mockTable = defineAsyncComponent(() => import('../mock/mockTable.vue'));
  const caseTable = defineAsyncComponent(() => import('../case/caseTable.vue'));

  const props = defineProps<{
    activeModule: string;
    offspringIds: string[];
    moduleTree: ModuleTreeNode[]; // 模块树
    selectedProtocols: string[];
    currentTab: string;
    memberOptions: { label: string; value: string }[];
  }>();

  const emit = defineEmits<{
    (e: 'deleteApi', id: string): void;
    (e: 'import'): void;
  }>();

  const userStore = useUserStore();
  const appStore = useAppStore();
  const { t } = useI18n();
  const { openModal } = useModal();

  const refreshModuleTree: (() => Promise<any>) | undefined = inject('refreshModuleTree');
  const apiTabs = defineModel<RequestParam[]>('apiTabs', {
    required: true,
  });
  const activeApiTab = defineModel<RequestParam>('activeApiTab', {
    required: true,
  });

  const protocols = ref<ProtocolItem[]>([]);
  async function initProtocolList() {
    try {
      protocols.value = await getProtocolList(appStore.currentOrgId);
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  onBeforeMount(() => {
    initProtocolList();
  });

  const initDefaultId = `definition-${Date.now()}`;
  const localProtocol = localStorage.getItem('currentProtocol');
  const defaultDefinitionParams: RequestParam = {
    type: 'api',
    definitionActiveKey: 'definition',
    id: initDefaultId,
    moduleId: props.activeModule === 'all' ? 'root' : props.activeModule,
    protocol: localProtocol || 'HTTP',
    tags: [],
    status: RequestDefinitionStatus.PROCESSING,
    description: '',
    url: '',
    activeTab: RequestComposition.HEADER,
    label: t('apiTestDebug.newApi'),
    closable: true,
    method: RequestMethods.GET,
    unSaved: false,
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
    mode: 'definition',
    executeLoading: false,
    preDependency: [], // 前置依赖
    postDependency: [], // 后置依赖
    errorMessageInfo: {},
  };

  function addApiTab(defaultProps?: Partial<TabItem>) {
    const id = `definition-${Date.now()}`;
    const protocol = localStorage.getItem('currentProtocol');
    apiTabs.value.push({
      ...cloneDeep(defaultDefinitionParams),
      moduleId: props.activeModule === 'all' ? 'root' : props.activeModule,
      label: t('apiTestManagement.newApi'),
      id,
      isNew: !defaultProps?.id, // 新开的tab标记为前端新增的调试，因为此时都已经有id了；但是如果是查看打开的会有携带id
      definitionActiveKey: !defaultProps ? 'definition' : 'preview',
      protocol: protocol || activeApiTab.value.protocol || defaultDefinitionParams.protocol, // 新开的tab默认使用当前激活的tab的协议
      ...defaultProps,
    });
    activeApiTab.value = apiTabs.value[apiTabs.value.length - 1];
  }

  const caseTableRef = ref<InstanceType<typeof caseTable>>();
  const refreshTableTimeStamp = ref(0);

  watch(
    () => activeApiTab.value.id,
    (id) => {
      if (id === 'all') {
        refreshTableTimeStamp.value = Date.now();
      } else if (activeApiTab.value.definitionActiveKey === 'case') {
        caseTableRef.value?.loadCaseList();
      }
    }
  );

  const loading = ref(false);
  const requestCompositionRef = ref<InstanceType<typeof requestComposition>>();
  async function openApiTab(options: {
    apiInfo: ModuleTreeNode | ApiDefinitionDetail | string;
    isCopy?: boolean;
    isExecute?: boolean;
    isEdit?: boolean;
    isDebugMock?: boolean;
  }) {
    const { apiInfo, isCopy = false, isExecute = false, isEdit = false, isDebugMock = false } = options;
    const isLoadedTabIndex = apiTabs.value.findIndex(
      (e) => e.id === (typeof apiInfo === 'string' ? apiInfo : apiInfo.id)
    );
    if (isLoadedTabIndex > -1 && !isCopy) {
      const preActiveApiTabId = activeApiTab.value.id;
      let loadedApiTab = apiTabs.value[isLoadedTabIndex] as RequestParam;
      if (isDebugMock) {
        const mockEnvId = appStore.envList.find((e) => e.mock)?.id;
        if (mockEnvId) {
          appStore.showLoading();
          await appStore.setEnvConfig(mockEnvId);
          appStore.hideLoading();
        }
        loadedApiTab = {
          ...loadedApiTab,
          ...(apiInfo as ApiDefinitionDetail).request,
          activeTab: (apiInfo as ApiDefinitionDetail).activeTab,
        };
      }
      // 如果点击的请求在tab中已经存在，则直接切换到该tab
      activeApiTab.value = {
        ...loadedApiTab,
        definitionActiveKey: isCopy || isExecute || isEdit ? 'definition' : 'preview',
        isExecute,
        mode: isExecute ? 'debug' : 'definition',
      };
      // requestCompositionRef里监听的是id,所以id相等的时候需要单独调执行
      if (preActiveApiTabId === apiTabs.value[isLoadedTabIndex].id) {
        requestCompositionRef.value?.execute(userStore.isPriorityLocalExec ? 'localExec' : 'serverExec');
      }
      return;
    }
    try {
      appStore.showLoading();
      loading.value = true;
      const res = await getDefinitionDetail(typeof apiInfo === 'string' ? apiInfo : apiInfo.id);
      appStore.hideLoading();
      let name = isCopy ? `copy_${res.name}` : res.name;
      if (name.length > 255) {
        name = name.slice(0, 255);
      }
      let parseRequestBodyResult;
      if (res.protocol === 'HTTP') {
        parseRequestBodyResult = parseRequestBodyFiles(res.request.body, res.response); // 解析请求体中的文件，将详情中的文件 id 集合收集，更新时以判断文件是否删除以及是否新上传的文件
      }
      let { request } = res;
      if (isDebugMock) {
        const mockEnvId = appStore.envList.find((e) => e.mock)?.id;
        if (mockEnvId) {
          await appStore.setEnvConfig(mockEnvId);
        }
        request = {
          ...res.request,
          ...(apiInfo as ApiDefinitionDetail).request,
          activeTab: (apiInfo as ApiDefinitionDetail).activeTab,
        };
      }
      addApiTab({
        label: name,
        ...res,
        ...request,
        response: cloneDeep(defaultResponse),
        responseDefinition: res.response.map((e) => ({ ...e, responseActiveTab: ResponseComposition.BODY })),
        url: res.path,
        name, // request里面还有个name但是是null
        isNew: isCopy,
        unSaved: isCopy,
        isCopy,
        id: isCopy ? new Date().getTime() : res.id,
        isExecute,
        mode: isExecute ? 'debug' : 'definition',
        definitionActiveKey: isCopy || isExecute || isEdit ? 'definition' : 'preview',
        ...parseRequestBodyResult,
      });
      nextTick(() => {
        // 等待内容渲染出来再隐藏loading
        loading.value = false;
      });
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
      loading.value = false;
      appStore.hideLoading();
    }
  }

  async function openApiTabAndDebugMock(mock: MockDetail) {
    let activeTab = RequestComposition.BODY;
    if (mock.mockMatchRule.body.bodyType !== RequestBodyFormat.NONE) {
      activeTab = RequestComposition.BODY;
    } else if (mock.mockMatchRule.header.matchRules.length > 0) {
      activeTab = RequestComposition.HEADER;
    } else if (mock.mockMatchRule.query.matchRules.length > 0) {
      activeTab = RequestComposition.QUERY;
    } else if (mock.mockMatchRule.rest.matchRules.length > 0) {
      activeTab = RequestComposition.REST;
    }
    openApiTab({
      apiInfo: {
        id: mock.apiDefinitionId as string,
        request: {
          body: {
            bodyType: mock.mockMatchRule.body.bodyType,
            binaryBody: mock.mockMatchRule.body.binaryBody,
            rawBody: mock.mockMatchRule.body.rawBody,
            xmlBody: mock.mockMatchRule.body.xmlBody,
            wwwFormBody: {
              formValues:
                mock.mockMatchRule.body.wwwFormBody.matchRules.map((e) => ({
                  ...defaultBodyParamsItem,
                  key: e.key,
                  value: e.value,
                })) || [],
            },
            jsonBody: mock.mockMatchRule.body.jsonBody,
            formDataBody: {
              formValues:
                mock.mockMatchRule.body.formDataBody.matchRules.map((e) => ({
                  ...defaultBodyParamsItem,
                  key: e.key,
                  value: e.value,
                  files: e.files || [],
                  paramType: e.files && e.files.length > 0 ? RequestParamsType.FILE : defaultBodyParamsItem.paramType,
                })) || [],
            },
          },
          headers:
            mock.mockMatchRule.header.matchRules.map((e) => ({
              ...defaultHeaderParamsItem,
              key: e.key,
              value: e.value,
            })) || [],
          query:
            mock.mockMatchRule.query.matchRules.map((e) => ({
              ...defaultRequestParamsItem,
              key: e.key,
              value: e.value,
            })) || [],
          rest:
            mock.mockMatchRule.rest.matchRules.map((e) => ({
              ...defaultRequestParamsItem,
              key: e.key,
              value: e.value,
            })) || [],
        },
        activeTab,
      } as unknown as ApiDefinitionDetail,
      isExecute: true,
      isDebugMock: true,
    });
  }

  // 新建接口后没有创建人，创建时间，更新时间的信息。所以需要刷新数据
  watch(
    () => activeApiTab.value.isNew,
    async (newValue, oldValue) => {
      // isNew从true变成了false
      if (oldValue && !newValue && activeApiTab.value.id !== 'all') {
        const res = await getDefinitionDetail(activeApiTab.value.id);
        activeApiTab.value.createUserName = res.createUserName;
        activeApiTab.value.updateTime = res.updateTime;
        activeApiTab.value.createTime = res.createTime;
      }
    }
  );

  function handleAddDone() {
    if (typeof refreshModuleTree === 'function') {
      refreshModuleTree();
    }
  }

  function refreshTable() {
    refreshTableTimeStamp.value = Date.now();
  }

  function changeDefinitionActiveKey(val: string | number) {
    // 在定义可以添加用例，故需要切换到case时刷新数据
    if (val === 'case') {
      caseTableRef.value?.loadCaseList();
    }
  }

  // 跳转到接口定义tab
  function toEditDefinition() {
    activeApiTab.value.definitionActiveKey = 'definition';
    activeApiTab.value.mode = 'definition';
  }

  // 跳转到接口定义tab，且执行
  function toExecuteDefinition(executeType?: 'localExec' | 'serverExec') {
    activeApiTab.value.definitionActiveKey = 'definition';
    activeApiTab.value.isExecute = true;
    activeApiTab.value.mode = 'debug';
    requestCompositionRef.value?.execute(executeType);
  }

  function handleDelete() {
    openModal({
      type: 'error',
      title: t('apiTestManagement.deleteApiTipTitle', { name: activeApiTab.value.name }),
      content: t('apiTestManagement.deleteApiTip'),
      okText: t('common.confirmDelete'),
      cancelText: t('common.cancel'),
      okButtonProps: {
        status: 'danger',
      },
      maskClosable: false,
      onBeforeOk: async () => {
        try {
          await deleteDefinition(activeApiTab.value.id as string);
          emit('deleteApi', activeApiTab.value.id as string);
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        }
      },
      hideCancel: false,
    });
  }

  defineExpose({
    openApiTab,
    addApiTab,
    openApiTabAndDebugMock,
    refreshTable,
  });
</script>

<style lang="less" scoped>
  .error-6 {
    color: rgb(var(--danger-6));
    &:hover {
      color: rgb(var(--danger-6));
    }
  }
  :deep(.ms-api-tab-nav) {
    @apply h-full;
    .arco-tabs {
      @apply border-b-0;
    }
    .arco-tabs-nav {
      border-bottom: 1px solid var(--color-text-n8);
    }
    .arco-tabs-content {
      @apply pt-0;

      height: calc(100% - 48px);
      .arco-tabs-content-list {
        @apply h-full;
        .arco-tabs-pane {
          @apply h-full;
        }
      }
    }
  }
</style>
