<template>
  <div class="box">
    <div class="left" :class="getStyleClass()">
      <div
        v-for="item of menuTab"
        :key="item.value"
        :class="`${activeTask === item.value ? 'active' : ''} item`"
        @click="toggleTask(item.value)"
      >
        <div class="mr-2">
          {{ item.label }}
        </div>
        <a-badge
          v-if="getTextFunc(item.value) !== ''"
          :class="`${item.value === activeTask ? 'active-badge' : ''} mt-[2px]`"
          :max-count="99"
          :text="getTextFunc(item.value)"
        />
      </div>
    </div>
    <div class="right">
      <a-tabs v-model:active-key="activeTab" class="no-content">
        <a-tab-pane v-for="item of rightTabList" :key="item.value" :title="item.label" />
      </a-tabs>
      <a-divider margin="0" class="!mb-[16px]"></a-divider>
      <!-- 接口用例列表-->
      <ApiCase
        v-if="
          activeTask === 'real' && (activeTab === TaskCenterEnum.API_CASE || activeTab === TaskCenterEnum.API_SCENARIO)
        "
        :name="listName"
        :module-type="activeTab"
        :group="props.group"
      />
      <!-- 测试计划列表-->
      <TestPlan
        v-if="activeTask === 'real' && activeTab === TaskCenterEnum.TEST_PLAN"
        :name="listName"
        :group="props.group"
      />
      <ScheduledTask v-if="activeTask === 'timing'" :name="listName" :group="props.group" :module-type="activeTab" />
    </div>
  </div>
</template>

<script setup lang="ts">
  import { useRoute } from 'vue-router';

  import ApiCase from './apiCase.vue';
  import ScheduledTask from './scheduledTask.vue';
  import TestPlan from './testPlan.vue';

  import {
    getOrgRealTotal,
    getOrgScheduleTotal,
    getProjectRealTotal,
    getProjectScheduleTotal,
    getSystemRealTotal,
    getSystemScheduleTotal,
  } from '@/api/modules/project-management/taskCenter';
  import { useI18n } from '@/hooks/useI18n';

  import { TaskCenterEnum } from '@/enums/taskCenter';

  import type { ExtractedKeys } from './utils';
  import { on } from 'events';

  const { t } = useI18n();

  const props = defineProps<{
    group: 'system' | 'organization' | 'project';
    mode?: 'modal' | 'normal';
  }>();

  const route = useRoute();

  const realTabList = ref([
    {
      value: TaskCenterEnum.API_CASE,
      label: t('project.taskCenter.interfaceCase'),
    },
    {
      value: TaskCenterEnum.API_SCENARIO,
      label: t('project.taskCenter.apiScenario'),
    },
    {
      value: TaskCenterEnum.TEST_PLAN,
      label: t('project.taskCenter.testPlan'),
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
    {
      value: TaskCenterEnum.TEST_PLAN,
      label: t('project.taskCenter.testPlan'),
    },
  ]);

  const activeTask = ref<string>((route.query.tab as string) || 'real');
  const activeTab = ref<ExtractedKeys>((route.query.type as ExtractedKeys) || TaskCenterEnum.API_CASE);

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
    return props.mode === 'modal' ? ['p-0', 'pt-[16px]', 'pr-[16px]'] : ['p-[16px]'];
  }

  const listName = computed(() => {
    return rightTabList.value.find((item) => item.value === activeTab.value)?.label || '';
  });

  export type menuType = 'real' | 'timing';

  const menuTab: { value: menuType; label: string }[] = [
    {
      value: 'real',
      label: t('project.taskCenter.realTimeTask'),
    },
    {
      value: 'timing',
      label: t('project.taskCenter.scheduledTask'),
    },
  ];

  const getTotalMap: Record<menuType, any> = {
    real: {
      system: getSystemRealTotal,
      organization: getOrgRealTotal,
      project: getProjectRealTotal,
    },
    timing: {
      system: getSystemScheduleTotal,
      organization: getOrgScheduleTotal,
      project: getProjectScheduleTotal,
    },
  };

  const totalMap = ref<Record<menuType, number>>({
    real: 0,
    timing: 0,
  });

  async function getTotal() {
    try {
      const [timingTotal, realTotal] = await Promise.all([
        getTotalMap.timing[props.group](),
        getTotalMap.real[props.group](),
      ]);
      totalMap.value.timing = timingTotal;
      totalMap.value.real = realTotal;
    } catch (error) {
      console.log(error);
    }
  }

  function getTextFunc(activeKey: menuType) {
    return totalMap.value[activeKey] > 99 ? '99+' : `${totalMap.value[activeKey]}` || '';
  }

  onMounted(() => {
    getTotal();
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
        display: flex;
        align-items: center;
        padding: 0 20px;
        height: 38px;
        font-size: 14px;
        border-radius: 4px;
        cursor: pointer;
        line-height: 38px;
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
