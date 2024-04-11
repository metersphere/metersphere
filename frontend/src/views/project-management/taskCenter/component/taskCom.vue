<template>
  <div class="box h-full">
    <div class="left" :class="getStyleClass()">
      <div class="item" :class="[activeTask === 'real' ? 'active' : '']" @click="toggleTask('real')">
        {{ t('project.taskCenter.realTimeTask') }}
      </div>
      <div class="item" :class="[activeTask === 'timing' ? 'active' : '']" @click="toggleTask('timing')">
        {{ t('project.taskCenter.scheduledTask') }}
      </div>
    </div>
    <div class="right">
      <a-tabs v-model:active-key="activeTab" class="no-content">
        <a-tab-pane v-for="item of rightTabList" :key="item.value" :title="item.label" />
      </a-tabs>
      <a-divider margin="0" class="!mb-[16px]"></a-divider>
      <!-- 接口用例列表-->
      <ApiCase v-if="activeTask === 'real'" :name="listName" :module-type="activeTab" :group="props.group" />
      <ScheduledTask v-if="activeTask === 'timing'" :name="listName" :group="props.group" :module-type="activeTab" />
    </div>
  </div>
</template>

<script setup lang="ts">
  import { ref } from 'vue';

  import ApiCase from './apiCase.vue';
  import ScheduledTask from './scheduledTask.vue';

  import { useI18n } from '@/hooks/useI18n';

  import { TaskCenterEnum } from '@/enums/taskCenter';

  const { t } = useI18n();
  const activeTab = ref<keyof typeof TaskCenterEnum>(TaskCenterEnum.API_CASE);

  const props = defineProps<{
    group: 'system' | 'organization' | 'project';
    mode?: 'modal' | 'normal';
  }>();

  const realTabList = ref([
    {
      value: TaskCenterEnum.API_CASE,
      label: t('project.taskCenter.interfaceCase'),
    },
    {
      value: TaskCenterEnum.API_SCENARIO,
      label: t('project.taskCenter.apiScenario'),
    },
    // TODO 第一个版本目前不上以下几类
    // {
    //   value: TaskCenterEnum.UI_TEST,
    //   label: t('project.taskCenter.uiDefaultFile'),
    // },
    // {
    //   value: TaskCenterEnum.LOAD_TEST,
    //   label: t('project.taskCenter.performanceTest'),
    // },
    // {
    //   value: TaskCenterEnum.TEST_PLAN,
    //   label: t('project.taskCenter.testPlan'),
    // },
  ]);

  const timingTabList = ref([
    {
      value: TaskCenterEnum.API_SCENARIO,
      label: t('project.taskCenter.apiScenario'),
    },
    {
      value: TaskCenterEnum.API_IMPORT,
      label: t('project.taskCenter.apiImport'),
    },
  ]);

  const activeTask = ref('real');

  const rightTabList = computed(() => {
    return activeTask.value === 'real' ? realTabList.value : timingTabList.value;
  });

  function toggleTask(activeType: string) {
    activeTask.value = activeType;
    if (activeTask.value === 'real') {
      activeTab.value = TaskCenterEnum.API_CASE;
    } else {
      activeTab.value = TaskCenterEnum.API_SCENARIO;
    }
  }

  function getStyleClass() {
    return props.mode === 'modal' ? ['p-0', 'pt-[24px]', 'pr-[24px]'] : ['p-[24px]'];
  }

  const listName = computed(() => {
    return rightTabList.value.find((item) => item.value === activeTab.value)?.label || '';
  });
</script>

<style scoped lang="less">
  .box {
    display: flex;
    height: 100%;
    .left {
      width: 252px;
      height: 100%;
      border-right: 1px solid var(--color-text-n8);
      .item {
        padding: 0 20px;
        height: 38px;
        font-size: 14px;
        line-height: 38px;
        border-radius: 4px;
        cursor: pointer;
        &.active {
          color: rgb(var(--primary-5));
          background: rgb(var(--primary-1));
        }
      }
    }
    .right {
      width: calc(100% - 300px);
      flex-grow: 1; /* 自适应 */
      height: 100%;
    }
  }
  .no-content {
    :deep(.arco-tabs-content) {
      padding-top: 0;
    }
  }
</style>
