<template>
  <div class="border-b border-[var(--color-text-n8)] p-[24px_24px_16px_24px]">
    <MsEditableTab
      v-model:active-tab="activeTab"
      :tabs="debugTabs"
      :more-action-list="moreActionList"
      @add="addDebugTab"
      @close="closeDebugTab"
      @click="setActiveDebug"
    >
      <template #label="{ tab }">
        <apiMethodName :method="tab.method" class="mr-[4px]" />
        {{ tab.label }}
        <div class="ml-[8px] h-[8px] w-[8px] rounded-full bg-[rgb(var(--primary-5))]"></div>
      </template>
    </MsEditableTab>
  </div>
  <div class="px-[24px] pt-[16px]">
    <div class="mb-[4px] flex items-center justify-between">
      <a-input-group class="flex-1">
        <a-select v-model:model-value="activeDebug.method" class="w-[140px]">
          <template #label="{ data }">
            <apiMethodName :method="data.value" class="inline-block" />
          </template>
          <a-option v-for="method of RequestMethods" :key="method" :value="method">
            <apiMethodName :method="method" />
          </a-option>
        </a-select>
        <a-input v-model:model-value="debugUrl" :placeholder="t('ms.apiTestDebug.urlPlaceholder')" />
      </a-input-group>
      <div class="ml-[16px]">
        <a-dropdown-button class="exec-btn">
          {{ t('ms.apiTestDebug.serverExec') }}
          <template #icon>
            <icon-down />
          </template>
          <template #content>
            <a-doption>{{ t('ms.apiTestDebug.localExec') }}</a-doption>
          </template>
        </a-dropdown-button>
        <a-button type="secondary">
          <div class="flex items-center">
            {{ t('common.save') }}
            <div class="text-[var(--color-text-4)]">(<icon-command size="14" /> + S)</div>
          </div>
        </a-button>
      </div>
    </div>
  </div>
  <div ref="splitContainerRef" class="flex-1">
    <MsSplitBox
      ref="splitBoxRef"
      v-model:size="splitBoxSize"
      :max="0.98"
      min="10px"
      :direction="activeLayout"
      @expand-change="handleExpandChange"
    >
      <template #first>
        <div :class="`h-full min-w-[500px] px-[24px] pb-[16px] ${activeLayout === 'horizontal' ? ' pr-[16px]' : ''}`">
          <a-tabs v-model:active-key="contentTab" class="no-content">
            <a-tab-pane v-for="item of contentTabList" :key="item.value" :title="item.label" />
          </a-tabs>
          <a-divider margin="0" class="!mb-[16px]"></a-divider>
          <debugHeader :params="activeDebug.params" :layout="activeLayout" :second-box-height="secondBoxHeight" />
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
            <div class="ml-[4px] mr-[24px] font-medium">{{ t('ms.apiTestDebug.responseContent') }}</div>
            <a-radio-group
              v-model:model-value="activeLayout"
              type="button"
              size="small"
              @change="handleActiveLayoutChange"
            >
              <a-radio value="vertical">{{ t('ms.apiTestDebug.vertical') }}</a-radio>
              <a-radio value="horizontal">{{ t('ms.apiTestDebug.horizontal') }}</a-radio>
            </a-radio-group>
          </div>
        </div>
      </template>
    </MsSplitBox>
  </div>
</template>

<script setup lang="ts">
  import { debounce } from 'lodash-es';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsEditableTab from '@/components/pure/ms-editable-tab/index.vue';
  import { TabItem } from '@/components/pure/ms-editable-tab/types';
  import MsSplitBox from '@/components/pure/ms-split-box/index.vue';
  import apiMethodName from '../../../components/apiMethodName.vue';
  import debugHeader from './header.vue';

  import { useI18n } from '@/hooks/useI18n';

  import { RequestComposition, RequestMethods } from '@/enums/apiEnum';

  const { t } = useI18n();

  const initDefaultId = `debug-${Date.now()}`;
  const activeTab = ref<string | number>(initDefaultId);
  const debugTabs = ref<TabItem[]>([
    {
      id: initDefaultId,
      label: t('ms.apiTestDebug.newApi'),
      closable: true,
      method: RequestMethods.GET,
      unSave: true,
      params: [],
    },
  ]);
  const debugUrl = ref('');
  const activeDebug = ref<TabItem>(debugTabs.value[0]);

  function setActiveDebug(item: TabItem) {
    activeDebug.value = item;
  }

  function addDebugTab() {
    const id = `debug-${Date.now()}`;
    debugTabs.value.push({
      id,
      label: t('ms.apiTestDebug.newApi'),
      closable: true,
      method: RequestMethods.GET,
      unSave: true,
      params: [],
    });
    activeTab.value = id;
  }

  function closeDebugTab(tab: TabItem) {
    const index = debugTabs.value.findIndex((item) => item.id === tab.id);
    if (activeTab.value === tab.id) {
      activeTab.value = debugTabs.value[0]?.id || '';
    }
    debugTabs.value.splice(index, 1);
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

  const contentTab = ref(RequestComposition.HEADER);
  const contentTabList = [
    {
      value: RequestComposition.HEADER,
      label: t('ms.apiTestDebug.header'),
    },
    {
      value: RequestComposition.BODY,
      label: t('ms.apiTestDebug.body'),
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
      label: t('ms.apiTestDebug.prefix'),
    },
    {
      value: RequestComposition.POST_CONDITION,
      label: t('ms.apiTestDebug.postCondition'),
    },
    {
      value: RequestComposition.ASSERTION,
      label: t('ms.apiTestDebug.assertion'),
    },
    {
      value: RequestComposition.AUTH,
      label: t('ms.apiTestDebug.auth'),
    },
    {
      value: RequestComposition.SETTING,
      label: t('ms.apiTestDebug.setting'),
    },
  ];

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
  }
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
