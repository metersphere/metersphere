<template>
  <div class="flex h-full flex-col gap-[16px]">
    <div class="action-line">
      <div class="action-group">
        <a-checkbox
          v-show="stepInfo.steps.length > 0"
          v-model:model-value="checkedAll"
          :indeterminate="indeterminate"
          @change="handleChangeAll"
        />
        <div class="flex items-center gap-[4px]">
          {{ t('apiScenario.sum') }}
          <div class="text-[rgb(var(--primary-5))]">{{ totalStepCount }}</div>
          {{ t('apiScenario.steps') }}
        </div>
      </div>
      <div class="action-group">
        <a-tooltip :content="isExpandAll ? t('apiScenario.collapseAllStep') : t('apiScenario.expandAllStep')">
          <a-button
            v-show="stepInfo.steps.length > 0"
            type="outline"
            class="expand-step-btn arco-btn-outline--secondary"
            size="mini"
            @click="expandAllStep"
          >
            <MsIcon v-if="isExpandAll" type="icon-icon_comment_collapse_text_input" />
            <MsIcon v-else type="icon-icon_comment_expand_text_input" />
          </a-button>
        </a-tooltip>
        <template v-if="checkedAll || indeterminate">
          <a-button type="outline" size="mini" @click="batchEnable">
            {{ t('common.batchEnable') }}
          </a-button>
          <a-button type="outline" size="mini" @click="batchDisable">
            {{ t('common.batchDisable') }}
          </a-button>
          <a-button type="outline" size="mini" @click="batchDebug">
            {{ t('common.batchDebug') }}
          </a-button>
          <a-button type="outline" size="mini" @click="batchDelete">
            {{ t('common.batchDelete') }}
          </a-button>
        </template>
      </div>
      <template v-if="stepInfo.executeTime">
        <div class="action-group">
          <div class="text-[var(--color-text-4)]">{{ t('apiScenario.executeTime') }}</div>
          <div class="text-[var(--color-text-4)]">{{ stepInfo.executeTime }}</div>
        </div>
        <div class="action-group">
          <div class="text-[var(--color-text-4)]">{{ t('apiScenario.executeResult') }}</div>
          <div class="flex items-center gap-[4px]">
            <div class="text-[var(--color-text-1)]">{{ t('common.success') }}</div>
            <div class="text-[rgb(var(--success-6))]">{{ stepInfo.executeSuccessCount }}</div>
          </div>
          <div class="flex items-center gap-[4px]">
            <div class="text-[var(--color-text-1)]">{{ t('common.fail') }}</div>
            <div class="text-[rgb(var(--success-6))]">{{ stepInfo.executeFailCount }}</div>
          </div>
          <MsButton type="text" @click="checkReport">{{ t('apiScenario.checkReport') }}</MsButton>
        </div>
      </template>
      <div v-if="!checkedAll && !indeterminate" class="action-group ml-auto">
        <a-input
          v-model:model-value="keyword"
          :placeholder="t('apiScenario.searchByName')"
          allow-clear
          class="w-[200px]"
        />
        <a-button
          v-if="!props.isNew"
          type="outline"
          class="arco-btn-outline--secondary !mr-0 !p-[8px]"
          @click="refreshStepInfo"
        >
          <template #icon>
            <icon-refresh class="text-[var(--color-text-4)]" />
          </template>
        </a-button>
      </div>
    </div>
    <div class="h-[calc(100%-48px)]">
      <stepTree
        ref="stepTreeRef"
        v-model:steps="stepInfo.steps"
        v-model:checked-keys="checkedKeys"
        v-model:stepKeyword="keyword"
        :expand-all="isExpandAll"
      />
    </div>
  </div>
  <a-modal
    v-model:visible="batchToggleVisible"
    :title="isBatchEnable ? t('common.batchEnable') : t('common.batchDisable')"
    :width="480"
    :ok-text="isBatchEnable ? t('common.enable') : t('common.disable')"
    class="ms-modal-form"
    title-align="start"
    body-class="!p-0"
    @close="resetBatchToggle"
    @before-ok="handleBeforeBatchToggle"
  >
    <div class="mb-[8px] text-[var(--color-text-1)]">{{ t('apiScenario.range') }}</div>
    <a-radio-group v-model:model-value="batchToggleRange">
      <a-radio value="top">{{ t('apiScenario.topStep') }}</a-radio>
      <a-radio value="all">{{ t('apiScenario.allStep') }}</a-radio>
    </a-radio-group>
  </a-modal>
</template>

<script setup lang="ts">
  // import dayjs from 'dayjs';

  import { Message } from '@arco-design/web-vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import stepTree, { ScenarioStepItem } from './stepTree.vue';

  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';
  import { countNodes } from '@/utils/tree';

  export interface ScenarioStepInfo {
    id: string | number;
    steps: ScenarioStepItem[];
    executeTime?: string; // 执行时间
    executeSuccessCount?: number; // 执行成功数量
    executeFailCount?: number; // 执行失败数量
  }

  const props = defineProps<{
    isNew?: boolean; // 是否新建
  }>();

  const appStore = useAppStore();
  const { t } = useI18n();

  const stepInfo = defineModel<ScenarioStepInfo>('step', {
    required: true,
  });

  const checkedAll = ref(false); // 是否全选
  const indeterminate = ref(false); // 是否半选
  const isExpandAll = ref(false); // 是否展开全部
  const checkedKeys = ref<(string | number)[]>([]); // 选中的key
  const stepTreeRef = ref<InstanceType<typeof stepTree>>();
  const keyword = ref('');

  const totalStepCount = computed(() => countNodes(stepInfo.value.steps));

  function handleChangeAll(value: boolean | (string | number | boolean)[]) {
    indeterminate.value = false;
    if (value) {
      checkedAll.value = true;
    } else {
      checkedAll.value = false;
    }
    stepTreeRef.value?.checkAll(checkedAll.value);
  }

  watch(checkedKeys, (val) => {
    if (val.length === 0) {
      checkedAll.value = false;
      indeterminate.value = false;
    } else if (val.length === totalStepCount.value) {
      checkedAll.value = true;
      indeterminate.value = false;
    } else {
      checkedAll.value = false;
      indeterminate.value = true;
    }
  });

  function expandAllStep() {
    isExpandAll.value = !isExpandAll.value;
  }

  // 批量启用/禁用
  const isBatchEnable = ref(false);
  const batchToggleVisible = ref(false);
  const batchToggleRange = ref('top');
  function batchEnable() {
    batchToggleVisible.value = true;
    isBatchEnable.value = true;
  }

  function batchDisable() {
    batchToggleVisible.value = true;
    isBatchEnable.value = false;
  }

  function resetBatchToggle() {
    batchToggleVisible.value = false;
    batchToggleRange.value = 'top';
    isBatchEnable.value = false;
  }

  async function handleBeforeBatchToggle(done: (closed: boolean) => void) {
    try {
      let ids = checkedKeys.value;
      if (batchToggleRange.value === 'top') {
        ids = stepInfo.value.steps.map((item) => item.id);
      }
      console.log('ids', ids);
      await new Promise((resolve) => {
        setTimeout(() => {
          resolve(true);
        }, 1000);
      });
      done(true);
      Message.success(isBatchEnable.value ? t('common.enableSuccess') : t('common.disableSuccess'));
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  function batchDebug() {
    console.log('批量调试');
  }

  function batchDelete() {
    console.log('批量删除');
  }

  function checkReport() {
    console.log('查看报告');
  }

  function refreshStepInfo() {
    console.log('刷新步骤信息');
  }
</script>

<style lang="less">
  .scenario-action-dropdown {
    .arco-dropdown-list-wrapper {
      width: 200px;
      max-height: 100%;
      .arco-dropdown-option-content {
        @apply flex w-full;
      }
    }
  }
</style>

<style lang="less" scoped>
  .action-line {
    @apply flex items-center;

    gap: 16px;
    height: 32px;
    .action-group {
      @apply flex items-center;

      gap: 8px;
      .expand-step-btn {
        padding: 4px;
        .arco-icon {
          color: var(--color-text-4);
        }
        &:hover {
          border-color: rgb(var(--primary-5)) !important;
          background-color: rgb(var(--primary-1)) !important;
          .arco-icon {
            color: rgb(var(--primary-5));
          }
        }
      }
    }
  }
</style>
