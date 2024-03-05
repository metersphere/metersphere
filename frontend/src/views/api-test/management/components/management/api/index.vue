<template>
  <div class="flex h-full flex-col">
    <div class="border-b border-[var(--color-text-n8)] px-[22px] pb-[16px]">
      <MsEditableTab v-model:active-tab="activeApiTab" v-model:tabs="apiTabs" @add="addApiTab">
        <template #label="{ tab }">
          <apiMethodName v-if="tab.id !== 'all'" :method="tab.method" class="mr-[4px]" />
          {{ tab.label }}
        </template>
      </MsEditableTab>
    </div>
    <div v-show="activeApiTab.id === 'all'" class="flex-1">
      <apiTable :active-module="props.activeModule" :offspring-ids="props.offspringIds" @open-api-tab="openApiTab" />
    </div>
    <div v-if="activeApiTab.id !== 'all'" class="flex-1 overflow-hidden">
      <a-tabs default-active-key="definition" animation lazy-load class="ms-api-tab-nav">
        <a-tab-pane key="definition" :title="t('apiTestManagement.definition')" class="ms-api-tab-pane">
          <MsSplitBox :size="0.7" :max="0.9" :min="0.7" direction="horizontal" expand-direction="right">
            <template #first>
              <requestComposition
                v-model:detail-loading="loading"
                v-model:request="activeApiTab"
                :module-tree="props.moduleTree"
                hide-response-layout-switch
                :create-api="addDefinition"
                :update-api="updateDefinition"
                :execute-api="executeDebug"
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
              <div class="p-[24px]">
                <!-- 第一版没有模板 -->
                <!-- <MsFormCreate v-model:api="fApi" :rule="currentApiTemplateRules" :option="options" /> -->
                <a-form :model="activeApiTab" layout="vertical">
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
                    />
                  </a-form-item>
                  <a-form-item :label="t('apiTestManagement.apiStatus')" class="mb-[16px]">
                    <a-select
                      v-model:model-value="activeApiTab.status"
                      :placeholder="t('common.pleaseSelect')"
                      class="param-input w-full"
                    >
                      <template #label>
                        <apiStatus :status="activeApiTab.status" />
                      </template>
                      <a-option v-for="item of RequestDefinitionStatus" :key="item" :value="item">
                        <apiStatus :status="item" />
                      </a-option>
                    </a-select>
                  </a-form-item>
                  <a-form-item :label="t('common.tag')" class="mb-[16px]">
                    <MsTagsInput v-model:model-value="activeApiTab.tags" />
                  </a-form-item>
                  <a-form-item :label="t('common.desc')" class="mb-[16px]">
                    <a-textarea v-model:model-value="activeApiTab.description" :max-length="1000" />
                  </a-form-item>
                </a-form>
                <a-dropdown @select="handleSelect">
                  <a-button type="outline">
                    <div class="flex items-center gap-[8px]">
                      <icon-plus />
                      {{ t('apiTestManagement.addDependency') }}
                    </div>
                  </a-button>
                  <template #content>
                    <a-doption value="pre">{{ t('apiTestManagement.preDependency') }}</a-doption>
                    <a-doption value="post">{{ t('apiTestManagement.postDependency') }}</a-doption>
                  </template>
                </a-dropdown>
              </div>
            </template>
          </MsSplitBox>
        </a-tab-pane>
        <a-tab-pane v-if="!activeApiTab.isNew" key="case" :title="t('apiTestManagement.case')" class="ms-api-tab-pane">
        </a-tab-pane>
        <a-tab-pane v-if="!activeApiTab.isNew" key="mock" title="MOCK" class="ms-api-tab-pane"> </a-tab-pane>
        <template #extra>
          <div class="flex items-center gap-[8px] pr-[24px]">
            <a-button type="outline" class="arco-btn-outline--secondary !p-[8px]">
              <template #icon>
                <icon-location class="text-[var(--color-text-4)]" />
              </template>
            </a-button>
            <MsSelect
              v-model:model-value="checkedEnv"
              mode="static"
              :options="envOptions"
              class="!w-[150px]"
              :search-keys="['label']"
              allow-search
            />
          </div>
        </template>
      </a-tabs>
    </div>
  </div>
  <addDependencyDrawer v-model:visible="showAddDependencyDrawer" :mode="addDependencyMode" />
</template>

<script setup lang="ts">
  import { Message } from '@arco-design/web-vue';
  import { cloneDeep } from 'lodash-es';

  import MsEditableTab from '@/components/pure/ms-editable-tab/index.vue';
  import { TabItem } from '@/components/pure/ms-editable-tab/types';
  // import MsFormCreate from '@/components/pure/ms-form-create/formCreate.vue';
  import MsSplitBox from '@/components/pure/ms-split-box/index.vue';
  import MsTagsInput from '@/components/pure/ms-tags-input/index.vue';
  import MsSelect from '@/components/business/ms-select';
  import addDependencyDrawer from './addDependencyDrawer.vue';
  import apiTable from './apiTable.vue';
  import apiMethodName from '@/views/api-test/components/apiMethodName.vue';
  import apiStatus from '@/views/api-test/components/apiStatus.vue';

  import { executeDebug, localExecuteApiDebug } from '@/api/modules/api-test/debug';
  import {
    addDefinition,
    getDefinitionDetail,
    getTransferOptions,
    transferFile,
    updateDefinition,
    uploadTempFile,
  } from '@/api/modules/api-test/management';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';
  import { filterTree } from '@/utils';

  import { ExecuteBody, RequestTaskResult } from '@/models/apiTest/common';
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
  // 懒加载requestComposition组件
  const requestComposition = defineAsyncComponent(
    () => import('@/views/api-test/components/requestComposition/index.vue')
  );

  const props = defineProps<{
    allCount: number;
    activeModule: string;
    offspringIds: string[];
    moduleTree: ModuleTreeNode[]; // 模块树
  }>();
  const emit = defineEmits(['addDone']);

  const appStore = useAppStore();
  const { t } = useI18n();

  const apiTabs = ref<RequestParam[]>([
    {
      id: 'all',
      label: `${t('apiTestManagement.allApi')}(${props.allCount})`,
      closable: false,
    } as RequestParam,
  ]);
  const activeApiTab = ref<RequestParam>(apiTabs.value[0] as RequestParam);

  function handleActiveDebugChange() {
    if (activeApiTab.value) {
      activeApiTab.value.unSaved = true;
    }
  }

  const setActiveApi: ((params: RequestParam) => void) | undefined = inject('setActiveApi');
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
  };

  function addApiTab(defaultProps?: Partial<TabItem>) {
    const id = `definition-${Date.now()}`;
    apiTabs.value.push({
      ...cloneDeep(defaultDefinitionParams),
      moduleId: props.activeModule === 'all' ? 'root' : props.activeModule,
      label: t('apiTestDebug.newApi'),
      id,
      isNew: !defaultProps?.id, // 新开的tab标记为前端新增的调试，因为此时都已经有id了；但是如果是查看打开的会有携带id
      ...defaultProps,
    });
    activeApiTab.value = apiTabs.value[apiTabs.value.length - 1] as RequestParam;
  }

  const loading = ref(false);
  async function openApiTab(apiInfo: ModuleTreeNode | ApiDefinitionDetail) {
    const isLoadedTabIndex = apiTabs.value.findIndex((e) => e.id === apiInfo.id);
    if (isLoadedTabIndex > -1) {
      // 如果点击的请求在tab中已经存在，则直接切换到该tab
      activeApiTab.value = apiTabs.value[isLoadedTabIndex] as RequestParam;
      return;
    }
    try {
      loading.value = true;
      const res = await getDefinitionDetail(apiInfo.id);
      addApiTab({
        label: apiInfo.name,
        ...res.request,
        ...res,
        response: cloneDeep(defaultResponse),
        url: res.path,
        name: res.name, // request里面还有个name但是是null
        isNew: false,
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

  const checkedEnv = ref('DEV');
  const envOptions = ref([
    {
      label: 'DEV',
      value: 'DEV',
    },
    {
      label: 'TEST',
      value: 'TEST',
    },
    {
      label: 'PRE',
      value: 'PRE',
    },
    {
      label: 'PROD',
      value: 'PROD',
    },
  ]);

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

  function handleSelect(value: string | number | Record<string, any> | undefined) {
    switch (value) {
      case 'pre':
        addDependencyMode.value = 'pre';
        showAddDependencyDrawer.value = true;
        break;
      case 'post':
        addDependencyMode.value = 'post';
        showAddDependencyDrawer.value = true;
        break;
      default:
        break;
    }
  }

  async function handleSave(params: ApiDefinitionCreateParams | ApiDefinitionUpdateParams) {
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
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      appStore.hideLoading();
    }
  }

  async function handleSaveAsCase(params: ApiDefinitionCreateParams) {
    console.log(params);
  }

  defineExpose({
    openApiTab,
    addApiTab,
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
