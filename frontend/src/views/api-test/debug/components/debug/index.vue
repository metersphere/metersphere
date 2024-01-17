<template>
  <div class="border-b border-[var(--color-text-n8)] p-[24px_24px_16px_24px]">
    <MsEditableTab
      v-model:active-tab="activeTab"
      :tabs="debugTabs"
      :more-action-list="moreActionList"
      @add="addDebugTab"
      @close="closeDebugTab"
      @change="setActiveDebug"
    >
      <template #label="{ tab }">
        <apiMethodName :method="tab.method" class="mr-[4px]" />
        {{ tab.label }}
        <div v-if="tab.unSave" class="ml-[8px] h-[8px] w-[8px] rounded-full bg-[rgb(var(--primary-5))]"></div>
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
          <a-select v-model:model-value="activeDebug.method" class="w-[140px]" @change="handleActiveDebugChange">
            <template #label="{ data }">
              <apiMethodName :method="data.value" class="inline-block" />
            </template>
            <a-option v-for="method of RequestMethods" :key="method" :value="method">
              <apiMethodName :method="method" />
            </a-option>
          </a-select>
          <a-input
            v-model:model-value="debugUrl"
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
            v-else-if="activeDebug.activeTab === RequestComposition.PREFIX"
            v-model:params="activeDebug.preconditions"
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
        <div class="min-w-[290px] bg-[var(--color-text-n9)] p-[8px_16px]">
          <div class="flex items-center">
            <template v-if="activeLayout === 'vertical'">
              <MsButton
                v-if="isExpanded"
                type="icon"
                class="!mr-0 !rounded-full bg-[rgb(var(--primary-1))]"
                @click="changeExpand(false)"
              >
                <icon-down :size="12" />
              </MsButton>
              <MsButton v-else type="icon" status="secondary" class="!mr-0 !rounded-full" @click="changeExpand(true)">
                <icon-right :size="12" />
              </MsButton>
            </template>
            <div class="ml-[4px] mr-[24px] font-medium">{{ t('apiTestDebug.responseContent') }}</div>
            <a-radio-group
              v-model:model-value="activeLayout"
              type="button"
              size="small"
              @change="handleActiveLayoutChange"
            >
              <a-radio value="vertical">{{ t('apiTestDebug.vertical') }}</a-radio>
              <a-radio value="horizontal">{{ t('apiTestDebug.horizontal') }}</a-radio>
            </a-radio-group>
          </div>
        </div>
        <div class="p-[16px]"></div>
      </template>
    </MsSplitBox>
  </div>
</template>

<script setup lang="ts">
  import { cloneDeep, debounce } from 'lodash-es';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsEditableTab from '@/components/pure/ms-editable-tab/index.vue';
  import { TabItem } from '@/components/pure/ms-editable-tab/types';
  import MsSplitBox from '@/components/pure/ms-split-box/index.vue';
  import apiMethodName from '../../../components/apiMethodName.vue';
  import debugAuth from './auth.vue';
  import debugBody, { BodyParams } from './body.vue';
  import debugHeader from './header.vue';
  import precondition from './precondition.vue';
  import debugQuery from './query.vue';
  import debugRest from './rest.vue';
  import debugSetting from './setting.vue';

  import { useI18n } from '@/hooks/useI18n';
  import { registerCatchSaveShortcut, removeCatchSaveShortcut } from '@/utils/event';

  import { RequestBodyFormat, RequestComposition, RequestMethods } from '@/enums/apiEnum';

  const { t } = useI18n();

  const initDefaultId = `debug-${Date.now()}`;
  const activeTab = ref<string | number>(initDefaultId);
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
  const debugTabs = ref<TabItem[]>([
    {
      id: initDefaultId,
      moduleProtocol: 'http',
      activeTab: RequestComposition.HEADER,
      label: t('apiTestDebug.newApi'),
      closable: true,
      method: RequestMethods.GET,
      unSave: false,
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
      setting: {
        connectTimeout: 60000,
        responseTimeout: 60000,
        certificateAlias: '',
        redirect: 'follow',
      },
    },
  ]);
  const debugUrl = ref('');
  const activeDebug = ref<TabItem>(debugTabs.value[0]);

  function setActiveDebug(item: TabItem) {
    activeDebug.value = item;
  }

  function handleActiveDebugChange() {
    activeDebug.value.unSave = true;
  }

  function addDebugTab() {
    const id = `debug-${Date.now()}`;
    debugTabs.value.push({
      id,
      moduleProtocol: 'http',
      activeTab: RequestComposition.HEADER,
      label: t('apiTestDebug.newApi'),
      closable: true,
      method: RequestMethods.GET,
      unSave: false,
      headerParams: [],
      bodyParams: cloneDeep(defaultBodyParams),
      queryParams: [],
      restParams: [],
      authParams: {
        authType: 'none',
        account: '',
        password: '',
      },
      setting: {
        connectTimeout: 60000,
        responseTimeout: 60000,
        certificateAlias: '',
        redirect: 'follow',
      },
    });
    activeTab.value = id;
  }

  function closeDebugTab(tab: TabItem) {
    const index = debugTabs.value.findIndex((item) => item.id === tab.id);
    debugTabs.value.splice(index, 1);
    if (activeTab.value === tab.id) {
      activeTab.value = debugTabs.value[0]?.id || '';
    }
  }

  const moreActionList = [
    {
      key: 'add',
      label: t('common.add'),
    },
    {
      key: 'delete',
      label: t('common.delete'),
    },
  ];

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
      value: RequestComposition.PREFIX,
      label: t('apiTestDebug.prefix'),
    },
    {
      value: RequestComposition.POST_CONDITION,
      label: t('apiTestDebug.postCondition'),
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
      if (splitContainerRef.value) {
        secondBoxHeight.value = splitContainerRef.value.clientHeight * (1 - val);
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

  function saveDebug() {
    activeDebug.value.unSave = false;
  }

  onMounted(() => {
    registerCatchSaveShortcut(saveDebug);
  });

  onBeforeUnmount(() => {
    removeCatchSaveShortcut(saveDebug);
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
