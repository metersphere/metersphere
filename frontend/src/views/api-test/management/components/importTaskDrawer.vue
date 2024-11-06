<template>
  <MsDrawer v-model:visible="taskDrawerVisible" :width="960" :title="t('apiTestManagement.timeTask')" :footer="false">
    <div class="mb-[16px] flex items-center justify-end">
      <a-input-search
        v-model:model-value="keyword"
        :placeholder="t('apiTestManagement.searchTaskPlaceholder')"
        allow-clear
        class="mr-[8px] w-[240px]"
        @search="loadTaskList"
        @press-enter="loadTaskList"
        @clear="loadTaskList"
      />
    </div>
    <ms-base-table v-bind="propsRes" no-disable v-on="propsEvent">
      <template #status="{ record }">
        <a-switch
          v-model:model-value="record.enable"
          type="line"
          size="small"
          :before-change="() => handleBeforeEnableChange(record)"
        ></a-switch>
      </template>
      <template #action="{ record }">
        <MsButton @click="deleteTask(record)">
          {{ t('common.delete') }}
        </MsButton>
      </template>
    </ms-base-table>
  </MsDrawer>
</template>

<script setup lang="ts">
  import { Message } from '@arco-design/web-vue';
  import dayjs from 'dayjs';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';

  import { deleteDefinitionSchedule, switchDefinitionSchedule } from '@/api/modules/api-test/management';
  import { getScheduleProApiCaseList } from '@/api/modules/taskCenter/project';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import { characterLimit } from '@/utils';

  import { TimingTaskCenterApiCaseItem } from '@/models/projectManagement/taskCenter';
  import { TaskCenterEnum } from '@/enums/taskCenter';

  const { t } = useI18n();
  const { openModal } = useModal();

  const taskDrawerVisible = defineModel<boolean>('visible', { required: true });
  const keyword = ref('');
  const columns: MsTableColumn = [
    {
      title: 'apiTestManagement.resourceID',
      dataIndex: 'resourceNum',
      slotName: 'resourceNum',
      width: 90,
      showInTable: true,
      showTooltip: true,
    },
    {
      title: 'apiTestManagement.resourceName',
      slotName: 'resourceName',
      dataIndex: 'resourceName',
      width: 200,
      showDrag: true,
      showTooltip: true,
    },
    {
      title: 'apiTestManagement.swaggerUrl',
      slotName: 'swaggerUrl',
      dataIndex: 'swaggerUrl',
      width: 300,
      showDrag: false,
      showTooltip: true,
      columnSelectorDisabled: true,
      showInTable: true,
    },
    {
      title: 'apiTestManagement.taskRunRule',
      dataIndex: 'value',
      width: 140,
    },
    {
      title: 'common.status',
      dataIndex: 'status',
      slotName: 'status',
      width: 80,
    },
    {
      title: 'apiTestManagement.taskNextRunTime',
      dataIndex: 'nextTime',
      showTooltip: true,
      width: 180,
    },
    {
      title: 'apiTestManagement.taskOperator',
      dataIndex: 'createUserName',
      showTooltip: true,
      width: 150,
    },
    {
      title: 'apiTestManagement.taskOperationTime',
      dataIndex: 'createTime',
      width: 180,
    },
    {
      title: 'common.operation',
      slotName: 'action',
      dataIndex: 'operation',
      fixed: 'right',
      width: 60,
    },
  ];
  const { propsRes, propsEvent, loadList, setLoadListParams } = useTable(
    getScheduleProApiCaseList,
    {
      columns,
      scroll: { x: '100%' },
    },
    (item) => ({
      ...item,
      operationTime: dayjs(item.operationTime).format('YYYY-MM-DD HH:mm:ss'),
      nextTime: item.nextTime ? dayjs(item.nextTime).format('YYYY-MM-DD HH:mm:ss') : '-',
    })
  );
  function loadTaskList() {
    setLoadListParams({
      keyword: keyword.value,
      moduleType: TaskCenterEnum.API_IMPORT,
    });
    loadList();
  }

  watch(
    () => taskDrawerVisible.value,
    (value) => {
      if (value) {
        loadTaskList();
      }
    },
    {
      immediate: true,
    }
  );

  async function handleBeforeEnableChange(record: TimingTaskCenterApiCaseItem) {
    try {
      await switchDefinitionSchedule(record.id);
      Message.success(
        t(record.enable ? 'apiTestManagement.disableTaskSuccess' : 'apiTestManagement.enableTaskSuccess')
      );
      return true;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
      return false;
    }
  }

  function deleteTask(record: TimingTaskCenterApiCaseItem) {
    openModal({
      type: 'error',
      title: t('ms.taskCenter.deleteTaskTitle', { name: characterLimit(record?.taskName) }),
      content: t('ms.taskCenter.deleteTimeTaskTip'),
      okText: t('common.confirmDelete'),
      cancelText: t('common.cancel'),
      okButtonProps: {
        status: 'danger',
      },
      maskClosable: false,
      onBeforeOk: async () => {
        try {
          await deleteDefinitionSchedule(record.id);
          Message.success(t('common.deleteSuccess'));
          loadTaskList();
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        }
      },
      hideCancel: false,
    });
  }
</script>

<style lang="less" scoped></style>
