<template>
  <div class="flex gap-[8px] px-[16px] pt-[16px]">
    <a-select v-model:model-value="currentTab" class="w-[80px]" :options="tabOptions" @change="currentTabChange" />
    <MsEditableTab
      v-model:active-tab="activeApiTab"
      v-model:tabs="apiTabs"
      :show-add="false"
      class="flex-1 overflow-hidden"
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
  <api
    v-show="currentTab === 'api'"
    ref="apiRef"
    :member-options="memberOptions"
    :module-tree="props.moduleTree"
    :active-module="props.activeModule"
    :offspring-ids="props.offspringIds"
    :selected-protocols="props.selectedProtocols"
  />
  <api-case
    v-show="currentTab === 'case'"
    :member-options="memberOptions"
    :active-module="props.activeModule"
    :offspring-ids="props.offspringIds"
    :selected-protocols="props.selectedProtocols"
  ></api-case>
</template>

<script setup lang="ts">
  import MsEditableTab from '@/components/pure/ms-editable-tab/index.vue';
  import api from './api/apiTable.vue';
  import apiCase from './case/caseTable.vue';
  import apiMethodName from '@/views/api-test/components/apiMethodName.vue';
  import { RequestParam } from '@/views/api-test/components/requestComposition/index.vue';

  import { getProjectOptions } from '@/api/modules/project-management/projectMember';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';

  import { ModuleTreeNode } from '@/models/common';

  const props = defineProps<{
    activeModule: string;
    offspringIds: string[];
    selectedProtocols: string[]; // 查看的协议类型
    moduleTree: ModuleTreeNode[]; // 模块树
  }>();

  const { t } = useI18n();
  const appStore = useAppStore();

  const currentTab = ref('api');
  const tabOptions = [
    { label: 'API', value: 'api' },
    { label: 'CASE', value: 'case' },
  ];

  const apiTabs = ref<RequestParam[]>([
    {
      id: 'all',
      label: t('apiTestManagement.allApi'),
      closable: false,
    } as RequestParam,
  ]);
  const activeApiTab = ref<RequestParam>(apiTabs.value[0] as RequestParam);

  const memberOptions = ref<{ label: string; value: string }[]>([]);

  async function initMemberOptions() {
    memberOptions.value = await getProjectOptions(appStore.currentProjectId);
    memberOptions.value = memberOptions.value.map((e: any) => ({ label: e.name, value: e.id }));
  }

  // 下拉框切换
  function currentTabChange(val: any) {
    apiTabs.value[0].label = val === 'api' ? t('apiTestManagement.allApi') : t('case.allCase');
  }

  onBeforeMount(() => {
    initMemberOptions();
  });
</script>

<style lang="less" scoped>
  .ms-api-tab-nav {
    @apply h-full;
    :deep(.arco-tabs-content) {
      height: calc(100% - 51px);
      .arco-tabs-content-list {
        @apply h-full;
        .arco-tabs-pane {
          @apply h-full;
        }
      }
    }
    :deep(.arco-tabs-nav) {
      border-bottom: 1px solid var(--color-text-n8);
    }
  }
</style>
