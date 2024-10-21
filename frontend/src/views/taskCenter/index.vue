<template>
  <div>
    <a-tabs v-model:active-key="activeTab" class="no-content" @change="handleTabChange">
      <a-tab-pane v-for="item of tabList" :key="item.value" :title="item.label" />
    </a-tabs>
    <a-divider margin="0"></a-divider>
    <div class="ms-taskCenter-content">
      <Suspense>
        <caseTaskTable
          v-if="activeTab === TaskCenterEnum.CASE"
          :type="props.type"
          @go-detail="goTaskDetail"
          @init="activeTaskId = ''"
        />
        <caseTaskDetailTable v-else-if="activeTab === TaskCenterEnum.DETAIL" :id="activeTaskId" :type="props.type" />
        <systemTaskTable v-else-if="activeTab === TaskCenterEnum.BACKEND" :type="props.type" />
      </Suspense>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { useRoute } from 'vue-router';

  import caseTaskDetailTable from './component/caseTaskDetailTable.vue';
  import caseTaskTable from './component/caseTaskTable.vue';
  import systemTaskTable from './component/systemTaskTable.vue';

  import { useI18n } from '@/hooks/useI18n';
  import useLocalForage from '@/hooks/useLocalForage';
  import useGlobalStore from '@/store/modules/global';

  import { GlobalEventNameEnum } from '@/enums/commonEnum';
  import { TaskCenterEnum } from '@/enums/taskCenter';

  const props = defineProps<{
    type: 'system' | 'org' | 'project';
    mode?: 'modal' | 'normal';
  }>();

  const { t } = useI18n();
  const route = useRoute();
  const globalStore = useGlobalStore();
  const { getItem, setItem } = useLocalForage();

  const tabList = ref([
    {
      value: TaskCenterEnum.CASE,
      label: t('ms.taskCenter.caseTaskList'),
    },
    {
      value: TaskCenterEnum.DETAIL,
      label: t('ms.taskCenter.caseTaskDetailList'),
    },
    {
      value: TaskCenterEnum.BACKEND,
      label: t('ms.taskCenter.backendTaskList'),
    },
  ]);

  const activeTab = ref<TaskCenterEnum>((route.query.type as TaskCenterEnum) || TaskCenterEnum.CASE);
  const activeTaskId = ref('');

  function goTaskDetail(id: string) {
    activeTaskId.value = id;
    activeTab.value = TaskCenterEnum.DETAIL;
  }

  function handleTabChange(key: string | number) {
    setItem('taskCenterActiveTab', key);
  }

  watch(
    () => globalStore.getGlobalEvent,
    (event) => {
      if (event && event.id && event.name === GlobalEventNameEnum.OPEN_TASK_CENTER) {
        activeTab.value = event.params?.tab;
        setItem('taskCenterActiveTab', event.params?.tab);
      }
    }
  );

  onBeforeMount(async () => {
    activeTab.value = (await getItem('taskCenterActiveTab')) || TaskCenterEnum.CASE;
  });
</script>

<style scoped lang="less">
  .no-content {
    :deep(.arco-tabs-content) {
      padding-top: 0;
    }
  }
</style>
