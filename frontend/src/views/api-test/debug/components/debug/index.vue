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
        <apiMethodName :method="tab.method" class="mr-[4px]" />
        {{ tab.label }}
      </template>
    </MsEditableTab>
  </div>
  <div class="px-[24px] pt-[16px]">
    <div class="mb-[4px] flex items-center justify-between">
      <div class="flex flex-1">
        <a-select
          v-model:model-value="activeDebug.moduleProtocol"
          :options="moduleProtocolOptions"
          class="mr-[4px] w-[90px]"
          @change="handleActiveDebugChange"
        />
        <a-input-group class="flex-1">
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
        <a-dropdown-button class="exec-btn">
          {{ t('apiTestDebug.serverExec') }}
          <template #icon>
            <icon-down />
          </template>
          <template #content>
            <a-doption>{{ t('apiTestDebug.localExec') }}</a-doption>
          </template>
        </a-dropdown-button>
        <a-button type="secondary">
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
        <div :class="`h-full min-w-[800px] px-[24px] pb-[16px] ${activeLayout === 'horizontal' ? ' pr-[16px]' : ''}`">
          <a-tabs v-model:active-key="activeDebug.activeTab" class="no-content">
            <a-tab-pane v-for="item of contentTabList" :key="item.value" :title="item.label" />
          </a-tabs>
          <a-divider margin="0" class="!mb-[16px]"></a-divider>
          <debugHeader
            v-if="activeDebug.activeTab === RequestComposition.HEADER"
            v-model:params="activeDebug.headerParams"
            :layout="activeLayout"
            :second-box-height="secondBoxHeight"
            @change="handleActiveDebugChange"
          />
          <debugBody
            v-else-if="activeDebug.activeTab === RequestComposition.BODY"
            v-model:params="activeDebug.bodyParams"
            :layout="activeLayout"
            :second-box-height="secondBoxHeight"
            @change="handleActiveDebugChange"
          />
          <debugQuery
            v-else-if="activeDebug.activeTab === RequestComposition.QUERY"
            v-model:params="activeDebug.queryParams"
            :layout="activeLayout"
            :second-box-height="secondBoxHeight"
            @change="handleActiveDebugChange"
          />
          <debugRest
            v-else-if="activeDebug.activeTab === RequestComposition.REST"
            v-model:params="activeDebug.restParams"
            :layout="activeLayout"
            :second-box-height="secondBoxHeight"
            @change="handleActiveDebugChange"
          />
          <precondition
            v-else-if="activeDebug.activeTab === RequestComposition.PRECONDITION"
            v-model:params="activeDebug.preconditions"
            @change="handleActiveDebugChange"
          />
          <postcondition
            v-else-if="activeDebug.activeTab === RequestComposition.POST_CONDITION"
            v-model:params="activeDebug.postConditions"
            :response="activeDebug.response.body"
            :layout="activeLayout"
            :second-box-height="secondBoxHeight"
            @change="handleActiveDebugChange"
          />
          <debugAuth
            v-else-if="activeDebug.activeTab === RequestComposition.AUTH"
            v-model:params="activeDebug.authParams"
            @change="handleActiveDebugChange"
          />
          <debugSetting
            v-else-if="activeDebug.activeTab === RequestComposition.SETTING"
            v-model:params="activeDebug.setting"
            @change="handleActiveDebugChange"
          />
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
        field="url"
        :label="t('apiTestDebug.requestUrl')"
        :rules="[{ required: true, message: t('apiTestDebug.requestUrlRequired') }]"
        asterisk-position="end"
      >
        <a-input v-model:model-value="saveModalForm.url" :placeholder="t('apiTestDebug.commonPlaceholder')" />
      </a-form-item>
      <a-form-item :label="t('apiTestDebug.requestModule')" class="mb-0">
        <a-tree-select
          v-model:modelValue="saveModalForm.module"
          :data="props.moduleTree"
          :field-names="{ title: 'name', key: 'id', children: 'children' }"
          allow-search
        />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup lang="ts">
  import { FormInstance, Message } from '@arco-design/web-vue';
  import { cloneDeep, debounce } from 'lodash-es';

  import MsEditableTab from '@/components/pure/ms-editable-tab/index.vue';
  import { TabItem } from '@/components/pure/ms-editable-tab/types';
  import MsSplitBox from '@/components/pure/ms-split-box/index.vue';
  import { ActionsItem } from '@/components/pure/ms-table-more-action/types';
  import debugAuth from './auth.vue';
  import debugBody, { BodyParams } from './body.vue';
  import debugHeader from './header.vue';
  import postcondition from './postcondition.vue';
  import precondition from './precondition.vue';
  import debugQuery from './query.vue';
  import response from './response.vue';
  import debugRest from './rest.vue';
  import debugSetting from './setting.vue';
  import apiMethodName from '@/views/api-test/components/apiMethodName.vue';
  import apiMethodSelect from '@/views/api-test/components/apiMethodSelect.vue';

  import { useI18n } from '@/hooks/useI18n';
  import { registerCatchSaveShortcut, removeCatchSaveShortcut } from '@/utils/event';

  import type { ModuleTreeNode } from '@/models/projectManagement/file';
  import { RequestBodyFormat, RequestComposition, RequestMethods, ResponseComposition } from '@/enums/apiEnum';

  const props = defineProps<{
    module: string; // 当前激活的接口模块
    moduleTree: ModuleTreeNode[]; // 接口模块树
  }>();

  const { t } = useI18n();

  const initDefaultId = `debug-${Date.now()}`;
  const activeRequestTab = ref<string | number>(initDefaultId);
  const defaultBodyParams: BodyParams = {
    format: RequestBodyFormat.NONE,
    formData: [],
    formUrlEncode: [],
    json: '',
    xml: '',
    binary: '',
    binaryDesc: '',
    binarySend: false,
    raw: '',
  };
  const defaultDebugParams = {
    id: initDefaultId,
    module: 'root',
    moduleProtocol: 'http',
    url: '',
    activeTab: RequestComposition.HEADER,
    label: t('apiTestDebug.newApi'),
    closable: true,
    method: RequestMethods.GET,
    unSaved: false,
    headerParams: [],
    bodyParams: cloneDeep(defaultBodyParams),
    queryParams: [],
    restParams: [],
    authParams: {
      authType: 'none',
      account: '',
      password: '',
    },
    preconditions: [],
    postConditions: [],
    setting: {
      connectTimeout: 60000,
      responseTimeout: 60000,
      certificateAlias: '',
      redirect: 'follow',
    },
    responseActiveTab: ResponseComposition.BODY,
    response: {
      status: 200,
      headers: [],
      timing: 12938,
      size: 8734,
      env: 'Mock',
      resource: '66',
      timingInfo: {
        ready: 10,
        socketInit: 50,
        dnsQuery: 20,
        tcpHandshake: 80,
        sslHandshake: 40,
        waitingTTFB: 30,
        downloadContent: 10,
        deal: 10,
        total: 250,
      },
      extract: {
        a: 'asdasd',
        b: 'asdasdasd43f43',
      },
      console: `GET https://qa-release.fit2cloud.com/test`,
      content: `请求地址:
https://qa-release.fit2cloud.com/test
请求头:
Connection: keep-alive
Content-Length: 0
Content-Type: application/x-www-form-urlencoded; charset=UTF-8
Host: qa-release.fit2cloud.com
User-Agent: Apache-HttpClient/4.5.14 (Java/17.0.9)

Body:
POST https://qa-release.fit2cloud.com/test

POST data:


[no cookies]
`,
      header: `HTTP/ 1.1 200 OK 
Content-Length: 2381 
Content-Type: text/html 
Server: bfe 
Date: Wed, 13 Dec 2023 08:53:25 GMTHTTP/ 1.1 200 OK 
Content-Length: 2381 
Content-Type: text/html 
Server: bfe 
Date: Wed, 13 Dec 2023 08:53:25 GMT`,
      body: `<?xml version="1.0"?>
<configuration xmlns:xdt="http://schemas.microsoft.com/XML-Document-Transform">
  <connectionStrings>
    <add name="MyDB" 
      connectionString="value for the deployed Web.config file" 
      xdt:Transform="SetAttributes" xdt:Locator="Match(name)"/>
  </connectionStrings>
  <a>哈哈哈哈哈哈哈</a>
  <system.web>
    <customErrors defaultRedirect="GenericError.htm"
      mode="RemoteOnly" xdt:Transform="Replace">
      <error statusCode="500" redirect="InternalError.htm"/>
    </customErrors>
  </system.web>
</configuration>`,
    }, // 调试返回的响应内容
  };
  const debugTabs = ref<TabItem[]>([cloneDeep(defaultDebugParams)]);
  const activeDebug = ref<TabItem>(debugTabs.value[0]);

  function setActiveDebug(item: TabItem) {
    activeDebug.value = item;
  }

  function handleActiveDebugChange() {
    activeDebug.value.unSaved = true;
  }

  function addDebugTab(defaultProps?: Partial<TabItem>) {
    const id = `debug-${Date.now()}`;
    debugTabs.value.push({
      ...cloneDeep(defaultDebugParams),
      module: props.module,
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

  const contentTabList = [
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

  const moduleProtocolOptions = ref([
    {
      label: 'HTTP',
      value: 'http',
    },
  ]);

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

  const saveModalVisible = ref(false);
  const saveModalForm = ref({
    name: '',
    url: activeDebug.value.url,
    module: activeDebug.value.module,
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

  function handleSaveShortcut() {
    saveModalForm.value = {
      name: '',
      url: activeDebug.value.url,
      module: activeDebug.value.module,
    };
    saveModalVisible.value = true;
  }

  function handleSave(done: (closed: boolean) => void) {
    saveModalFormRef.value?.validate(async (errors) => {
      if (!errors) {
        try {
          saveLoading.value = true;
          // eslint-disable-next-line no-promise-executor-return
          await new Promise((resolve) => setTimeout(resolve, 2000));
          saveLoading.value = false;
          saveModalVisible.value = false;
          done(true);
          activeDebug.value.unSaved = false;
          Message.success(t('common.saveSuccess'));
        } catch (error) {
          saveLoading.value = false;
        }
      } else {
        done(false);
      }
    });
  }

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
  :deep(.no-content) {
    .arco-tabs-content {
      display: none;
    }
  }
</style>
