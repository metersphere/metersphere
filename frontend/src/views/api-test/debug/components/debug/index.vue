<template>
  <div class="border-b border-[var(--color-text-n8)] p-[24px_24px_16px_24px]">
    <MsEditableTab
      v-model:active-tab="activeRequestTab"
      v-model:tabs="debugTabs"
      :more-action-list="moreActionList"
      at-least-one
      @add="addDebugTab"
      @close="closeDebugTab"
      @change="setActiveDebug"
      @more-action-select="handleMoreActionSelect"
    >
      <template #label="{ tab }">
        <apiMethodName v-if="isHttpProtocol" :method="tab.method" class="mr-[4px]" />
        {{ tab.label }}
      </template>
    </MsEditableTab>
  </div>
  <div class="px-[24px] pt-[16px]">
    <div class="mb-[4px] flex items-center justify-between">
      <div class="flex flex-1">
        <a-select
          v-model:model-value="activeDebug.protocol"
          :options="protocolOptions"
          :loading="protocolLoading"
          class="mr-[4px] w-[90px]"
          @change="(val) => handleActiveDebugProtocolChange(val as string)"
        />
        <a-input-group v-if="isHttpProtocol" class="flex-1">
          <apiMethodSelect
            v-model:model-value="activeDebug.method"
            class="w-[140px]"
            @change="handleActiveDebugChange"
          />
          <a-input
            v-model:model-value="activeDebug.url"
            :max-length="255"
            :placeholder="t('apiTestDebug.urlPlaceholder')"
            @change="handleActiveDebugChange"
          />
        </a-input-group>
      </div>
      <div class="ml-[16px]">
        <a-dropdown-button
          :button-props="{ loading: executeLoading }"
          :disabled="executeLoading"
          class="exec-btn"
          @click="execute"
        >
          {{ t('apiTestDebug.serverExec') }}
          <template #icon>
            <icon-down />
          </template>
          <template #content>
            <a-doption>{{ t('apiTestDebug.localExec') }}</a-doption>
          </template>
        </a-dropdown-button>
        <a-button type="secondary" @click="handleSaveShortcut">
          <div class="flex items-center">
            {{ t('common.save') }}
            <div class="text-[var(--color-text-4)]">(<icon-command size="14" />+S)</div>
          </div>
        </a-button>
      </div>
    </div>
  </div>
  <div ref="splitContainerRef" class="h-[calc(100%-125px)]">
    <MsSplitBox
      ref="splitBoxRef"
      v-model:size="splitBoxSize"
      :max="0.98"
      min="10px"
      :direction="activeLayout"
      second-container-class="!overflow-y-hidden"
      @expand-change="handleExpandChange"
    >
      <template #first>
        <div
          :class="`flex h-full min-w-[800px] flex-col px-[24px] pb-[16px] ${
            activeLayout === 'horizontal' ? ' pr-[16px]' : ''
          }`"
        >
          <div>
            <a-tabs v-model:active-key="activeDebug.activeTab" class="no-content">
              <a-tab-pane v-for="item of contentTabList" :key="item.value" :title="item.label" />
            </a-tabs>
            <a-divider margin="0" class="!mb-[16px]"></a-divider>
          </div>
          <div class="tab-pane-container">
            <template v-if="isInitPluginForm || activeDebug.activeTab === RequestComposition.PLUGIN">
              <a-spin v-show="activeDebug.activeTab === RequestComposition.PLUGIN" :loading="pluginLoading">
                <MsFormCreate v-model:api="fApi" :rule="currentPluginScript" :option="options" />
              </a-spin>
            </template>
            <debugHeader
              v-if="activeDebug.activeTab === RequestComposition.HEADER"
              v-model:params="activeDebug.headers"
              :layout="activeLayout"
              :second-box-height="secondBoxHeight"
              @change="handleActiveDebugChange"
            />
            <debugBody
              v-else-if="activeDebug.activeTab === RequestComposition.BODY"
              v-model:params="activeDebug.body"
              :layout="activeLayout"
              :second-box-height="secondBoxHeight"
              @change="handleActiveDebugChange"
            />
            <debugQuery
              v-else-if="activeDebug.activeTab === RequestComposition.QUERY"
              v-model:params="activeDebug.query"
              :layout="activeLayout"
              :second-box-height="secondBoxHeight"
              @change="handleActiveDebugChange"
            />
            <debugRest
              v-else-if="activeDebug.activeTab === RequestComposition.REST"
              v-model:params="activeDebug.rest"
              :layout="activeLayout"
              :second-box-height="secondBoxHeight"
              @change="handleActiveDebugChange"
            />
            <precondition
              v-else-if="activeDebug.activeTab === RequestComposition.PRECONDITION"
              v-model:params="activeDebug.children[0].preProcessorConfig.processors"
              @change="handleActiveDebugChange"
            />
            <postcondition
              v-else-if="activeDebug.activeTab === RequestComposition.POST_CONDITION"
              v-model:params="activeDebug.children[0].postProcessorConfig.processors"
              :response="activeDebug.response.requestResults[0]?.responseResult.body"
              :layout="activeLayout"
              :second-box-height="secondBoxHeight"
              @change="handleActiveDebugChange"
            />
            <debugAuth
              v-else-if="activeDebug.activeTab === RequestComposition.AUTH"
              v-model:params="activeDebug.authConfig"
              @change="handleActiveDebugChange"
            />
            <debugSetting
              v-else-if="activeDebug.activeTab === RequestComposition.SETTING"
              v-model:params="activeDebug.otherConfig"
              @change="handleActiveDebugChange"
            />
          </div>
        </div>
      </template>
      <template #second>
        <response
          v-model:active-layout="activeLayout"
          v-model:active-tab="activeDebug.responseActiveTab"
          :is-expanded="isExpanded"
          :response="activeDebug.response"
          @change-expand="changeExpand"
          @change-layout="handleActiveLayoutChange"
        />
      </template>
    </MsSplitBox>
  </div>
  <a-modal
    v-model:visible="saveModalVisible"
    :title="t('common.save')"
    :ok-loading="saveLoading"
    class="ms-modal-form"
    title-align="start"
    body-class="!p-0"
    @before-ok="handleSave"
    @cancel="handleCancel"
  >
    <a-form ref="saveModalFormRef" :model="saveModalForm" layout="vertical">
      <a-form-item
        field="name"
        :label="t('apiTestDebug.requestName')"
        :rules="[{ required: true, message: t('apiTestDebug.requestNameRequired') }]"
        asterisk-position="end"
      >
        <a-input v-model:model-value="saveModalForm.name" :placeholder="t('apiTestDebug.requestNamePlaceholder')" />
      </a-form-item>
      <a-form-item
        v-if="isHttpProtocol"
        field="path"
        :label="t('apiTestDebug.requestUrl')"
        :rules="[{ required: true, message: t('apiTestDebug.requestUrlRequired') }]"
        asterisk-position="end"
      >
        <a-input v-model:model-value="saveModalForm.path" :placeholder="t('apiTestDebug.commonPlaceholder')" />
      </a-form-item>
      <a-form-item :label="t('apiTestDebug.requestModule')" class="mb-0">
        <a-tree-select
          v-model:modelValue="saveModalForm.moduleId"
          :data="props.moduleTree"
          :field-names="{ title: 'name', key: 'id', children: 'children' }"
          allow-search
        />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup lang="ts">
  import { FormInstance, Message, SelectOptionData } from '@arco-design/web-vue';
  import { cloneDeep, debounce } from 'lodash-es';

  import MsEditableTab from '@/components/pure/ms-editable-tab/index.vue';
  import { TabItem } from '@/components/pure/ms-editable-tab/types';
  import MsFormCreate from '@/components/pure/ms-form-create/formCreate.vue';
  import MsSplitBox from '@/components/pure/ms-split-box/index.vue';
  import { ActionsItem } from '@/components/pure/ms-table-more-action/types';
  import debugAuth from './auth.vue';
  import postcondition from './postcondition.vue';
  import precondition from './precondition.vue';
  import response from './response.vue';
  import debugSetting from './setting.vue';
  import apiMethodName from '@/views/api-test/components/apiMethodName.vue';
  import apiMethodSelect from '@/views/api-test/components/apiMethodSelect.vue';

  import { addDebug, executeDebug } from '@/api/modules/api-test/debug';
  import { getPluginScript, getProtocolList } from '@/api/modules/api-test/management';
  import { getSocket } from '@/api/modules/project-management/commonScript';
  import { useI18n } from '@/hooks/useI18n';
  import { useAppStore } from '@/store';
  import { getGenerateId } from '@/utils';
  import { scrollIntoView } from '@/utils/dom';
  import { registerCatchSaveShortcut, removeCatchSaveShortcut } from '@/utils/event';

  import { ExecuteBody, ExecuteHTTPRequestFullParams } from '@/models/apiTest/debug';
  import { ModuleTreeNode } from '@/models/common';
  import { RequestBodyFormat, RequestComposition, RequestMethods, ResponseComposition } from '@/enums/apiEnum';

  // 懒加载Http协议组件
  const debugHeader = defineAsyncComponent(() => import('./header.vue'));
  const debugBody = defineAsyncComponent(() => import('./body.vue'));
  const debugQuery = defineAsyncComponent(() => import('./query.vue'));
  const debugRest = defineAsyncComponent(() => import('./rest.vue'));

  export type DebugTabParam = ExecuteHTTPRequestFullParams & TabItem & Record<string, any>;

  const props = defineProps<{
    module: string; // 当前激活的接口模块
    moduleTree: ModuleTreeNode[]; // 接口模块树
  }>();

  const appStore = useAppStore();
  const { t } = useI18n();

  const initDefaultId = `debug-${Date.now()}`;
  const activeRequestTab = ref<string | number>(initDefaultId);
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
  const defaultDebugParams: DebugTabParam = {
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
      authType: 'NONE',
      username: '',
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
      followRedirects: false,
      autoRedirects: false,
    },
    responseActiveTab: ResponseComposition.BODY,
    response: {
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
    }, // 调试返回的响应内容
  };
  const debugTabs = ref<DebugTabParam[]>([cloneDeep(defaultDebugParams)]);
  const activeDebug = ref<DebugTabParam>(debugTabs.value[0]);
  const isHttpProtocol = computed(() => activeDebug.value.protocol === 'HTTP');
  const isInitPluginForm = ref(false); // 是否初始化过插件表单

  watch(
    () => activeDebug.value.protocol,
    (val) => {
      if (val !== 'HTTP') {
        isInitPluginForm.value = true;
      }
    },
    {
      immediate: true,
    }
  );

  function setActiveDebug(item: TabItem) {
    activeDebug.value = item as DebugTabParam;
  }

  function handleActiveDebugChange() {
    activeDebug.value.unSaved = true;
  }

  function addDebugTab(defaultProps?: Partial<TabItem>) {
    const id = `debug-${Date.now()}`;
    debugTabs.value.push({
      ...cloneDeep(defaultDebugParams),
      moduleId: props.module,
      id,
      ...defaultProps,
    });
    activeRequestTab.value = id;
    nextTick(() => {
      if (defaultProps) {
        handleActiveDebugChange();
      }
    });
  }

  function closeDebugTab(tab: TabItem) {
    const index = debugTabs.value.findIndex((item) => item.id === tab.id);
    debugTabs.value.splice(index, 1);
    if (activeRequestTab.value === tab.id) {
      activeRequestTab.value = debugTabs.value[0]?.id || '';
    }
  }

  const moreActionList = [
    {
      eventTag: 'closeOther',
      label: t('apiTestDebug.closeOther'),
    },
  ];

  function handleMoreActionSelect(event: ActionsItem) {
    if (event.eventTag === 'closeOther') {
      debugTabs.value = debugTabs.value.filter((item) => item.id === activeRequestTab.value);
    }
  }

  // 请求内容公共tabKey
  const commonContentTabKey = [
    RequestComposition.PRECONDITION,
    RequestComposition.POST_CONDITION,
    RequestComposition.ASSERTION,
  ];
  // 请求内容插件tab
  const pluginContentTab = [
    {
      value: RequestComposition.PLUGIN,
      label: t('apiTestDebug.pluginData'),
    },
  ];
  // Http 请求的tab
  const httpContentTabList = [
    {
      value: RequestComposition.HEADER,
      label: t('apiTestDebug.header'),
    },
    {
      value: RequestComposition.BODY,
      label: t('apiTestDebug.body'),
    },
    {
      value: RequestComposition.QUERY,
      label: RequestComposition.QUERY,
    },
    {
      value: RequestComposition.REST,
      label: RequestComposition.REST,
    },
    {
      value: RequestComposition.PRECONDITION,
      label: t('apiTestDebug.prefix'),
    },
    {
      value: RequestComposition.POST_CONDITION,
      label: t('apiTestDebug.post'),
    },
    {
      value: RequestComposition.ASSERTION,
      label: t('apiTestDebug.assertion'),
    },
    {
      value: RequestComposition.AUTH,
      label: t('apiTestDebug.auth'),
    },
    {
      value: RequestComposition.SETTING,
      label: t('apiTestDebug.setting'),
    },
  ];
  // 根据协议类型获取请求内容tab
  const contentTabList = computed(() =>
    isHttpProtocol.value
      ? httpContentTabList
      : [...pluginContentTab, ...httpContentTabList.filter((e) => commonContentTabKey.includes(e.value))]
  );
  const protocolLoading = ref(false);
  const protocolOptions = ref<SelectOptionData[]>([]);

  async function initProtocolList() {
    try {
      protocolLoading.value = true;
      const res = await getProtocolList(appStore.currentOrgId);
      protocolOptions.value = res.map((e) => ({
        label: e.protocol,
        value: e.protocol,
        polymorphicName: e.polymorphicName,
        pluginId: e.pluginId,
      }));
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      protocolLoading.value = false;
    }
  }

  const pluginScriptMap = ref<Record<string, any>>({}); // 存储初始化过后的插件配置
  const pluginLoading = ref(false);
  const currentPluginScript = computed<Record<string, any>[]>(
    () => pluginScriptMap.value[activeDebug.value.protocol] || []
  );
  async function initPluginScript() {
    if (pluginScriptMap.value[activeDebug.value.protocol] !== undefined) {
      // 已经初始化过
      return;
    }
    try {
      pluginLoading.value = true;
      const res = await getPluginScript(
        protocolOptions.value.find((e) => e.value === activeDebug.value.protocol)?.pluginId || ''
      );
      pluginScriptMap.value[activeDebug.value.protocol] = res.script;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      pluginLoading.value = false;
    }
  }

  function handleActiveDebugProtocolChange(val: string) {
    if (val !== 'HTTP') {
      activeDebug.value.activeTab = RequestComposition.PLUGIN;
      initPluginScript();
    } else {
      activeDebug.value.activeTab = RequestComposition.HEADER;
    }
    handleActiveDebugChange();
  }

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

  const splitBoxSize = ref<string | number>(0.6);
  const activeLayout = ref<'horizontal' | 'vertical'>('vertical');
  const splitContainerRef = ref<HTMLElement>();
  const secondBoxHeight = ref(0);

  watch(
    () => splitBoxSize.value,
    debounce((val) => {
      // 动画 300ms
      if (splitContainerRef.value) {
        if (typeof val === 'string' && val.includes('px')) {
          val = Number(val.split('px')[0]);
          secondBoxHeight.value = splitContainerRef.value.clientHeight - val;
        } else {
          secondBoxHeight.value = splitContainerRef.value.clientHeight * (1 - val);
        }
      }
    }, 300),
    {
      immediate: true,
    }
  );

  const splitBoxRef = ref<InstanceType<typeof MsSplitBox>>();
  const isExpanded = ref(true);

  function handleExpandChange(val: boolean) {
    isExpanded.value = val;
  }
  function changeExpand(val: boolean) {
    isExpanded.value = val;
    if (val) {
      splitBoxRef.value?.expand(0.6);
    } else {
      splitBoxRef.value?.collapse(splitContainerRef.value ? `${splitContainerRef.value.clientHeight - 42}px` : 0);
    }
  }

  function handleActiveLayoutChange() {
    isExpanded.value = true;
    splitBoxSize.value = 0.6;
    splitBoxRef.value?.expand(0.6);
  }

  const executeLoading = ref(false);
  const reportId = ref('');
  const websocket = ref<WebSocket>();
  function debugSocket() {
    websocket.value = getSocket(reportId.value);
    websocket.value.addEventListener('message', (event) => {
      const data = JSON.parse(event.data);
      if (data.msgType === 'EXEC_RESULT') {
        activeDebug.value.response = data.taskResult;
        executeLoading.value = false;
      }
    });

    websocket.value.addEventListener('close', (event) => {
      console.log('关闭:', event);
    });

    websocket.value.addEventListener('error', (event) => {
      console.error('错误:', event);
    });
  }

  function makeRequestParams() {
    const polymorphicName = protocolOptions.value.find((e) => e.value === activeDebug.value.protocol)?.polymorphicName; // 协议多态名称

    let requestParams;
    if (isHttpProtocol.value) {
      requestParams = {
        authConfig: activeDebug.value.authConfig,
        body: { ...activeDebug.value.body, binaryBody: undefined },
        headers: activeDebug.value.headers,
        method: activeDebug.value.method,
        otherConfig: activeDebug.value.otherConfig,
        path: activeDebug.value.url,
        query: activeDebug.value.query,
        rest: activeDebug.value.rest,
        url: activeDebug.value.url,
        polymorphicName,
      };
    } else {
      requestParams = {
        ...fApi.value.form,
        polymorphicName,
      };
    }
    reportId.value = getGenerateId();
    debugSocket(); // 开启websocket
    return {
      id: activeDebug.value.id.toString(),
      reportId: reportId.value,
      environmentId: '',
      tempFileIds: [],
      request: {
        ...requestParams,
        children: [
          {
            polymorphicName: 'MsCommonElement', // 协议多态名称，写死MsCommonElement
            assertionConfig: {
              // TODO:暂时不做断言
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
      },
      projectId: appStore.currentProjectId,
    };
  }

  async function execute() {
    if (isHttpProtocol.value) {
      try {
        executeLoading.value = true;
        await executeDebug(makeRequestParams());
      } catch (error) {
        // eslint-disable-next-line no-console
        console.log(error);
        executeLoading.value = false;
      }
    } else {
      // 插件需要校验动态表单
      fApi.value?.validate(async (valid) => {
        if (valid === true) {
          try {
            executeLoading.value = true;
            await executeDebug(makeRequestParams());
          } catch (error) {
            // eslint-disable-next-line no-console
            console.log(error);
            executeLoading.value = false;
          }
        } else {
          activeDebug.value.activeTab = RequestComposition.PLUGIN;
          nextTick(() => {
            scrollIntoView(document.querySelector('.arco-form-item-message'), { block: 'center' });
          });
        }
      });
    }
  }

  const saveModalVisible = ref(false);
  const saveModalForm = ref({
    name: '',
    path: activeDebug.value.url || '',
    moduleId: activeDebug.value.module,
  });
  const saveModalFormRef = ref<FormInstance>();
  const saveLoading = ref(false);

  watch(
    () => saveModalVisible.value,
    (val) => {
      if (!val) {
        saveModalFormRef.value?.resetFields();
      }
    }
  );

  async function handleSaveShortcut() {
    try {
      if (!isHttpProtocol.value) {
        // 插件需要校验动态表单
        await fApi.value?.validate();
      }
      saveModalForm.value = {
        name: '',
        path: activeDebug.value.url || '',
        moduleId: activeDebug.value.module,
      };
      saveModalVisible.value = true;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
      // 校验不通过则不进行保存
      activeDebug.value.activeTab = RequestComposition.PLUGIN;
      nextTick(() => {
        scrollIntoView(document.querySelector('.arco-form-item-message'), { block: 'center' });
      });
    }
  }

  function handleCancel() {
    saveModalFormRef.value?.resetFields();
  }

  async function handleSave(done: (closed: boolean) => void) {
    saveModalFormRef.value?.validate(async (errors) => {
      if (!errors) {
        try {
          saveLoading.value = true;
          await addDebug({
            ...makeRequestParams(),
            ...saveModalForm.value,
            protocol: activeDebug.value.protocol,
            method: isHttpProtocol.value ? activeDebug.value.method : activeDebug.value.protocol,
            uploadFileIds: [],
            linkFileIds: [],
          });
          saveLoading.value = false;
          saveModalVisible.value = false;
          done(true);
          activeDebug.value.unSaved = false;
          Message.success(t('common.saveSuccess'));
        } catch (error) {
          saveLoading.value = false;
        }
      }
    });
    done(false);
  }

  onBeforeMount(() => {
    initProtocolList();
  });

  onMounted(() => {
    registerCatchSaveShortcut(handleSaveShortcut);
  });

  onBeforeUnmount(() => {
    removeCatchSaveShortcut(handleSaveShortcut);
  });

  defineExpose({
    addDebugTab,
  });
</script>

<style lang="less" scoped>
  .exec-btn {
    margin-right: 12px;
    :deep(.arco-btn) {
      color: white !important;
      background-color: rgb(var(--primary-5)) !important;
      .btn-base-primary-hover();
      .btn-base-primary-active();
      .btn-base-primary-disabled();
    }
  }
  .tab-pane-container {
    @apply flex-1 overflow-y-auto;
    .ms-scroll-bar();
  }
  :deep(.no-content) {
    .arco-tabs-content {
      display: none;
    }
  }
</style>
