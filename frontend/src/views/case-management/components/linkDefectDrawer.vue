<template>
  <MsDrawer
    v-model:visible="showDrawer"
    :mask="true"
    :title="t('caseManagement.featureCase.linkDefect')"
    :ok-text="t('caseManagement.featureCase.associated')"
    :ok-disabled="currentCaseTable.propsRes.value.selectedKeys.size === 0"
    :width="1200"
    :mask-closable="true"
    unmount-on-close
    :show-continue="false"
    :ok-loading="props.drawerLoading"
    @confirm="handleDrawerConfirm"
    @cancel="handleDrawerCancel"
  >
    <MsAdvanceFilter
      ref="msAdvanceFilterRef"
      v-model:keyword="keyword"
      :view-type="ViewTypeEnum.PLAN_BUG_DRAWER"
      :filter-config-list="filterConfigList"
      :custom-fields-config-list="searchCustomFields"
      :search-placeholder="t('caseManagement.featureCase.searchByNameAndId')"
      @keyword-search="getFetch()"
      @adv-search="handleAdvSearch"
      @refresh="searchList()"
    >
      <template #left>
        <div class="font-medium">{{ t('caseManagement.featureCase.defectList') }}</div>
      </template>
    </MsAdvanceFilter>
    <ms-base-table
      ref="tableRef"
      class="mt-[16px]"
      v-bind="currentCaseTable.propsRes.value"
      :action-config="{
        baseAction: [],
        moreAction: [],
      }"
      v-on="currentCaseTable.propsEvent.value"
    >
      <template #name="{ record }">
        <BugNamePopover :name="record.name || record.title" :content="record.content || record.description || ''" />
      </template>
      <template #createUserName="{ record }">
        <a-tooltip :content="`${record.createUserName}`" position="tl">
          <div class="one-line-text">{{ record.createUserName || '-' }}</div>
        </a-tooltip>
      </template>
    </ms-base-table>
  </MsDrawer>
</template>

<script setup lang="ts">
  import { ref } from 'vue';

  import { getFilterCustomFields, MsAdvanceFilter } from '@/components/pure/ms-advance-filter';
  import { FilterFormItem, FilterResult } from '@/components/pure/ms-advance-filter/type';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { MsTableColumn, MsTableProps } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import BugNamePopover from '@/views/case-management/caseManagementFeature/components/tabContent/tabBug/bugNamePopover.vue';

  import { getBugList, getCustomFieldHeader, getCustomOptionHeader } from '@/api/modules/bug-management';
  import { getDrawerDebugPage } from '@/api/modules/case-management/featureCase';
  import { getPlatformOptions } from '@/api/modules/project-management/menuManagement';
  import {
    getTestPlanApiBugPage,
    getTestPlanBugPage,
    getTestPlanScenarioBugPage,
  } from '@/api/modules/test-plan/testPlan';
  import { useI18n } from '@/hooks/useI18n';
  import { useAppStore } from '@/store';

  import { BugEditCustomField, BugListItem, BugOptionItem } from '@/models/bug-management';
  import { PoolOption } from '@/models/projectManagement/menuManagement';
  import { FilterType, ViewTypeEnum } from '@/enums/advancedFilterEnum';
  import { AssociatedBugApiTypeEnum } from '@/enums/associateBugEnum';
  import { MenuEnum } from '@/enums/commonEnum';
  import { TableKeyEnum } from '@/enums/tableEnum';

  import debounce from 'lodash-es/debounce';

  const { t } = useI18n();
  const appStore = useAppStore();

  const getModuleTreeApiMap: Record<string, any> = {
    [AssociatedBugApiTypeEnum.FUNCTIONAL_BUG_LIST]: getDrawerDebugPage, // 用例测试-功能用例-待关联缺陷列表
    [AssociatedBugApiTypeEnum.TEST_PLAN_BUG_LIST]: getTestPlanBugPage, // 测试计划-功能用例-待关联缺陷列表
    [AssociatedBugApiTypeEnum.BUG_TOTAL_LIST]: getBugList, // 总缺陷列表
    [AssociatedBugApiTypeEnum.API_BUG_LIST]: getTestPlanApiBugPage, // 接口用例-待关联缺陷列表
    [AssociatedBugApiTypeEnum.SCENARIO_BUG_LIST]: getTestPlanScenarioBugPage, // 接口用例-待关联缺陷列表
  };

  const currentProjectId = computed(() => appStore.currentProjectId);

  const props = withDefaults(
    defineProps<{
      loadApi: AssociatedBugApiTypeEnum; // 关联缺陷接口类型
      drawerLoading: boolean;
      visible: boolean;
      caseId?: string; // 用例id
      isBatch?: boolean;
      showSelectorAll?: boolean;
    }>(),
    {
      showSelectorAll: true,
    }
  );

  const emit = defineEmits(['update:visible', 'save']);
  const columns: MsTableColumn = [
    {
      title: 'caseManagement.featureCase.tableColumnID',
      dataIndex: 'num',
      width: 150,
      showInTable: true,
      showTooltip: true,
      showDrag: false,
    },
    {
      title: 'caseManagement.featureCase.defectName',
      slotName: 'name',
      dataIndex: 'name',
      width: 300,
      showDrag: false,
    },

    {
      title: 'caseManagement.featureCase.defectState',
      slotName: 'statusName',
      dataIndex: 'statusName',
      showInTable: true,
      showTooltip: true,
      width: 200,
      showDrag: false,
    },
    {
      title: 'caseManagement.featureCase.tableColumnTag',
      slotName: 'tags',
      dataIndex: 'tags',
      showInTable: true,
      isTag: true,
      width: 300,
      showDrag: true,
    },
    {
      title: 'caseManagement.featureCase.updateUser',
      slotName: 'handleUserName',
      dataIndex: 'handleUserName',
      showInTable: true,
      showTooltip: true,
      width: 200,
      showDrag: false,
    },
    {
      title: 'caseManagement.featureCase.tableColumnCreateUser',
      slotName: 'createUserName',
      dataIndex: 'createUser',
      showInTable: true,
      showTooltip: true,
      width: 200,
    },
    {
      title: 'caseManagement.featureCase.tableColumnCreateTime',
      slotName: 'createTime',
      dataIndex: 'createTime',
      showInTable: true,
      width: 200,
      showDrag: true,
    },
  ];

  const totalCaseProps = ref<Partial<MsTableProps<BugListItem>>>({
    scroll: { x: '100%' },
    columns,
    tableKey: TableKeyEnum.CASE_MANAGEMENT_TAB_DEFECT,
    selectable: true,
    showSelectorAll: props.showSelectorAll,
    heightUsed: 340,
  });

  const getTotalBugTable = useTable(
    getModuleTreeApiMap[AssociatedBugApiTypeEnum.BUG_TOTAL_LIST],
    totalCaseProps.value,
    (record) => {
      return {
        ...record,
        tags: (record.tags || []).map((item: string, i: number) => {
          return {
            id: `${record.id}-${i}`,
            name: item,
          };
        }),
      };
    }
  );

  const getSingleBugTable = useTable(getModuleTreeApiMap[props.loadApi], totalCaseProps.value, (record) => {
    return {
      ...record,
      tags: (record.tags || []).map((item: string, i: number) => {
        return {
          id: `${record.id}-${i}`,
          name: item,
        };
      }),
    };
  });

  watch(
    () => props.showSelectorAll,
    (val) => {
      totalCaseProps.value.showSelectorAll = val;
    },
    {
      immediate: true,
    }
  );

  const currentCaseTable = computed(() => {
    return props.isBatch ? getTotalBugTable : getSingleBugTable;
  });

  const keyword = ref<string>('');

  const showDrawer = computed({
    get() {
      return props.visible;
    },
    set(value) {
      emit('update:visible', value);
    },
  });

  function handleDrawerConfirm() {
    const { excludeKeys, selectedKeys, selectorStatus } = currentCaseTable.value.propsRes.value;
    const params = {
      excludeIds: [...excludeKeys],
      selectIds: selectorStatus === 'all' ? [] : [...selectedKeys],
      selectAll: selectorStatus === 'all',
      projectId: currentProjectId.value,
      keyword: keyword.value,
      searchMode: 'AND',
      combine: {},
      caseId: props.caseId || '',
      condition: { keyword: keyword.value },
    };
    showDrawer.value = false;
    emit('save', params);
  }

  function handleDrawerCancel() {
    currentCaseTable.value.resetSelector();
  }

  function getFetch() {
    currentCaseTable.value.setLoadListParams({
      keyword: keyword.value,
      projectId: currentProjectId.value,
      sourceId: props.caseId || '',
      viewId: currentCaseTable.value.viewId.value,
      combineSearch: currentCaseTable.value.advanceFilter,
    });
    currentCaseTable.value.loadList();
  }

  // 获取自定义字段
  const searchCustomFields = ref<FilterFormItem[]>([]);
  const getCustomFieldColumns = async () => {
    try {
      const res = await getCustomFieldHeader(currentProjectId.value);
      searchCustomFields.value = getFilterCustomFields(
        res.filter((item: BugEditCustomField) => item.fieldId !== 'title')
      );
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  };

  const statusOption = ref<BugOptionItem[]>([]);
  const handleUserOption = ref<BugOptionItem[]>([]);
  async function initFilterOptions() {
    try {
      const res = await getCustomOptionHeader(appStore.currentProjectId);
      statusOption.value = res.statusOption;
      handleUserOption.value = res.handleUserOption;
    } catch (error) {
      // eslint-disable-next-line no-console
    }
  }

  const platformOption = ref<PoolOption[]>([]);
  const initPlatformOption = async () => {
    try {
      const res = await getPlatformOptions(appStore.currentOrgId, MenuEnum.bugManagement);
      platformOption.value = [...res, { id: 'Local', name: 'Local' }];
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  };

  const filterConfigList = computed<FilterFormItem[]>(() => [
    {
      title: 'bugManagement.ID',
      dataIndex: 'num',
      type: FilterType.INPUT,
    },
    {
      title: 'bugManagement.bugName',
      dataIndex: 'title',
      type: FilterType.INPUT,
    },
    {
      title: 'bugManagement.belongPlatform',
      dataIndex: 'platform',
      type: FilterType.SELECT,
      selectProps: {
        multiple: true,
        labelKey: 'name',
        valueKey: 'name',
        options: platformOption.value,
      },
    },
    {
      title: 'caseManagement.featureCase.defectState',
      dataIndex: 'status',
      type: FilterType.SELECT,
      selectProps: {
        multiple: true,
        labelKey: 'text',
        options: statusOption.value,
      },
    },
    {
      title: 'bugManagement.numberOfCase',
      dataIndex: 'relationCaseCount',
      type: FilterType.NUMBER,
      numberProps: {
        min: 0,
        precision: 0,
      },
    },
    {
      title: 'bugManagement.handleMan',
      dataIndex: 'handleUser',
      type: FilterType.SELECT,
      selectProps: {
        multiple: true,
        labelKey: 'text',
        options: handleUserOption.value,
      },
    },
    {
      title: 'common.tag',
      dataIndex: 'tags',
      type: FilterType.TAGS_INPUT,
      numberProps: {
        min: 0,
        precision: 0,
      },
    },
    {
      title: 'common.creator',
      dataIndex: 'createUser',
      type: FilterType.MEMBER,
    },
    {
      title: 'common.createTime',
      dataIndex: 'createTime',
      type: FilterType.DATE_PICKER,
    },
    {
      title: 'apiScenario.table.columns.updateUser',
      dataIndex: 'updateUser',
      type: FilterType.MEMBER,
    },
    {
      title: 'common.updateTime',
      dataIndex: 'updateTime',
      type: FilterType.DATE_PICKER,
    },
  ]);
  // 高级检索
  const handleAdvSearch = (filter: FilterResult, id: string) => {
    keyword.value = '';
    currentCaseTable.value.setAdvanceFilter(filter, id);
    getFetch(); // 基础筛选都清空
  };

  const searchList = debounce(() => {
    getFetch();
  }, 100);

  watch(
    () => props.visible,
    (val) => {
      if (val) {
        keyword.value = '';
        currentCaseTable.value.resetSelector();
        getFetch();
      }
    }
  );

  onBeforeMount(() => {
    initFilterOptions();
    getCustomFieldColumns();
    initPlatformOption();
  });
</script>

<style lang="less">
  .bug-content-popover {
    .arco-popover-content {
      overflow: auto;
      max-height: 400px;
      .ms-scroll-bar();
    }
  }
</style>
