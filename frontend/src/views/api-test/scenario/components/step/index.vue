<template>
  <div class="flex flex-col gap-[16px]">
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
          <div class="text-[rgb(var(--primary-5))]">{{ stepInfo.steps.length }}</div>
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
        <a-input-search
          v-model:model-value="keyword"
          :placeholder="t('apiScenario.searchByName')"
          allow-clear
          class="w-[200px]"
          @search="searchStep"
          @press-enter="searchStep"
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
    <div>
      <stepTree ref="stepTreeRef" v-model:checked-keys="checkedKeys" :steps="stepInfo.steps" />
    </div>
  </div>
</template>

<script setup lang="ts">
  // import dayjs from 'dayjs';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import stepTree, { ScenarioStepItem } from './stepTree.vue';

  import { useI18n } from '@/hooks/useI18n';

  import { ScenarioExecuteStatus, ScenarioStepType } from '@/enums/apiEnum';

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

  const { t } = useI18n();

  const checkedAll = ref(false); // 是否全选
  const indeterminate = ref(false); // 是否半选
  const isExpandAll = ref(false); // 是否展开全部
  const checkedKeys = ref<string[]>([]); // 选中的key
  const stepTreeRef = ref<InstanceType<typeof stepTree>>();
  const keyword = ref('');
  const stepInfo = ref<ScenarioStepInfo>({
    id: new Date().getTime(),
    steps: [
      {
        id: 1,
        order: 1,
        checked: false,
        type: ScenarioStepType.CUSTOM_API,
        name: 'API1',
        description: 'API1描述',
        status: ScenarioExecuteStatus.SUCCESS,
        children: [
          {
            id: 11,
            order: 1,
            checked: false,
            type: ScenarioStepType.CUSTOM_API,
            name: 'API11',
            description: 'API11描述',
            status: ScenarioExecuteStatus.SUCCESS,
          },
          {
            id: 12,
            order: 2,
            checked: false,
            type: ScenarioStepType.CUSTOM_API,
            name: 'API12',
            description: 'API12描述',
            status: ScenarioExecuteStatus.SUCCESS,
          },
        ],
      },
      {
        id: 2,
        order: 2,
        checked: false,
        type: ScenarioStepType.CUSTOM_API,
        name: 'API1',
        description: 'API1描述',
        status: ScenarioExecuteStatus.SUCCESS,
      },
    ],
    executeTime: '',
    executeSuccessCount: 0,
    executeFailCount: 0,
  });

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
    } else if (val.length === stepInfo.value.steps.length) {
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

  function batchEnable() {
    console.log('批量启用');
  }

  function batchDisable() {
    console.log('批量禁用');
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

  function searchStep(val: string) {
    stepInfo.value.steps = stepInfo.value.steps.filter((item) => item.name.includes(val));
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
