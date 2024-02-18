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
    <div v-show="activeApiTab?.id === 'all'" class="flex-1">
      <apiTable :active-module="props.activeModule" :offspring-ids="props.offspringIds" />
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
                hide-response-layout-swicth
                :create-api="addDebug"
                :update-api="updateDebug"
                :execute-api="executeDebug"
                is-definiton
                @add-done="emit('addDone')"
              />
            </template>
            <template #second>
              <div class="p-[24px]">
                <MsFormCreate v-model:api="fApi" :rule="currentApiTemplateRules" :option="options" />
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
        <a-tab-pane key="case" :title="t('apiTestManagement.case')" class="ms-api-tab-pane"> </a-tab-pane>
        <a-tab-pane key="mock" title="MOCK" class="ms-api-tab-pane"> </a-tab-pane>
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
  import { cloneDeep } from 'lodash-es';

  import MsEditableTab from '@/components/pure/ms-editable-tab/index.vue';
  import { TabItem } from '@/components/pure/ms-editable-tab/types';
  import MsFormCreate from '@/components/pure/ms-form-create/formCreate.vue';
  import MsSplitBox from '@/components/pure/ms-split-box/index.vue';
  import MsSelect from '@/components/business/ms-select';
  import addDependencyDrawer from './addDependencyDrawer.vue';
  import apiTable from './apiTable.vue';
  import apiMethodName from '@/views/api-test/components/apiMethodName.vue';

  import { addDebug, executeDebug, getDebugDetail, updateDebug } from '@/api/modules/api-test/debug';
  import { useI18n } from '@/hooks/useI18n';

  import { ExecuteBody } from '@/models/apiTest/debug';
  import { ModuleTreeNode } from '@/models/common';
  import {
    RequestAuthType,
    RequestBodyFormat,
    RequestComposition,
    RequestMethods,
    ResponseComposition,
  } from '@/enums/apiEnum';

  import type { RequestParam } from '@/views/api-test/components/requestComposition/index.vue';
  // 懒加载requestComposition组件
  const requestComposition = defineAsyncComponent(
    () => import('@/views/api-test/components/requestComposition/index.vue')
  );

  const props = defineProps<{
    module: string;
    allCount: number;
    activeModule: string;
    offspringIds: string[];
    moduleTree: ModuleTreeNode[]; // 模块树
  }>();
  const emit = defineEmits(['addDone']);

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

  const initDefaultId = `debug-${Date.now()}`;
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
  const defaultResponse = {
    requestResults: [
      {
        body: '',
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
        },
      },
    ],
    console: '',
  }; // 调试返回的响应内容
  const defaultDebugParams: RequestParam = {
    id: initDefaultId,
    moduleId: 'root',
    protocol: 'HTTP',
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
      userName: '',
      password: '',
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
    isNew: true,
  };
  function addApiTab(defaultProps?: Partial<TabItem>) {
    const id = `debug-${Date.now()}`;
    apiTabs.value.push({
      ...defaultDebugParams,
      moduleId: props.module,
      label: t('apiTestDebug.newApi'),
      id,
      isNew: !defaultProps?.id, // 新开的tab标记为前端新增的调试，因为此时都已经有id了；但是如果是查看打开的会有携带id
      ...defaultProps,
    });
    activeApiTab.value = apiTabs.value[apiTabs.value.length - 1] as RequestParam;
    nextTick(() => {
      if (defaultProps) {
        handleActiveDebugChange();
      }
    });
  }

  const loading = ref(false);
  async function openApiTab(apiInfo: ModuleTreeNode) {
    const isLoadedTabIndex = apiTabs.value.findIndex((e) => e.id === apiInfo.id);
    if (isLoadedTabIndex > -1) {
      // 如果点击的请求在tab中已经存在，则直接切换到该tab
      activeApiTab.value = apiTabs.value[isLoadedTabIndex] as RequestParam;
      return;
    }
    try {
      loading.value = true;
      const res = await getDebugDetail(apiInfo.id);
      addApiTab({
        label: apiInfo.name,
        ...res,
        response: cloneDeep(defaultResponse),
        ...res.request,
        url: res.path,
        name: res.name, // request里面还有个name但是是null
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

  const fApi = ref();
  const options = {
    form: {
      layout: 'vertical',
      labelPosition: 'right',
      size: 'small',
      labelWidth: '00px',
      hideRequiredAsterisk: false,
      showMessage: true,
      inlineMessage: false,
      scrollToFirstError: true,
    },
    submitBtn: false,
    resetBtn: false,
  };
  const currentApiTemplateRules = [];
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
