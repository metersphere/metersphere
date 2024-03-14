<template>
  <div class="flex flex-col gap-[16px]">
    <div class="action-line">
      <div class="action-group">
        <a-checkbox
          v-model:model-value="stepInfo.checkedAll"
          :indeterminate="stepInfo.indeterminate"
          @change="handleChangeAll"
        />
        <div class="flex items-center gap-[4px]">
          {{ t('apiScenario.sum') }}
          <div class="text-[rgb(var(--primary-5))]">{{ stepInfo.steps.length }}</div>
          {{ t('apiScenario.steps') }}
        </div>
      </div>
      <div v-if="stepInfo.checkedAll || stepInfo.indeterminate" class="action-group">
        <a-tooltip :content="stepInfo.isExpand ? t('apiScenario.collapseAllStep') : t('apiScenario.expandAllStep')">
          <a-button
            type="outline"
            class="expand-step-btn arco-btn-outline--secondary"
            size="mini"
            @click="expandAllStep"
          >
            <MsIcon v-if="stepInfo.isExpand" type="icon-icon_comment_collapse_text_input" />
            <MsIcon v-else type="icon-icon_comment_expand_text_input" />
          </a-button>
        </a-tooltip>
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
      </div>
      <template v-else>
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
        <div class="action-group ml-auto">
          <a-input-search
            v-model:model-value="keyword"
            :placeholder="t('apiScenario.searchByName')"
            allow-clear
            class="w-[200px]"
            @search="searchStep"
            @press-enter="searchStep"
          />
          <a-button type="outline" class="arco-btn-outline--secondary !mr-0 !p-[8px]" @click="refreshStepInfo">
            <template #icon>
              <icon-refresh class="text-[var(--color-text-4)]" />
            </template>
          </a-button>
        </div>
      </template>
    </div>
    <a-dropdown class="scenario-action-dropdown">
      <a-button type="dashed" class="add-step-btn" long>
        <div class="flex items-center gap-[8px]">
          <icon-plus />
          {{ t('apiScenario.addStep') }}
        </div>
      </a-button>
      <template #content>
        <a-dgroup :title="t('apiScenario.requestScenario')">
          <a-doption>{{ t('apiScenario.importSystemApi') }}</a-doption>
          <a-doption>{{ t('apiScenario.customApi') }}</a-doption>
        </a-dgroup>
        <a-dgroup :title="t('apiScenario.logicControl')">
          <a-doption>
            <div class="flex w-full items-center justify-between">
              {{ t('apiScenario.loopControl') }}
              <MsButton type="text" @click="openTutorial">{{ t('apiScenario.tutorial') }}</MsButton>
            </div>
          </a-doption>
          <a-doption>{{ t('apiScenario.conditionControl') }}</a-doption>
          <a-doption>{{ t('apiScenario.onlyOnceControl') }}</a-doption>
        </a-dgroup>
        <a-dgroup :title="t('apiScenario.other')">
          <a-doption>{{ t('apiScenario.scriptOperation') }}</a-doption>
          <a-doption>{{ t('apiScenario.waitTime') }}</a-doption>
        </a-dgroup>
      </template>
    </a-dropdown>
  </div>
</template>

<script setup lang="ts">
  import dayjs from 'dayjs';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import executeStatus from '../common/executeStatus.vue';
  import stepType from '../common/stepType.vue';

  import { useI18n } from '@/hooks/useI18n';

  import { ScenarioExecuteStatus, ScenarioStepType } from '@/enums/apiEnum';

  export interface ScenarioStepItem {
    id: string | number;
    type: ScenarioStepType;
    name: string;
    description: string;
    status: ScenarioExecuteStatus;
    children?: ScenarioStepItem[];
  }

  export interface ScenarioStepInfo {
    id: string | number;
    steps: ScenarioStepItem[];
    checkedAll: boolean; // 是否全选
    indeterminate: boolean; // 是否半选
    isExpand: boolean; // 是否全部展开
    executeTime: string; // 执行时间
    executeSuccessCount?: number; // 执行成功数量
    executeFailCount?: number; // 执行失败数量
  }

  const { t } = useI18n();

  const keyword = ref('');
  const stepInfo = ref<ScenarioStepInfo>({
    id: new Date().getTime(),
    steps: [],
    checkedAll: false,
    indeterminate: false,
    isExpand: false,
    executeTime: dayjs().format('YYYY-MM-DD HH:mm:ss'),
    executeSuccessCount: 0,
    executeFailCount: 0,
  });

  function handleChangeAll(value: boolean | (string | number | boolean)[]) {
    stepInfo.value.indeterminate = false;
    if (value) {
      stepInfo.value.checkedAll = true;
    } else {
      stepInfo.value.checkedAll = false;
    }
  }

  function expandAllStep() {
    stepInfo.value.isExpand = !stepInfo.value.isExpand;
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

  function openTutorial() {
    window.open('https://zhuanlan.zhihu.com/p/597905464?utm_id=0', '_blank');
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
  .add-step-btn {
    @apply bg-white;

    padding: 4px;
    border: 1px dashed rgb(var(--primary-3));
    color: rgb(var(--primary-5));
    &:hover,
    &:focus {
      border: 1px dashed rgb(var(--primary-5));
      color: rgb(var(--primary-5));
      background-color: rgb(var(--primary-1));
    }
  }
</style>
