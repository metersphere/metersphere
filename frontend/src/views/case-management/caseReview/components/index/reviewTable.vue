<template>
  <div class="px-[24px] py-[16px]">
    <div class="mb-[16px] flex items-center justify-between">
      <div class="flex items-center">
        <div class="mr-[4px] text-[var(--color-text-1)]">全部评审</div>
        <div class="text-[var(--color-text-4)]">(2)</div>
      </div>
      <div class="flex items-center gap-[8px]">
        <a-input-search
          v-model="keyword"
          :placeholder="t('caseManagement.caseReview.searchPlaceholder')"
          allow-clear
          @press-enter="searchReview"
          @search="searchReview"
        />
        <a-button type="outline" class="arco-btn-outline--secondary px-[8px]">
          <MsIcon type="icon-icon-filter" class="mr-[4px] text-[var(--color-text-4)]" />
          <div class="text-[var(--color-text-4)]">{{ t('common.filter') }}</div>
        </a-button>
        <a-button type="outline" class="arco-btn-outline--secondary p-[10px]">
          <icon-refresh class="text-[var(--color-text-4)]" />
        </a-button>
      </div>
    </div>
    <ms-base-table
      v-bind="propsRes"
      :action-config="batchActions"
      no-disable
      filter-icon-align-left
      v-on="propsEvent"
      @selected-change="handleTableSelect"
      @batch-action="handleTableBatch"
    >
      <!-- <template #status-filter>
        <a-checkbox-group>
          <a-checkbox :value="0">
            <a-tag :color="statusMap[0].color" :class="statusMap[0].class">
              {{ t(statusMap[0].label) }}
            </a-tag>
          </a-checkbox>
          <a-checkbox :value="1">
            <a-tag :color="statusMap[1].color" :class="statusMap[1].class">
              {{ t(statusMap[1].label) }}
            </a-tag>
          </a-checkbox>
          <a-checkbox :value="2">
            <a-tag :color="statusMap[2].color" :class="statusMap[2].class">
              {{ t(statusMap[2].label) }}
            </a-tag>
          </a-checkbox>
          <a-checkbox :value="3">
            <a-tag :color="statusMap[3].color" :class="statusMap[3].class">
              {{ t(statusMap[3].label) }}
            </a-tag>
          </a-checkbox>
        </a-checkbox-group>
      </template> -->
      <template #passRateColumn>
        <div class="flex items-center text-[var(--color-text-3)]">
          {{ t('caseManagement.caseReview.passRate') }}
          <a-tooltip :content="t('caseManagement.caseReview.passRateTip')" position="right">
            <icon-question-circle
              class="ml-[4px] text-[var(--color-text-4)] hover:text-[rgb(var(--primary-5))]"
              size="16"
            />
          </a-tooltip>
        </div>
      </template>
      <template #name="{ record }">
        <a-tooltip :content="record.name">
          <a-button type="text" class="px-0" @click="openDetail(record.id)">
            <div class="one-line-text max-w-[168px]">{{ record.name }}</div>
          </a-button>
        </a-tooltip>
      </template>
      <template #status="{ record }">
        <statusTag :status="record.status" />
      </template>
      <template #passRate="{ record }">
        <div class="mr-[8px] w-[100px]">
          <passRateLine :review-detail="record" height="5px" />
        </div>
        <div class="text-[var(--color-text-1)]">
          {{ `${(((record.passCount + record.failCount) / record.caseCount) * 100).toFixed(2)}%` }}
        </div>
      </template>
      <template #action="{ record }">
        <MsButton type="text" class="!mr-0">
          {{ t('common.edit') }}
        </MsButton>
        <a-divider direction="vertical" :margin="8"></a-divider>
        <MsButton type="text" class="!mr-0">
          {{ t('common.export') }}
        </MsButton>
        <a-divider direction="vertical" :margin="8"></a-divider>
        <MsTableMoreAction :list="getMoreAction(record.status)" @select="handleMoreActionSelect($event, record)" />
      </template>
      <template v-if="keyword.trim() === ''" #empty>
        <div class="flex items-center justify-center p-[8px] text-[var(--color-text-4)]">
          {{ t('caseManagement.caseReview.tableNoData') }}
          <MsButton class="ml-[8px]" @click="handleAddClick">
            {{ t('caseManagement.caseReview.create') }}
          </MsButton>
        </div>
      </template>
    </ms-base-table>
    <a-modal
      v-model:visible="dialogVisible"
      :on-before-ok="handleDeleteConfirm"
      class="p-[4px]"
      title-align="start"
      body-class="p-0"
      :mask-closable="false"
    >
      <template #title>
        <div class="flex items-center justify-start">
          <icon-exclamation-circle-fill size="20" class="mr-[8px] text-[rgb(var(--danger-6))]" />
          <div class="text-[var(--color-text-1)]">
            {{ t('caseManagement.caseReview.deleteReviewTitle', { name: activeRecord.name }) }}
          </div>
        </div>
      </template>
      <div v-if="activeRecord.status === 2" class="mb-[10px]">
        <div>{{ t('caseManagement.caseReview.deleteFinishedReviewContent1') }}</div>
        <div>{{ t('caseManagement.caseReview.deleteFinishedReviewContent2') }}</div>
      </div>
      <div v-else class="mb-[10px]">
        {{
          activeRecord.status === 1
            ? t('caseManagement.caseReview.deleteReviewingContent')
            : t('caseManagement.caseReview.deleteReviewContent', {
                status: t(statusMap[activeRecord.status as StatusMap].label),
              })
        }}
      </div>
      <a-input
        v-model:model-value="confirmReviewName"
        :placeholder="t('caseManagement.caseReview.deleteReviewPlaceholder')"
      />
      <template #footer>
        <div class="flex items-center justify-end">
          <a-button type="secondary" @click="handleDialogCancel">{{ t('common.cancel') }}</a-button>
          <a-button
            type="primary"
            status="danger"
            :disabled="confirmReviewName !== activeRecord.name"
            class="ml-[12px]"
            @click="handleDialogCancel"
          >
            {{ t('common.confirmDelete') }}
          </a-button>
          <a-button v-if="activeRecord.status === 2" type="primary" class="ml-[12px]" @click="handleDialogCancel">
            {{ t('caseManagement.caseReview.archive') }}
          </a-button>
        </div>
      </template>
    </a-modal>
    <a-modal
      v-model:visible="moveModalVisible"
      title-align="start"
      class="ms-modal-no-padding ms-modal-small"
      :mask-closable="false"
      :ok-text="t('caseManagement.caseReview.batchMoveConfirm', { count: tableSelected.length })"
      :ok-button-props="{ disabled: selectedModuleKeys.length === 0 }"
      :cancel-button-props="{ disabled: batchMoveFileLoading }"
      :on-before-ok="handleReviewMove"
      @close="handleMoveModalCancel"
    >
      <template #title>
        <div class="flex items-center">
          {{ t('caseManagement.caseReview.batchMove') }}
          <div class="ml-[4px] text-[var(--color-text-4)]">
            {{ t('caseManagement.caseReview.batchMoveTitleSub', { count: tableSelected.length }) }}
          </div>
        </div>
      </template>
      <ModuleTree
        v-if="moveModalVisible"
        v-model:selected-keys="selectedModuleKeys"
        :is-expand-all="true"
        :active-folder="props.activeFolder"
        is-modal
        @folder-node-select="folderNodeSelect"
      />
    </a-modal>
  </div>
</template>

<script setup lang="ts">
  import { useRouter } from 'vue-router';
  import { Message } from '@arco-design/web-vue';
  import dayjs from 'dayjs';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { BatchActionParams, BatchActionQueryParams, MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsTableMoreAction from '@/components/pure/ms-table-more-action/index.vue';
  import type { ActionsItem } from '@/components/pure/ms-table-more-action/types';
  import passRateLine from '../passRateLine.vue';
  import statusTag from '../statusTag.vue';
  import ModuleTree from './moduleTree.vue';

  import { getCaseList } from '@/api/modules/case-management/caseReview';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import useTableStore from '@/hooks/useTableStore';

  import { CaseManagementRouteEnum } from '@/enums/routeEnum';
  import { TableKeyEnum } from '@/enums/tableEnum';

  const props = defineProps<{
    activeFolder: string | number;
  }>();

  const router = useRouter();
  const { t } = useI18n();
  const { openModal } = useModal();

  const keyword = ref('');

  type StatusMap = 0 | 1 | 2 | 3;
  const statusMap = {
    0: {
      label: 'caseManagement.caseReview.unStart',
      color: 'var(--color-text-n8)',
      class: '!text-[var(--color-text-1)]',
    },
    1: {
      label: 'caseManagement.caseReview.going',
      color: 'rgb(var(--link-2))',
      class: '!text-[rgb(var(--link-6))]',
    },
    2: {
      label: 'caseManagement.caseReview.finished',
      color: 'rgb(var(--success-2))',
      class: '!text-[rgb(var(--success-6))]',
    },
    3: {
      label: 'caseManagement.caseReview.archived',
      color: 'var(--color-text-n8)',
      class: '!text-[var(--color-text-4)]',
    },
  } as const;

  const typeMap = {
    single: 'caseManagement.caseReview.single',
    multi: 'caseManagement.caseReview.multi',
  };

  const columns: MsTableColumn = [
    {
      title: 'ID',
      dataIndex: 'id',
      sortIndex: 1,
      showTooltip: true,
      width: 90,
    },
    {
      title: 'caseManagement.caseReview.name',
      slotName: 'name',
      dataIndex: 'name',
      sortable: {
        sortDirections: ['ascend', 'descend'],
      },
      width: 200,
    },
    {
      title: 'caseManagement.caseReview.caseCount',
      dataIndex: 'caseCount',
      width: 90,
    },
    {
      title: 'caseManagement.caseReview.status',
      dataIndex: 'status',
      slotName: 'status',
      width: 150,
    },
    {
      title: 'caseManagement.caseReview.passRate',
      slotName: 'passRate',
      titleSlotName: 'passRateColumn',
      width: 200,
    },
    {
      title: 'caseManagement.caseReview.type',
      dataIndex: 'type',
      width: 90,
    },
    {
      title: 'caseManagement.caseReview.reviewer',
      dataIndex: 'reviewer',
      showTooltip: true,
      sortable: {
        sortDirections: ['ascend', 'descend'],
      },
      width: 150,
    },
    {
      title: 'caseManagement.caseReview.creator',
      dataIndex: 'creator',
      width: 90,
    },
    {
      title: 'caseManagement.caseReview.module',
      dataIndex: 'module',
      width: 90,
    },
    {
      title: 'caseManagement.caseReview.tag',
      dataIndex: 'tags',
      isTag: true,
      width: 150,
    },
    {
      title: 'caseManagement.caseReview.desc',
      dataIndex: 'desc',
      width: 150,
      showTooltip: true,
    },
    {
      title: 'caseManagement.caseReview.cycle',
      dataIndex: 'cycle',
      width: 340,
    },
    {
      title: 'common.operation',
      slotName: 'action',
      dataIndex: 'operation',
      fixed: 'right',
      width: 140,
    },
  ];
  const tableStore = useTableStore();
  tableStore.initColumn(TableKeyEnum.CASE_MANAGEMENT_REVIEW, columns, 'drawer');
  const { propsRes, propsEvent, loadList, setLoadListParams, resetSelector } = useTable(
    getCaseList,
    {
      tableKey: TableKeyEnum.CASE_MANAGEMENT_REVIEW,
      showSetting: true,
      selectable: true,
      showSelectAll: true,
    },
    (item) => {
      return {
        ...item,
        type: t(typeMap[item.type as keyof typeof typeMap]),
        tags: item.tags?.map((e: string) => ({ id: e, name: e })) || [],
        cycle: `${dayjs(item.cycle[0]).format('YYYY-MM-DD HH:mm:ss')} - ${dayjs(item.cycle[1]).format(
          'YYYY-MM-DD HH:mm:ss'
        )}`,
      };
    }
  );
  const batchActions = {
    baseAction: [
      {
        label: 'caseManagement.caseReview.move',
        eventTag: 'move',
      },
    ],
  };

  function searchReview() {
    setLoadListParams({
      keyword: keyword.value,
    });
    loadList();
  }

  onBeforeMount(() => {
    loadList();
  });

  const tableSelected = ref<(string | number)[]>([]);
  const batchParams = ref<BatchActionQueryParams>({
    selectedIds: [],
    selectAll: false,
    excludeIds: [],
    currentSelectCount: 0,
  });

  /**
   * 处理表格选中
   */
  function handleTableSelect(arr: (string | number)[]) {
    tableSelected.value = arr;
  }

  const dialogVisible = ref<boolean>(false);
  const activeRecord = ref({
    id: '',
    name: '',
    status: 0,
  });
  const confirmReviewName = ref('');

  function handleDialogCancel() {
    dialogVisible.value = false;
  }

  /**
   * 拦截切换最新版确认
   * @param done 关闭弹窗
   */
  async function handleDeleteConfirm(done: (closed: boolean) => void) {
    try {
      // if (replaceVersion.value !== '') {
      //   await useLatestVersion(replaceVersion.value);
      // }
      // await toggleVersionStatus(activeRecord.value.id);
      // Message.success(t('caseManagement.caseReview.close', { name: activeRecord.value.name }));
      loadList();
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
      done(false);
    } finally {
      done(true);
    }
  }

  /**
   * 根据评审状态获取更多按钮列表
   * @param status 评审状态
   */
  function getMoreAction(status: number) {
    if (status === 2) {
      return [
        {
          label: 'caseManagement.caseReview.archive',
          eventTag: 'archive',
        },
        {
          isDivider: true,
        },
        {
          label: 'common.delete',
          eventTag: 'delete',
          danger: true,
        },
      ];
    }
    return [
      {
        label: 'common.delete',
        eventTag: 'delete',
        danger: true,
      },
    ];
  }

  function handleArchive(record: any) {
    openModal({
      type: 'warning',
      title: t('caseManagement.caseReview.archivedTitle', { name: record.name }),
      content: t('caseManagement.caseReview.archivedContent'),
      okText: t('caseManagement.caseReview.archive'),
      cancelText: t('common.cancel'),
      onBeforeOk: async () => {
        try {
          // await resetUserPassword({
          //   selectIds,
          //   selectAll: !!params?.selectAll,
          //   excludeIds: params?.excludeIds || [],
          //   condition: { keyword: keyword.value },
          // });
          Message.success(t('caseManagement.caseReview.archiveSuccess'));
          resetSelector();
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        }
      },
      hideCancel: false,
    });
  }

  const moveModalVisible = ref(false);
  const selectedModuleKeys = ref<(string | number)[]>([]);
  const batchMoveFileLoading = ref(false);

  async function handleReviewMove() {
    try {
      batchMoveFileLoading.value = true;
      // await batchMoveFile({
      //   selectIds: isBatchMove.value ? batchParams.value?.selectedIds || [] : [activeFile.value?.id || ''],
      //   selectAll: !!batchParams.value?.selectAll,
      //   excludeIds: batchParams.value?.excludeIds || [],
      //   condition: { keyword: keyword.value, combine: combine.value },
      //   projectId: appStore.currentProjectId,
      //   fileType: tableFileType.value,
      //   moduleIds: isMyOrAllFolder.value ? [] : [props.activeFolder],
      //   moveModuleId: selectedModuleKeys.value[0],
      // });
      Message.success(t('caseManagement.caseReview.batchMoveSuccess'));
      tableSelected.value = [];
      loadList();
      resetSelector();
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      batchMoveFileLoading.value = false;
    }
  }

  function handleMoveModalCancel() {
    moveModalVisible.value = false;
    selectedModuleKeys.value = [];
  }

  /**
   * 处理文件夹树节点选中事件
   */
  function folderNodeSelect(keys: (string | number)[]) {
    selectedModuleKeys.value = keys;
  }

  /**
   * 处理表格选中后批量操作
   * @param event 批量操作事件对象
   */
  function handleTableBatch(event: BatchActionParams, params: BatchActionQueryParams) {
    tableSelected.value = params?.selectedIds || [];
    batchParams.value = params;
    switch (event.eventTag) {
      case 'move':
        moveModalVisible.value = true;
        break;
      default:
        break;
    }
  }

  /**
   * 处理表格更多按钮事件
   * @param item
   */
  function handleMoreActionSelect(item: ActionsItem, record: any) {
    switch (item.eventTag) {
      case 'delete':
        activeRecord.value = record;
        confirmReviewName.value = '';
        dialogVisible.value = true;
        break;
      case 'archive':
        handleArchive(record);
        break;
      default:
        break;
    }
  }

  function handleAddClick() {
    console.log('handleAddClick');
  }

  function openDetail(id: string) {
    router.push({
      name: CaseManagementRouteEnum.CASE_MANAGEMENT_REVIEW_DETAIL,
      query: {
        id,
      },
    });
  }
</script>

<style lang="less" scoped></style>
