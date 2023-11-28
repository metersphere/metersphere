<template>
  <div class="px-[24px] py-[16px]">
    <div class="mb-[16px] flex items-center justify-between">
      <div class="flex items-center">
        <a-button type="primary" class="mr-[12px]" @click="associateDrawerVisible = true">
          {{ t('ms.case.associate.title') }}
        </a-button>
        <a-button type="outline" @click="createCase">{{ t('caseManagement.caseReview.createCase') }}</a-button>
      </div>
      <div class="flex w-[70%] items-center justify-end gap-[8px]">
        <a-input-search
          v-model="keyword"
          :placeholder="t('caseManagement.caseReview.searchPlaceholder')"
          allow-clear
          class="w-[200px]"
          @press-enter="searchReview"
          @search="searchReview"
        />
        <a-button type="outline" class="arco-btn-outline--secondary px-[8px]">
          <MsIcon type="icon-icon-filter" class="mr-[4px] text-[var(--color-text-4)]" />
          <div class="text-[var(--color-text-4)]">{{ t('common.filter') }}</div>
        </a-button>
        <a-radio-group v-model:model-value="showType" type="button" class="case-show-type">
          <a-radio value="list" class="show-type-icon p-[2px]"><MsIcon type="icon-icon_view-list_outlined" /></a-radio>
          <a-radio value="mind" class="show-type-icon p-[2px]"><MsIcon type="icon-icon_mindnote_outlined" /></a-radio>
        </a-radio-group>
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
      <template #resultColumn>
        <div class="flex items-center text-[var(--color-text-3)]">
          {{ t('caseManagement.caseReview.reviewResult') }}
          <a-tooltip :content="t('caseManagement.caseReview.reviewResultTip')" position="right">
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
      <template #result="{ record }">
        <div class="flex items-center gap-[4px]">
          <MsIcon
            :type="resultMap[record.result as ResultMap].icon"
            :style="{
              color: resultMap[record.result as ResultMap].color
            }"
          />
          {{ t(resultMap[record.result as ResultMap].label) }}
        </div>
      </template>
      <template #action="{ record }">
        <MsButton type="text" class="!mr-0" @click="review(record)">
          {{ t('caseManagement.caseReview.review') }}
        </MsButton>
        <a-divider direction="vertical" :margin="8"></a-divider>
        <MsPopconfirm
          :title="t('caseManagement.caseReview.disassociateTip')"
          :sub-title-tip="t('caseManagement.caseReview.disassociateTipContent')"
          :ok-text="t('common.confirm')"
          type="error"
        >
          <MsButton type="text" class="!mr-0">
            {{ t('caseManagement.caseReview.disassociate') }}
          </MsButton>
        </MsPopconfirm>
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
      @close="handleDialogCancel"
    >
      <template #title>
        <div class="flex items-center justify-start">
          <div class="text-[var(--color-text-1)]">
            {{ dialogTitle }}
          </div>
          <a-tooltip
            v-if="dialogShowType === 'review'"
            :content="t('caseManagement.caseReview.batchReviewTip')"
            position="right"
          >
            <icon-question-circle
              class="ml-[4px] text-[var(--color-text-4)] hover:text-[rgb(var(--primary-5))]"
              size="16"
            />
          </a-tooltip>
          <div class="ml-[8px] font-normal text-[var(--color-text-4)]">
            ({{ t('caseManagement.caseReview.selectedCase', { count: tableSelected.length }) }})
          </div>
        </div>
      </template>
      <a-form ref="dialogFormRef" :model="dialogForm" layout="vertical">
        <a-form-item
          v-if="dialogShowType === 'review'"
          field="reason"
          :label="t('caseManagement.caseReview.reviewResult')"
          class="mb-[16px]"
        >
          <a-radio-group v-model:model-value="dialogForm.result" @change="() => dialogFormRef?.resetFields()">
            <a-radio value="pass">
              <div class="inline-flex items-center">
                <MsIcon type="icon-icon_succeed_filled" class="mr-[4px] text-[rgb(var(--success-6))]" />
                {{ t('caseManagement.caseReview.pass') }}
              </div>
            </a-radio>
            <a-radio value="fail">
              <div class="inline-flex items-center">
                <MsIcon type="icon-icon_close_filled" class="mr-[4px] text-[rgb(var(--danger-6))]" />
                {{ t('caseManagement.caseReview.fail') }}
              </div>
            </a-radio>
          </a-radio-group>
        </a-form-item>
        <a-form-item
          v-if="dialogShowType === 'review' || dialogShowType === 'reReview'"
          field="reason"
          :label="t('caseManagement.caseReview.reason')"
          :rules="
            dialogForm.result === 'fail' && dialogShowType !== 'reReview'
              ? [{ required: true, message: t('caseManagement.caseReview.reasonRequired') }]
              : []
          "
          asterisk-position="end"
          class="mb-0"
        >
          <a-input
            v-model:model-value="dialogForm.reason"
            :placeholder="t('caseManagement.caseReview.reasonPlaceholder')"
          />
        </a-form-item>
        <a-form-item
          v-if="dialogShowType === 'changeReviewer'"
          field="reviewer"
          :label="t('caseManagement.caseReview.chooseReviewer')"
          :rules="[{ required: true, message: t('caseManagement.caseReview.reviewerRequired') }]"
          asterisk-position="end"
          class="mb-0"
        >
          <MsSelect
            v-model:modelValue="dialogForm.reviewer"
            mode="static"
            :placeholder="t('caseManagement.caseReview.reviewerPlaceholder')"
            :options="reviewersOptions"
            :search-keys="['label']"
            allow-search
            multiple
          />
        </a-form-item>
      </a-form>
      <template #footer>
        <div class="flex items-center justify-end">
          <div v-if="dialogShowType === 'changeReviewer'" class="mr-auto flex items-center">
            <a-switch v-model:model-value="dialogForm.isAppend" size="small" class="mr-[4px]"></a-switch>
            {{ t('caseManagement.caseReview.append') }}
            <a-tooltip :content="t('caseManagement.caseReview.reviewResultTip')" position="right">
              <template #content>
                <div>{{ t('caseManagement.caseReview.appendTip1') }}</div>
                <div>{{ t('caseManagement.caseReview.appendTip2') }}</div>
              </template>
              <icon-question-circle
                class="ml-[4px] text-[var(--color-text-4)] hover:text-[rgb(var(--primary-5))]"
                size="16"
              />
            </a-tooltip>
          </div>
          <a-button type="secondary" @click="handleDialogCancel">{{ t('common.cancel') }}</a-button>
          <a-button v-if="dialogShowType === 'review'" type="primary" class="ml-[12px]" @click="commitResult">
            {{ t('caseManagement.caseReview.commitResult') }}
          </a-button>
          <a-button v-if="dialogShowType === 'changeReviewer'" type="primary" class="ml-[12px]" @click="changeReviewer">
            {{ t('common.update') }}
          </a-button>
          <a-button v-if="dialogShowType === 'reReview'" type="primary" class="ml-[12px]" @click="reReview">
            {{ t('caseManagement.caseReview.reReview') }}
          </a-button>
        </div>
      </template>
    </a-modal>
    <AssociateDrawer
      v-model:visible="associateDrawerVisible"
      v-model:project="associateDrawerProject"
      @success="writeAssociateCases"
    />
  </div>
</template>

<script setup lang="ts">
  import { useRoute, useRouter } from 'vue-router';
  import { FormInstance, Message } from '@arco-design/web-vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsPopconfirm from '@/components/pure/ms-popconfirm/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { BatchActionParams, BatchActionQueryParams, MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsSelect from '@/components/business/ms-select';
  import AssociateDrawer from '../create/associateDrawer.vue';

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
  const route = useRoute();

  const { t } = useI18n();
  const { openModal } = useModal();

  const keyword = ref('');
  const showType = ref<'list' | 'mind'>('list');

  type ResultMap = 0 | 1 | 2 | 3 | 4;
  const resultMap = {
    0: {
      label: 'caseManagement.caseReview.unReview',
      color: 'var(--color-text-input-border)',
      icon: 'icon-icon_block_filled',
    },
    1: {
      label: 'caseManagement.caseReview.reviewing',
      color: 'rgb(var(--link-6))',
      icon: 'icon-icon_testing',
    },
    2: {
      label: 'caseManagement.caseReview.reviewPass',
      color: 'rgb(var(--success-6))',
      icon: 'icon-icon_succeed_filled',
    },
    3: {
      label: 'caseManagement.caseReview.fail',
      color: 'rgb(var(--danger-6))',
      icon: 'icon-icon_close_filled',
    },
    4: {
      label: 'caseManagement.caseReview.reReview',
      color: 'rgb(var(--warning-6))',
      icon: 'icon-icon_resubmit_filled',
    },
  } as const;

  const columns: MsTableColumn = [
    {
      title: 'ID',
      dataIndex: 'id',
      sortIndex: 1,
      showTooltip: true,
      width: 100,
    },
    {
      title: 'caseManagement.caseReview.caseName',
      slotName: 'name',
      dataIndex: 'name',
      sortable: {
        sortDirections: ['ascend', 'descend'],
      },
      width: 200,
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
      title: 'caseManagement.caseReview.reviewResult',
      dataIndex: 'result',
      slotName: 'result',
      titleSlotName: 'resultColumn',
      width: 110,
    },
    {
      title: 'caseManagement.caseReview.version',
      dataIndex: 'version',
      width: 90,
    },
    {
      title: 'caseManagement.caseReview.creator',
      dataIndex: 'creator',
      width: 150,
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
  tableStore.initColumn(TableKeyEnum.CASE_MANAGEMENT_REVIEW_CASE, columns, 'drawer');
  const { propsRes, propsEvent, loadList, setLoadListParams, resetSelector } = useTable(getCaseList, {
    scroll: { x: '100%' },
    tableKey: TableKeyEnum.CASE_MANAGEMENT_REVIEW_CASE,
    showSetting: true,
    selectable: true,
    showSelectAll: true,
    draggable: { type: 'handle', width: 32 },
  });
  const batchActions = {
    baseAction: [
      {
        label: 'caseManagement.caseReview.review',
        eventTag: 'review',
      },
      {
        label: 'caseManagement.caseReview.changeReviewer',
        eventTag: 'changeReviewer',
      },
      {
        label: 'caseManagement.caseReview.disassociate',
        eventTag: 'disassociate',
      },
      {
        label: 'caseManagement.caseReview.reReview',
        eventTag: 'reReview',
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
  const defaultDialogForm = {
    result: 'pass',
    reason: '',
    reviewer: [],
    isAppend: false,
  };
  const dialogForm = ref({ ...defaultDialogForm });
  const dialogFormRef = ref<FormInstance>();
  const dialogShowType = ref<'review' | 'changeReviewer' | 'reReview'>('review');
  const dialogTitle = computed(() => {
    switch (dialogShowType.value) {
      case 'review':
        return t('caseManagement.caseReview.batchReview');
      case 'changeReviewer':
        return t('caseManagement.caseReview.changeReviewer');
      case 'reReview':
        return t('caseManagement.caseReview.reReview');
      default:
        return '';
    }
  });
  const reviewersOptions = ref([
    {
      label: '张三',
      value: '1',
    },
    {
      label: '李四',
      value: '2',
    },
    {
      label: '王五',
      value: '3',
    },
  ]);

  function handleDialogCancel() {
    dialogVisible.value = false;
    dialogFormRef.value?.resetFields();
    dialogForm.value = { ...defaultDialogForm };
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

  const selectedModuleKeys = ref<(string | number)[]>([]);

  /**
   * 处理文件夹树节点选中事件
   */
  function folderNodeSelect(keys: (string | number)[]) {
    selectedModuleKeys.value = keys;
  }

  function disassociate() {
    openModal({
      type: 'warning',
      title: t('caseManagement.caseReview.disassociateConfirmTitle', { count: tableSelected.value.length }),
      content: t('caseManagement.caseReview.disassociateTipContent'),
      okText: t('caseManagement.caseReview.disassociate'),
      cancelText: t('common.cancel'),
      onBeforeOk: async () => {
        try {
          // await resetUserPassword({
          //   selectIds,
          //   selectAll: !!params?.selectAll,
          //   excludeIds: params?.excludeIds || [],
          //   condition: { keyword: keyword.value },
          // });
          Message.success(t('common.updateSuccess'));
          resetSelector();
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        }
      },
      hideCancel: false,
    });
  }

  function reReview() {
    try {
      Message.success(t('common.updateSuccess'));
      dialogVisible.value = false;
      resetSelector();
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  function changeReviewer() {
    dialogFormRef.value?.validate(async (errors) => {
      if (!errors) {
        try {
          Message.success(t('common.updateSuccess'));
          dialogVisible.value = false;
          resetSelector();
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        }
      }
    });
  }

  function commitResult() {
    dialogFormRef.value?.validate(async (errors) => {
      if (!errors) {
        try {
          Message.success(t('common.updateSuccess'));
          dialogVisible.value = false;
          resetSelector();
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        }
      }
    });
  }

  /**
   * 处理表格选中后批量操作
   * @param event 批量操作事件对象
   */
  function handleTableBatch(event: BatchActionParams, params: BatchActionQueryParams) {
    tableSelected.value = params?.selectedIds || [];
    batchParams.value = params;
    switch (event.eventTag) {
      case 'review':
        dialogVisible.value = true;
        dialogShowType.value = 'review';
        break;
      case 'changeReviewer':
        dialogVisible.value = true;
        dialogShowType.value = 'changeReviewer';
        break;
      case 'disassociate':
        disassociate();
        break;
      case 'reReview':
        dialogVisible.value = true;
        dialogShowType.value = 'reReview';
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
      name: CaseManagementRouteEnum.CASE_MANAGEMENT_REVIEW_DETAIL_CASE_DETAIL,
      query: {
        ...route.query,
        caseId: id,
      },
    });
  }

  function review(record: any) {
    console.log('review');
  }

  function createCase() {
    router.push({
      name: CaseManagementRouteEnum.CASE_MANAGEMENT_REVIEW_CREATE,
      query: {
        reviewId: route.query.id,
      },
    });
  }

  const associateDrawerVisible = ref(false);
  const associateDrawerProject = ref('');

  function writeAssociateCases(ids: string[]) {
    console.log('writeAssociateCases', ids);
  }
</script>

<style lang="less" scoped>
  .case-show-type {
    @apply grid grid-cols-2;
    .show-type-icon {
      :deep(.arco-radio-button-content) {
        @apply flex;

        padding: 4px;
        line-height: 20px;
      }
    }
  }
  :deep(.arco-radio-label) {
    @apply inline-flex;
  }
</style>
