<template>
  <div class="flex flex-1 flex-col overflow-hidden">
    <div v-show="activeApiTab.id === 'all'" class="flex-1 pt-[16px]">
      <apiTable
        ref="apiTableRef"
        :active-module="props.activeModule"
        :offspring-ids="props.offspringIds"
        :protocol="props.protocol"
        @open-api-tab="(record, isExecute) => openApiTab(record, false, isExecute)"
        @open-copy-api-tab="openApiTab($event, true)"
      />
    </div>
    <div v-if="activeApiTab.id !== 'all'" class="flex-1 overflow-hidden">
      <a-tabs v-model:active-key="activeApiTab.definitionActiveKey" animation lazy-load class="ms-api-tab-nav">
        <a-tab-pane
          v-if="!activeApiTab.isNew"
          key="preview"
          :title="t('apiTestManagement.preview')"
          class="ms-api-tab-pane"
        >
          <preview
            v-if="activeApiTab.definitionActiveKey === 'preview'"
            :detail="activeApiTab"
            :module-tree="props.moduleTree"
            :protocols="protocols"
            @update-follow="activeApiTab.follow = !activeApiTab.follow"
          />
        </a-tab-pane>
        <a-tab-pane key="definition" :title="t('apiTestManagement.definition')" class="ms-api-tab-pane">
          <requestComposition
            v-model:detail-loading="loading"
            v-model:request="activeApiTab"
            :module-tree="props.moduleTree"
            hide-response-layout-switch
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
            :current-env-config="currentEnvConfig"
            is-definition
            @add-done="handleAddDone"
          />
        </a-tab-pane>
        <a-tab-pane v-if="!activeApiTab.isNew" key="case" :title="t('apiTestManagement.case')" class="ms-api-tab-pane">
          <caseTable
            :is-api="true"
            :active-module="props.activeModule"
            :protocol="activeApiTab.protocol"
            :api-detail="activeApiTab"
          />
        </a-tab-pane>
        <!-- <a-tab-pane v-if="!activeApiTab.isNew" key="mock" title="MOCK" class="ms-api-tab-pane"> </a-tab-pane> -->
      </a-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { cloneDeep } from 'lodash-es';

  // import MsButton from '@/components/pure/ms-button/index.vue';
  import { TabItem } from '@/components/pure/ms-editable-tab/types';
  import caseTable from '../case/caseTable.vue';
  // import MsFormCreate from '@/components/pure/ms-form-create/formCreate.vue';
  import apiTable from './apiTable.vue';

  import { getProtocolList, localExecuteApiDebug } from '@/api/modules/api-test/common';
  import {
    addDefinition,
    debugDefinition,
    getDefinitionDetail,
    getTransferOptions,
    transferFile,
    updateDefinition,
    uploadTempFile,
  } from '@/api/modules/api-test/management';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';

  import { ProtocolItem } from '@/models/apiTest/common';
  import { ApiDefinitionDetail } from '@/models/apiTest/management';
  import { ModuleTreeNode } from '@/models/common';
  import { EnvConfig } from '@/models/projectManagement/environmental';
  import {
    RequestAuthType,
    RequestComposition,
    RequestDefinitionStatus,
    RequestMethods,
    ResponseComposition,
  } from '@/enums/apiEnum';

  import { defaultBodyParams, defaultResponse, defaultResponseItem } from '@/views/api-test/components/config';
  import type { RequestParam } from '@/views/api-test/components/requestComposition/index.vue';
  import { parseRequestBodyFiles } from '@/views/api-test/components/utils';
  // 懒加载requestComposition组件
  const requestComposition = defineAsyncComponent(
    () => import('@/views/api-test/components/requestComposition/index.vue')
  );
  const preview = defineAsyncComponent(() => import('./preview/index.vue'));

  const props = defineProps<{
    activeModule: string;
    offspringIds: string[];
    moduleTree: ModuleTreeNode[]; // 模块树
    protocol: string;
  }>();

  const refreshModuleTree: (() => Promise<any>) | undefined = inject('refreshModuleTree');

  const currentEnvConfig = inject<Ref<EnvConfig>>('currentEnvConfig');

  const appStore = useAppStore();
  const { t } = useI18n();

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
  const defaultDefinitionParams: RequestParam = {
    type: 'api',
    definitionActiveKey: 'definition',
    id: initDefaultId,
    moduleId: props.activeModule === 'all' ? 'root' : props.activeModule,
    protocol: 'HTTP',
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
          enableGlobal: false,
          assertions: [],
        },
        postProcessorConfig: {
          enableGlobal: false,
          processors: [],
        },
        preProcessorConfig: {
          enableGlobal: false,
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
  };

  function addApiTab(defaultProps?: Partial<TabItem>) {
    const id = `definition-${Date.now()}`;
    apiTabs.value.push({
      ...cloneDeep(defaultDefinitionParams),
      moduleId: props.activeModule === 'all' ? 'root' : props.activeModule,
      label: t('apiTestManagement.newApi'),
      id,
      isNew: !defaultProps?.id, // 新开的tab标记为前端新增的调试，因为此时都已经有id了；但是如果是查看打开的会有携带id
      definitionActiveKey: !defaultProps ? 'definition' : 'preview',
      ...defaultProps,
    });
    activeApiTab.value = apiTabs.value[apiTabs.value.length - 1];
  }

  const apiTableRef = ref<InstanceType<typeof apiTable>>();

  watch(
    () => activeApiTab.value.id,
    (id) => {
      if (id === 'all') {
        apiTableRef.value?.loadApiList();
      }
    }
  );

  const loading = ref(false);
  async function openApiTab(apiInfo: ModuleTreeNode | ApiDefinitionDetail | string, isCopy = false, isExecute = false) {
    const isLoadedTabIndex = apiTabs.value.findIndex(
      (e) => e.id === (typeof apiInfo === 'string' ? apiInfo : apiInfo.id)
    );
    if (isLoadedTabIndex > -1 && !isCopy) {
      // 如果点击的请求在tab中已经存在，则直接切换到该tab
      activeApiTab.value = {
        ...(apiTabs.value[isLoadedTabIndex] as RequestParam),
        isExecute,
        mode: isExecute ? 'debug' : 'definition',
      };
      return;
    }
    try {
      loading.value = true;
      const res = await getDefinitionDetail(typeof apiInfo === 'string' ? apiInfo : apiInfo.id);
      const name = isCopy ? `copy-${res.name}` : res.name;
      let parseRequestBodyResult;
      if (res.protocol === 'HTTP') {
        parseRequestBodyResult = parseRequestBodyFiles(res.request.body); // 解析请求体中的文件，将详情中的文件 id 集合收集，更新时以判断文件是否删除以及是否新上传的文件
      }
      addApiTab({
        label: name,
        ...res.request,
        ...res,
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
        definitionActiveKey: isCopy || isExecute ? 'definition' : 'preview',
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
    }
  }

  function handleAddDone() {
    if (typeof refreshModuleTree === 'function') {
      refreshModuleTree();
    }
  }

  function refreshTable() {
    apiTableRef.value?.loadApiList();
  }

  defineExpose({
    openApiTab,
    addApiTab,
    refreshTable,
  });
</script>

<style lang="less" scoped>
  :deep(.ms-api-tab-nav) {
    @apply h-full;
    .arco-tabs-nav-tab {
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
