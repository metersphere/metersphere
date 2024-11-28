<template>
  <MsCard auto-height simple>
    <div class="flex items-center justify-between">
      <div
        class="font-medium text-[var(--color-text-1)]"
        :class="props.type !== 'my_todo' ? 'cursor-pointer' : ''"
        @click="goBugList"
      >
        {{ t('ms.workbench.myFollowed.feature.BUG') }}
      </div>
    </div>
    <MsBaseTable ref="tableRef" class="mt-[16px]" v-bind="propsRes" v-on="propsEvent">
      <!-- ID -->
      <template #num="{ record }">
        <a-button type="text" class="px-0 text-[14px] leading-[22px]" @click="handleShowDetail(record.id)">
          {{ record.num }}
        </a-button>
      </template>
      <template #relationCaseCount="{ record, rowIndex }">
        <a-button type="text" class="px-0" @click="showDetail(record.id, rowIndex, 'case')">
          {{ record.relationCaseCount }}
        </a-button>
      </template>
      <template #statusName="{ record }">
        {{ record.statusName || '-' }}
      </template>
      <template #handleUserTitle>
        <div class="flex items-center text-[var(--color-text-3)]">
          {{ t('bugManagement.handleMan') }}
          <a-tooltip :content="t('bugManagement.handleManTips')" position="right">
            <icon-question-circle
              class="ml-[4px] text-[var(--color-text-4)] hover:text-[rgb(var(--primary-5))]"
              size="16"
            />
          </a-tooltip>
        </div>
      </template>
    </MsBaseTable>
  </MsCard>
</template>

<script setup lang="ts">
  import { TableData } from '@arco-design/web-vue';
  import { cloneDeep } from 'lodash-es';

  import MsCard from '@/components/pure/ms-card/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';

  import {
    getCustomFieldHeader,
    getCustomOptionHeader,
    workbenchBugList,
    workbenchTodoBugList,
  } from '@/api/modules/workbench';
  import { useI18n } from '@/hooks/useI18n';
  import useOpenNewPage from '@/hooks/useOpenNewPage';
  import useAppStore from '@/store/modules/app';
  import { customFieldDataToTableData, customFieldToColumns } from '@/utils';

  import { BugEditCustomField, BugOptionItem } from '@/models/bug-management';
  import { BugManagementRouteEnum } from '@/enums/routeEnum';

  import { makeColumns } from '@/views/case-management/caseManagementFeature/components/utils';

  const props = defineProps<{
    project: string;
    type: 'my_follow' | 'my_create' | 'my_todo';
    refreshId: string;
  }>();

  const appStore = useAppStore();
  const { t } = useI18n();
  const { openNewPage } = useOpenNewPage();

  const tableRef = ref<InstanceType<typeof MsBaseTable>>();
  const originColumns: MsTableColumn = [
    {
      title: 'bugManagement.ID',
      dataIndex: 'num',
      slotName: 'num',
      width: 100,
      fixed: 'left',
      showInTable: true,
      columnSelectorDisabled: true,
    },
    {
      title: 'bugManagement.bugName',
      dataIndex: 'title',
      width: 180,
      fixed: 'left',
      showTooltip: true,
      showInTable: true,
    },
    {
      title: 'bugManagement.status',
      dataIndex: 'status',
      width: 100,
      showTooltip: false,
      slotName: 'statusName',
      filterConfig: {
        options: [],
        labelKey: 'text',
      },
      showInTable: true,
    },
    {
      title: 'bugManagement.handleMan',
      dataIndex: 'handleUser',
      slotName: 'handleUser',
      titleSlotName: 'handleUserTitle',
      showTooltip: true,
      width: 125,
      showDrag: true,
      showInTable: true,
    },
    {
      title: 'bugManagement.creator',
      dataIndex: 'createUser',
      slotName: 'createUser',
      width: 125,
      showTooltip: true,
      showDrag: true,
      filterConfig: {
        options: [],
        labelKey: 'text',
      },
      showInTable: true,
    },
    {
      title: 'bugManagement.createTime',
      dataIndex: 'createTime',
      showDrag: true,
      width: 199,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      showInTable: true,
    },
    {
      title: 'bugManagement.updateUser',
      dataIndex: 'updateUser',
      width: 125,
      showTooltip: true,
      showDrag: true,
      filterConfig: {
        options: [],
        labelKey: 'text',
      },
      showInTable: true,
    },
    {
      title: 'bugManagement.updateTime',
      dataIndex: 'updateTime',
      showDrag: true,
      width: 199,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      showInTable: true,
    },
  ];
  let columns = cloneDeep(originColumns);

  const statusOption = ref<BugOptionItem[]>([]);
  async function initFilterOptions() {
    const res = await getCustomOptionHeader(props.project);
    statusOption.value = res.statusOption;
    const filterOptionsMaps: Record<string, any> = {
      status: res.statusOption,
      createUser: res.userOption,
      updateUser: res.userOption,
    };

    columns = makeColumns(filterOptionsMaps, columns);
  }

  // 自定义字段
  const customFields = ref<BugEditCustomField[]>([]);
  // 获取自定义字段
  const getCustomFieldColumns = async () => {
    const res = await getCustomFieldHeader(props.project);
    customFields.value = res;
    return customFieldToColumns(res);
  };

  let customColumns: MsTableColumn = [];
  async function getColumnHeaders() {
    try {
      const res = await getCustomFieldColumns();
      customColumns = res.filter((item) => {
        // 严重程度
        if ((item.title === '严重程度' || item.title === 'Bug Degree') && item.internal) {
          item.showInTable = true;
          item.slotName = 'severity';
          item.filterConfig = {
            options: item.options || [],
            labelKey: 'text',
          };
        }
        return (item.title === '严重程度' || item.title === 'Bug Degree') && item.internal;
      });
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  try {
    appStore.showLoading();
    await getColumnHeaders();
    columns.splice(2, 0, ...customColumns);
    await initFilterOptions();
  } catch (error) {
    // eslint-disable-next-line no-console
    console.log(error);
  } finally {
    appStore.hideLoading();
  }

  const workbenchBugPage = computed(() => {
    return props.type === 'my_todo' ? workbenchTodoBugList : workbenchBugList;
  });

  const { propsRes, propsEvent, setLoadListParams, setLoading, loadList } = useTable(
    workbenchBugPage.value,
    {
      columns,
      scroll: { x: '100%' },
      selectable: false,
      noDisable: false,
      showSetting: false,
      paginationSize: 'mini',
    },
    (record: TableData) => ({
      ...record,
      createUser: record.createUserName,
      handleUser: record.handleUserName,
      updateUser: record.updateUserName,
      ...customFieldDataToTableData(record.customFields, customFields.value),
    })
  );

  const detailVisible = ref(false);
  const activeDetailId = ref<string>('');
  const activeCaseIndex = ref<number>(0);
  const activeDetailTab = ref<string>('');

  const showDetail = (id: string, rowIndex: number, tab: string) => {
    activeDetailId.value = id;
    activeCaseIndex.value = rowIndex;
    activeDetailTab.value = tab;
    detailVisible.value = true;
  };

  function handleShowDetail(id: number) {
    openNewPage(BugManagementRouteEnum.BUG_MANAGEMENT_INDEX, { id, pId: props.project });
  }

  function goBugList() {
    if (props.type === 'my_todo') {
      return;
    }
    openNewPage(BugManagementRouteEnum.BUG_MANAGEMENT_INDEX, {
      view: props.type,
      pId: props.project,
    });
  }

  async function init() {
    setLoadListParams({
      projectId: props.project,
      viewId: props.type,
      myTodo: props.type === 'my_todo',
    });
    loadList();
  }

  async function refresh() {
    try {
      setLoading(true);
      columns = cloneDeep(originColumns);
      await getColumnHeaders();
      columns.splice(2, 0, ...customColumns);
      await initFilterOptions();
      tableRef.value?.initColumn(columns);
      init();
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  watch(
    () => props.refreshId,
    () => {
      refresh();
    }
  );

  onMounted(() => {
    init();
  });
</script>

<style lang="less" scoped></style>
