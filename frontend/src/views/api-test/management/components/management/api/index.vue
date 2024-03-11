<template>
  <div class="flex h-full flex-col">
    <div class="border-b border-[var(--color-text-n8)] px-[22px] pb-[16px]">
      <MsEditableTab
        v-model:active-tab="activeApiTab"
        v-model:tabs="apiTabs"
        @add="addApiTab"
        @change="handleActiveTabChange"
      >
        <template #label="{ tab }">
          <apiMethodName
            v-if="tab.id !== 'all'"
            :method="tab.protocol === 'HTTP' ? tab.method : tab.protocol"
            class="mr-[4px]"
          />
          <a-tooltip :content="tab.name || tab.label" :mouse-enter-delay="500">
            <div class="one-line-text max-w-[144px]">
              {{ tab.name || tab.label }}
            </div>
          </a-tooltip>
        </template>
      </MsEditableTab>
    </div>
    <div v-show="activeApiTab.id === 'all'" class="flex-1">
      <apiTable
        ref="apiTableRef"
        :active-module="props.activeModule"
        :offspring-ids="props.offspringIds"
        :protocol="props.protocol"
        @open-api-tab="openApiTab"
        @open-copy-api-tab="openApiTab($event, true)"
      />
    </div>
    <div v-if="activeApiTab.id !== 'all'" class="flex-1 overflow-hidden">
      <a-tabs v-model:active-key="definitionActiveKey" animation lazy-load class="ms-api-tab-nav">
        <a-tab-pane
          v-if="!activeApiTab.isNew"
          key="preview"
          :title="t('apiTestManagement.preview')"
          class="ms-api-tab-pane"
        >
          <preview
            v-if="definitionActiveKey === 'preview'"
            :detail="activeApiTab"
            :module-tree="props.moduleTree"
            :protocols="protocols"
            @update-follow="activeApiTab.follow = !activeApiTab.follow"
          />
        </a-tab-pane>
        <a-tab-pane key="definition" :title="t('apiTestManagement.definition')" class="ms-api-tab-pane">
          <MsSplitBox
            ref="splitBoxRef"
            :size="0.7"
            :max="0.9"
            :min="0.7"
            direction="horizontal"
            expand-direction="right"
          >
            <template #first>
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
                is-definition
                @add-done="emit('addDone')"
                @save="handleSave"
                @save-as-case="handleSaveAsCase"
              />
            </template>
            <template #second>
              <div class="p-[18px]">
                <!-- TODO:第一版没有模板 -->
                <!-- <MsFormCreate v-model:api="fApi" :rule="currentApiTemplateRules" :option="options" /> -->
                <a-form ref="activeApiTabFormRef" :model="activeApiTab" layout="vertical">
                  <a-form-item
                    field="name"
                    :label="t('apiTestManagement.apiName')"
                    class="mb-[16px]"
                    :rules="[{ required: true, message: t('apiTestManagement.apiNameRequired') }]"
                  >
                    <a-input
                      v-model:model-value="activeApiTab.name"
                      :max-length="255"
                      :placeholder="t('apiTestManagement.apiNamePlaceholder')"
                      allow-clear
                      @change="handleActiveApiChange"
                    />
                  </a-form-item>
                  <a-form-item :label="t('apiTestManagement.belongModule')" class="mb-[16px]">
                    <a-tree-select
                      v-model:modelValue="activeApiTab.moduleId"
                      :data="selectTree"
                      :field-names="{ title: 'name', key: 'id', children: 'children' }"
                      :tree-props="{
                        virtualListProps: {
                          height: 200,
                          threshold: 200,
                        },
                      }"
                      allow-search
                      @change="handleActiveApiChange"
                    />
                  </a-form-item>
                  <a-form-item :label="t('apiTestManagement.apiStatus')" class="mb-[16px]">
                    <a-select
                      v-model:model-value="activeApiTab.status"
                      :placeholder="t('common.pleaseSelect')"
                      class="param-input w-full"
                      @change="handleActiveApiChange"
                    >
                      <template #label>
                        <apiStatus :status="activeApiTab.status" />
                      </template>
                      <a-option v-for="item of Object.values(RequestDefinitionStatus)" :key="item" :value="item">
                        <apiStatus :status="item" />
                      </a-option>
                    </a-select>
                  </a-form-item>
                  <a-form-item :label="t('common.tag')" class="mb-[16px]">
                    <MsTagsInput v-model:model-value="activeApiTab.tags" @change="handleActiveApiChange" />
                  </a-form-item>
                  <a-form-item :label="t('common.desc')" class="mb-[16px]">
                    <a-textarea
                      v-model:model-value="activeApiTab.description"
                      :max-length="1000"
                      @change="handleActiveApiChange"
                    />
                  </a-form-item>
                </a-form>
                <!-- TODO:第一版先不做依赖 -->
                <!-- <div class="mb-[8px] flex items-center">
                  <div class="text-[var(--color-text-2)]">
                    {{ t('apiTestManagement.addDependency') }}
                  </div>
                  <a-divider margin="4px" direction="vertical" />
                  <MsButton
                    type="text"
                    class="font-medium"
                    :disabled="activeApiTab.preDependency.length === 0 && activeApiTab.postDependency.length === 0"
                    @click="clearAllDependency"
                  >
                    {{ t('apiTestManagement.clearSelected') }}
                  </MsButton>
                </div>
                <div class="rounded-[var(--border-radius-small)] bg-[var(--color-text-n9)] p-[12px]">
                  <div class="flex items-center">
                    <div class="flex items-center gap-[4px] text-[var(--color-text-2)]">
                      {{ t('apiTestManagement.preDependency') }}
                      <div class="text-[rgb(var(--primary-5))]">
                        {{ activeApiTab.preDependency.length }}
                      </div>
                      {{ t('apiTestManagement.dependencyUnit') }}
                    </div>
                    <a-divider margin="8px" direction="vertical" />
                    <MsButton type="text" class="font-medium" @click="handleDddDependency('pre')">
                      {{ t('apiTestManagement.addPreDependency') }}
                    </MsButton>
                  </div>
                  <div class="mt-[8px] flex items-center">
                    <div class="flex items-center gap-[4px] text-[var(--color-text-2)]">
                      {{ t('apiTestManagement.postDependency') }}
                      <div class="text-[rgb(var(--primary-5))]">
                        {{ activeApiTab.postDependency.length }}
                      </div>
                      {{ t('apiTestManagement.dependencyUnit') }}
                    </div>
                    <a-divider margin="8px" direction="vertical" />
                    <MsButton type="text" class="font-medium" @click="handleDddDependency('post')">
                      {{ t('apiTestManagement.addPostDependency') }}
                    </MsButton>
                  </div>
                </div> -->
              </div>
            </template>
          </MsSplitBox>
        </a-tab-pane>
        <a-tab-pane v-if="!activeApiTab.isNew" key="case" :title="t('apiTestManagement.case')" class="ms-api-tab-pane">
        </a-tab-pane>
        <a-tab-pane v-if="!activeApiTab.isNew" key="mock" title="MOCK" class="ms-api-tab-pane"> </a-tab-pane>
      </a-tabs>
    </div>
  </div>
  <addDependencyDrawer v-model:visible="showAddDependencyDrawer" :mode="addDependencyMode" />
</template>

<script setup lang="ts">
  import { FormInstance, Message, SelectOptionData } from '@arco-design/web-vue';
  import { cloneDeep } from 'lodash-es';

  // import MsButton from '@/components/pure/ms-button/index.vue';
  import MsEditableTab from '@/components/pure/ms-editable-tab/index.vue';
  import { TabItem } from '@/components/pure/ms-editable-tab/types';
  // import MsFormCreate from '@/components/pure/ms-form-create/formCreate.vue';
  import MsSplitBox from '@/components/pure/ms-split-box/index.vue';
  import MsTagsInput from '@/components/pure/ms-tags-input/index.vue';
  import addDependencyDrawer from './addDependencyDrawer.vue';
  import apiTable from './apiTable.vue';
  import apiMethodName from '@/views/api-test/components/apiMethodName.vue';
  import apiStatus from '@/views/api-test/components/apiStatus.vue';

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
  import { filterTree } from '@/utils';

  import { ExecuteBody, ProtocolItem, RequestTaskResult } from '@/models/apiTest/common';
  import {
    ApiDefinitionCreateParams,
    ApiDefinitionDetail,
    ApiDefinitionUpdateParams,
  } from '@/models/apiTest/management';
  import { ModuleTreeNode } from '@/models/common';
  import {
    RequestAuthType,
    RequestBodyFormat,
    RequestComposition,
    RequestDefinitionStatus,
    RequestMethods,
    ResponseComposition,
  } from '@/enums/apiEnum';

  import { defaultResponseItem } from '@/views/api-test/components/config';
  import type { RequestParam } from '@/views/api-test/components/requestComposition/index.vue';
  import { parseRequestBodyFiles } from '@/views/api-test/components/utils';
  // 懒加载requestComposition组件
  const requestComposition = defineAsyncComponent(
    () => import('@/views/api-test/components/requestComposition/index.vue')
  );
  const preview = defineAsyncComponent(() => import('./preview.vue'));

  const props = defineProps<{
    activeModule: string;
    offspringIds: string[];
    moduleTree: ModuleTreeNode[]; // 模块树
    protocol: string;
  }>();
  const emit = defineEmits(['addDone']);
  const definitionActiveKey = ref('definition');
  const setActiveApi: ((params: RequestParam) => void) | undefined = inject('setActiveApi');
  const refreshModuleTree: (() => Promise<any>) | undefined = inject('refreshModuleTree');

  const appStore = useAppStore();
  const { t } = useI18n();

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

  const apiTabs = ref<RequestParam[]>([
    {
      id: 'all',
      label: t('apiTestManagement.allApi'),
      closable: false,
    } as RequestParam,
  ]);
  const activeApiTab = ref<RequestParam>(apiTabs.value[0] as RequestParam);

  function handleActiveApiChange() {
    if (activeApiTab.value) {
      activeApiTab.value.unSaved = true;
    }
  }

  watch(
    () => activeApiTab.value.id,
    () => {
      if (typeof setActiveApi === 'function') {
        setActiveApi(activeApiTab.value);
      }
    }
  );

  const selectTree = computed(() =>
    filterTree(cloneDeep(props.moduleTree), (e) => {
      e.draggable = false;
      return e.type === 'MODULE';
    })
  );

  const initDefaultId = `definition-${Date.now()}`;
  const defaultBodyParams: ExecuteBody = {
    bodyType: RequestBodyFormat.NONE,
    formDataBody: {
      formValues: [],
    },
    wwwFormBody: {
      formValues: [],
    },
    jsonBody: {
      jsonValue: '',
    },
    xmlBody: { value: '' },
    binaryBody: {
      description: '',
      file: undefined,
    },
    rawBody: { value: '' },
  };
  const defaultResponse: RequestTaskResult = {
    requestResults: [
      {
        body: '',
        headers: '',
        method: '',
        url: '',
        responseResult: {
          body: '',
          contentType: '',
          headers: '',
          dnsLookupTime: 0,
          downloadTime: 0,
          latency: 0,
          responseCode: 0,
          responseTime: 0,
          responseSize: 0,
          socketInitTime: 0,
          tcpHandshakeTime: 0,
          transferStartTime: 0,
          sslHandshakeTime: 0,
        },
      },
    ],
    console: '',
  }; // 调试返回的响应内容
  const defaultDefinitionParams: RequestParam = {
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
      ...defaultProps,
    });
    activeApiTab.value = apiTabs.value[apiTabs.value.length - 1];
  }

  const apiTableRef = ref<InstanceType<typeof apiTable>>();

  function handleActiveTabChange(item: TabItem) {
    if (item.id === 'all') {
      apiTableRef.value?.loadApiList();
    }
  }

  const loading = ref(false);
  async function openApiTab(apiInfo: ModuleTreeNode | ApiDefinitionDetail | string, isCopy = false) {
    const isLoadedTabIndex = apiTabs.value.findIndex(
      (e) => e.id === (typeof apiInfo === 'string' ? apiInfo : apiInfo.id)
    );
    if (isLoadedTabIndex > -1 && !isCopy) {
      // 如果点击的请求在tab中已经存在，则直接切换到该tab
      activeApiTab.value = apiTabs.value[isLoadedTabIndex] as RequestParam;
      return;
    }
    try {
      loading.value = true;
      const res = await getDefinitionDetail(typeof apiInfo === 'string' ? apiInfo : apiInfo.id);
      const name = isCopy ? `${res.name}-copy` : res.name;
      definitionActiveKey.value = isCopy ? 'definition' : 'preview';
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

  // const fApi = ref();
  // const options = {
  //   form: {
  //     layout: 'vertical',
  //     labelPosition: 'right',
  //     size: 'small',
  //     labelWidth: '00px',
  //     hideRequiredAsterisk: false,
  //     showMessage: true,
  //     inlineMessage: false,
  //     scrollToFirstError: true,
  //   },
  //   submitBtn: false,
  //   resetBtn: false,
  // };
  // const currentApiTemplateRules = [];
  const showAddDependencyDrawer = ref(false);
  const addDependencyMode = ref<'pre' | 'post'>('pre');

  // function handleDddDependency(value: string | number | Record<string, any> | undefined) {
  //   switch (value) {
  //     case 'pre':
  //       addDependencyMode.value = 'pre';
  //       showAddDependencyDrawer.value = true;
  //       break;
  //     case 'post':
  //       addDependencyMode.value = 'post';
  //       showAddDependencyDrawer.value = true;
  //       break;
  //     default:
  //       break;
  //   }
  // }

  // function clearAllDependency() {
  //   activeApiTab.value.preDependency = [];
  //   activeApiTab.value.postDependency = [];
  // }

  const splitBoxRef = ref<InstanceType<typeof MsSplitBox>>();
  const activeApiTabFormRef = ref<FormInstance>();

  function handleSave(params: ApiDefinitionCreateParams) {
    activeApiTabFormRef.value?.validate(async (errors) => {
      if (errors) {
        splitBoxRef.value?.expand();
      } else {
        try {
          appStore.showLoading();
          let res;
          params.versionId = 'v1.0';
          if (params.isNew) {
            res = await addDefinition(params);
          } else {
            res = await updateDefinition(params as ApiDefinitionUpdateParams);
          }
          activeApiTab.value.id = res.id;
          activeApiTab.value.isNew = false;
          Message.success(t('common.saveSuccess'));
          activeApiTab.value.unSaved = false;
          activeApiTab.value.name = res.name;
          activeApiTab.value.label = res.name;
          activeApiTab.value.url = res.path;
          if (typeof refreshModuleTree === 'function') {
            refreshModuleTree();
          }
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        } finally {
          appStore.hideLoading();
        }
      }
    });
  }

  async function handleSaveAsCase(params: ApiDefinitionCreateParams) {
    console.log(params);
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
  .ms-api-tab-nav {
    @apply h-full;
    :deep(.arco-tabs-content) {
      @apply pt-0;

      height: calc(100% - 51px);
      .arco-tabs-content-list {
        @apply h-full;
        .arco-tabs-pane {
          @apply h-full;
        }
      }
    }
  }
</style>
