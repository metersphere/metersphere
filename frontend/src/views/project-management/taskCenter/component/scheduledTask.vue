<template>
  <div class="p-4 pt-0">
    <div class="mb-4 flex items-center justify-between">
      <!-- TODO这个版本不上 -->
      <a-button type="primary">
        {{ t('project.taskCenter.createTask') }}
      </a-button>
      <a-input-search
        v-model:model-value="keyword"
        :placeholder="t('caseManagement.featureCase.searchByNameAndId')"
        allow-clear
        class="mx-[8px] w-[240px]"
        @search="searchList"
        @press-enter="searchList"
      ></a-input-search>
    </div>
    <ms-base-table
      v-bind="propsRes"
      ref="tableRef"
      :action-config="tableBatchActions"
      v-on="propsEvent"
      @batch-action="handleTableBatch"
    >
      <template #resourceNum="{ record }">
        <a-button type="text" class="flex w-full">{{ record.resourceNum }}</a-button>
      </template>
      <template #resourceName="{ record }">
        <a-button type="text" class="flex w-full">{{ record.resourceName }}</a-button>
      </template>
      <template #operation="{ record }">
        <a-switch v-model="record.enable" size="small" type="line" />
        <!-- TODO这一版不上 -->
        <!-- <a-divider direction="vertical" />
        <MsButton class="!mr-0" @click="edit(record)">{{ t('common.edit') }}</MsButton>
        <a-divider direction="vertical" />
        <MsTableMoreAction :list="moreActions" @select="handleMoreActionSelect" /> -->
      </template>
    </ms-base-table>
  </div>
</template>

<script setup lang="ts">
  import { ref } from 'vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { BatchActionParams, BatchActionQueryParams, MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsTableMoreAction from '@/components/pure/ms-table-more-action/index.vue';
  import type { ActionsItem } from '@/components/pure/ms-table-more-action/types';

  import {
    getScheduleOrgApiCaseList,
    getScheduleProApiCaseList,
    getScheduleSysApiCaseList,
  } from '@/api/modules/project-management/taskCenter';
  import { useI18n } from '@/hooks/useI18n';

  import { TaskCenterEnum } from '@/enums/taskCenter';

  const { t } = useI18n();

  const props = defineProps<{
    group: 'system' | 'organization' | 'project';
    moduleType: keyof typeof TaskCenterEnum;
    name: string;
  }>();

  const keyword = ref<string>('');
  const columns: MsTableColumn = [
    {
      title: 'project.taskCenter.resourceID',
      dataIndex: 'resourceNum',
      slotName: 'resourceNum',
      width: 300,
      showInTable: true,
    },
    {
      title: 'project.taskCenter.resourceName',
      slotName: 'resourceName',
      dataIndex: 'resourceName',
      width: 200,
      showDrag: true,
    },
    {
      title: 'project.taskCenter.resourceClassification',
      dataIndex: 'resourceType',
      slotName: 'resourceType',
      showInTable: true,
      width: 150,
      showDrag: true,
    },
    {
      title: 'project.taskCenter.operationRule',
      dataIndex: 'value',
      slotName: 'value',
      showInTable: true,
      isTag: true,
      width: 150,
      showDrag: true,
    },
    {
      title: 'project.taskCenter.operator',
      slotName: 'createUserName',
      dataIndex: 'createUserName',
      showInTable: true,
      width: 200,
      showDrag: true,
    },
    {
      title: 'project.taskCenter.operating',
      slotName: 'createTime',
      dataIndex: 'createTime',
      showInTable: true,
      width: 200,
      showDrag: true,
    },
    {
      title: 'project.taskCenter.nextExecutionTime',
      slotName: 'nextTime',
      dataIndex: 'nextTime',
      showInTable: true,
      width: 300,
      showDrag: true,
    },
    {
      title: 'common.operation',
      slotName: 'operation',
      dataIndex: 'operation',
      width: 120,
      fixed: 'right',
    },
  ];

  const loadRealMap = ref({
    system: getScheduleSysApiCaseList,
    organization: getScheduleOrgApiCaseList,
    project: getScheduleProApiCaseList,
  });

  const { propsRes, propsEvent, loadList, setLoadListParams, resetSelector, setProps } = useTable(
    loadRealMap.value[props.group],
    {
      columns,
      scroll: {
        x: '100%',
      },
      showSetting: false,
      selectable: true,
      heightUsed: 300,
      enableDrag: true,
      showSelectAll: true,
    }
  );

  function initData() {
    setLoadListParams({
      keyword: keyword.value,
      moduleType: props.moduleType,
    });
    loadList();
  }

  function searchList() {
    resetSelector();
    initData();
  }

  const tableBatchActions = {
    baseAction: [
      {
        label: 'project.taskCenter.batchStop',
        eventTag: 'batchStop',
      },
      {
        label: 'project.taskCenter.batchExecution',
        eventTag: 'batchExecution',
      },
    ],
  };

  function handleTableBatch(event: BatchActionParams, params: BatchActionQueryParams) {}

  function edit(record: any) {}

  const moreActions: ActionsItem[] = [
    {
      label: 'common.delete',
      danger: true,
      eventTag: 'delete',
    },
  ];

  function handleMoreActionSelect(item: ActionsItem) {}

  onBeforeMount(() => {
    initData();
  });

  watch(
    () => props.moduleType,
    (val) => {
      if (val) {
        resetSelector();
        initData();
      }
    }
  );
</script>

<style scoped></style>
