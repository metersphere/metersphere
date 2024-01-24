<template>
  <ms-base-table
    v-if="showType === 'list'"
    v-bind="propsRes"
    ref="tableRef"
    class="mt-4"
    :action-config="tableBatchActions"
    :expanded-keys="expandedKeys"
    @selected-change="handleTableSelect"
    v-on="propsEvent"
    @batch-action="handleTableBatch"
  >
    <template #num="{ record }">
      <div v-if="(record.children || []).length > 0" class="mr-2 flex items-center" @click="expandHandler(record)">
        <MsIcon
          type="icon-icon_split-turn-down-left"
          class="arrowIcon mr-1 text-[16px]"
          :class="getIconClass(record)"
        />
        <span :class="getIconClass(record)">{{ (record.children || []).length || 0 }}</span>
      </div>
      <span>{{ record.id }}</span>
    </template>
    <template #statusFilter="{ columnConfig }">
      <a-trigger v-model:popup-visible="statusFilterVisible" trigger="click" @popup-visible-change="handleFilterHidden">
        <a-button type="text" class="arco-btn-text--secondary" @click="statusFilterVisible = true">
          {{ t(columnConfig.title as string) }}
          <icon-down :class="statusFilterVisible ? 'text-[rgb(var(--primary-5))]' : ''" />
        </a-button>
        <template #content>
          <div class="arco-table-filters-content">
            <div class="flex items-center justify-center px-[6px] py-[2px]">
              <a-checkbox-group v-model:model-value="statusFilters" direction="vertical" size="small">
                <a-checkbox v-for="key of Object.keys(reviewStatusMap)" :key="key" :value="key">
                  <a-tag
                    :color="reviewStatusMap[key].color"
                    :class="[reviewStatusMap[key].class, 'px-[4px]']"
                    size="small"
                  >
                    {{ t(reviewStatusMap[key].label) }}
                  </a-tag>
                </a-checkbox>
              </a-checkbox-group>
            </div>
          </div>
        </template>
      </a-trigger>
    </template>
    <template #name="{ record }">
      <a-button type="text" class="px-0">{{ record.name }}</a-button>
    </template>

    <template #status="{ record }">
      <statusTag :status="record.status" />
    </template>

    <template #passRate="{ record }">
      <div class="mr-[8px] w-[100px]">
        <passRateLine :review-detail="record" height="5px" />
      </div>
      <div class="text-[var(--color-text-1)]">
        {{ `${record.passRate || 0}%` }}
      </div>
    </template>

    <template #operation="{ record }">
      <MsButton>{{ t('testPlan.testPlanIndex.execution') }}</MsButton>
      <a-divider direction="vertical" :margin="8"></a-divider>
      <MsButton>{{ t('testPlan.testPlanIndex.copy') }}</MsButton>
      <a-divider direction="vertical" :margin="8"></a-divider>
      <MsTableMoreAction :list="moreActions" @select="handleMoreActionSelect($event, record)" />
    </template>
  </ms-base-table>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { Message } from '@arco-design/web-vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { BatchActionParams, BatchActionQueryParams, MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsTableMoreAction from '@/components/pure/ms-table-more-action/index.vue';
  import { ActionsItem } from '@/components/pure/ms-table-more-action/types';
  import passRateLine from '@/views/case-management/caseReview/components/passRateLine.vue';
  import statusTag from '@/views/case-management/caseReview/components/statusTag.vue';

  import { reviewStatusMap } from '@/config/caseManagement';
  import { useI18n } from '@/hooks/useI18n';
  import { useAppStore, useTableStore } from '@/store';

  import { ModuleTreeNode } from '@/models/projectManagement/file';
  import { ColumnEditTypeEnum, TableKeyEnum } from '@/enums/tableEnum';

  const tableStore = useTableStore();
  const { t } = useI18n();

  const columns: MsTableColumn = [
    {
      title: 'testPlan.testPlanIndex.ID',
      slotName: 'num',
      dataIndex: 'num',
      width: 200,
      showInTable: true,
      ellipsis: true,
      showDrag: false,
    },
    {
      title: 'testPlan.testPlanIndex.testPlanName',
      slotName: 'name',
      dataIndex: 'name',
      showInTable: true,
      showTooltip: true,
      width: 300,
      editType: ColumnEditTypeEnum.INPUT,
      sortable: {
        sortDirections: ['ascend', 'descend'],
      },
      ellipsis: true,
      showDrag: false,
    },
    {
      title: 'testPlan.testPlanIndex.desc',
      slotName: 'desc',
      showInTable: true,
      width: 200,
      showDrag: true,
    },
    {
      title: 'testPlan.testPlanIndex.status',
      dataIndex: 'status',
      slotName: 'status',
      titleSlotName: 'statusFilter',
      width: 150,
    },
    {
      title: 'testPlan.testPlanIndex.passRate',
      dataIndex: 'passRate',
      slotName: 'passRate',
      showInTable: true,
      width: 200,
      showDrag: true,
    },
    {
      title: 'testPlan.testPlanIndex.useCount',
      slotName: 'versionId',
      dataIndex: 'versionId',
      width: 300,
      showTooltip: true,
      showInTable: true,
      showDrag: true,
    },
    {
      title: 'testPlan.testPlanIndex.bugCount',
      slotName: 'moduleId',
      dataIndex: 'moduleId',
      showInTable: true,
      width: 300,
      showDrag: true,
    },
    {
      title: 'testPlan.testPlanIndex.belongModule',
      slotName: 'belongModule',
      dataIndex: 'belongModule',
      showInTable: true,
      showDrag: true,
    },
    {
      title: 'testPlan.testPlanIndex.creator',
      slotName: 'createUser',
      dataIndex: 'createUser',
      showInTable: true,
      width: 200,
      showDrag: true,
    },
    {
      title: 'testPlan.testPlanIndex.createTime',
      slotName: 'createTime',
      dataIndex: 'createTime',
      showInTable: true,
      sortable: {
        sortDirections: ['ascend', 'descend'],
      },
      width: 200,
      showDrag: true,
    },
    {
      title: 'testPlan.testPlanIndex.operation',
      slotName: 'operation',
      dataIndex: 'operation',
      fixed: 'right',
      width: 260,
      showInTable: true,
      showDrag: false,
    },
  ];

  /**
   * 更新测试计划名称
   */
  async function updatePlanName() {
    try {
      Message.success(t('common.updateSuccess'));
      return Promise.resolve(true);
    } catch (error) {
      console.log(error);
      return Promise.resolve(false);
    }
  }

  const keyword = ref<string>('');
  const scrollWidth = ref<number>(3400);
  const statusFilterVisible = ref(false);
  const statusFilters = ref<string[]>(Object.keys(reviewStatusMap));

  const tableBatchActions = {
    baseAction: [
      {
        label: 'caseManagement.featureCase.export',
        eventTag: 'export',
        children: [
          {
            label: 'caseManagement.featureCase.exportExcel',
            eventTag: 'exportExcel',
          },
          {
            label: 'caseManagement.featureCase.exportXMind',
            eventTag: 'exportXMind',
          },
        ],
      },
      {
        label: 'common.edit',
        eventTag: 'batchEdit',
      },
      {
        label: 'caseManagement.featureCase.moveTo',
        eventTag: 'batchMoveTo',
      },
      {
        label: 'caseManagement.featureCase.copyTo',
        eventTag: 'batchCopyTo',
      },
    ],
    moreAction: [
      {
        label: 'caseManagement.featureCase.addDemand',
        eventTag: 'addDemand',
      },
      {
        label: 'caseManagement.featureCase.associatedDemand',
        eventTag: 'associatedDemand',
      },
      {
        label: 'caseManagement.featureCase.generatingDependencies',
        eventTag: 'generatingDependencies',
      },
      {
        label: 'caseManagement.featureCase.addToPublic',
        eventTag: 'addToPublic',
      },
      {
        isDivider: true,
      },
      {
        label: 'common.delete',
        eventTag: 'delete',
        danger: true,
      },
    ],
  };

  const moreActions: ActionsItem[] = [
    {
      label: 'common.delete',
      danger: true,
      eventTag: 'delete',
    },
  ];

  const tableSelected = ref<(string | number)[]>([]);
  function handleTableSelect(selectArr: (string | number)[]) {
    tableSelected.value = selectArr;
  }

  const data = [
    {
      id: '100944',
      projectId: 'string',
      num: '100944',
      name: '系统示例',
      status: 'PREPARED',
      tags: ['string'],
      schedule: 'string',
      createUser: 'string',
      createTime: 'string',
      moduleName: 'string',
      moduleId: 'string',
      passCount: 0,
      unPassCount: 0,
      reviewedCount: 0,
      underReviewedCount: 0,

      children: [
        {
          id: '100945',
          projectId: 'string',
          num: '100945',
          name: '系统示例',
          status: 'COMPLETED',
          tags: ['string'],
          schedule: 'string',
          createUser: 'string',
          createTime: 'string',
          moduleName: 'string',
          moduleId: 'string',
          testPlanItem: [],
          testPlanGroupId: 'string',
          passCount: 0,
          unPassCount: 0,
          reviewedCount: 0,
          underReviewedCount: 0,
        },
      ],
      testPlanGroupId: 'string',
    },
  ];
  const { propsRes, propsEvent, loadList, setLoadListParams, resetSelector, setKeyword, setAdvanceFilter, setProps } =
    useTable(
      undefined,
      {
        tableKey: TableKeyEnum.TEST_PLAN_ALL_TABLE,
        scroll: { x: scrollWidth.value },
        selectable: true,
        showSetting: true,
        heightUsed: 374,
        enableDrag: true,
      },
      (item) => {
        return {
          ...item,
          tags: (item.tags || []).map((e: string) => ({ id: e, name: e })),
        };
      }
    );

  const showType = ref<string>('list');
  const batchParams = ref<BatchActionQueryParams>({
    selectedIds: [],
    selectAll: false,
    excludeIds: [],
    currentSelectCount: 0,
  });

  function handleTableBatch(event: BatchActionParams, params: BatchActionQueryParams) {
    batchParams.value = params;
  }

  function deletePlan(record: any) {}

  function handleMoreActionSelect(item: ActionsItem, record: any) {
    if (item.eventTag === 'delete') {
      deletePlan(record);
    }
  }

  tableStore.initColumn(TableKeyEnum.TEST_PLAN_ALL_TABLE, columns, 'drawer');

  const expandedKeys = ref<string[]>([]);

  function expandHandler(record: any) {
    if (expandedKeys.value.includes(record.id)) {
      expandedKeys.value = expandedKeys.value.filter((key) => key !== record.id);
    } else {
      expandedKeys.value = [...expandedKeys.value, record.id];
    }
  }

  function getIconClass(record: any) {
    return expandedKeys.value.includes(record.id) ? 'text-[rgb(var(--primary-5))]' : 'text-[var(--color-text-4)]';
  }

  function searchPlan() {}

  function handleFilterHidden(val: boolean) {
    if (!val) {
      searchPlan();
    }
  }

  onMounted(() => {
    setProps({ data });
  });
</script>

<style scoped lang="less">
  :deep(.arco-table-cell-expand-icon .arco-table-cell-inline-icon) {
    display: none;
  }
  :deep(.arco-table-cell-align-left) > span:first-child {
    padding-left: 0 !important;
  }
  .arrowIcon {
    transform: scaleX(-1);
  }
</style>
