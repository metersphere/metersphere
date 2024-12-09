<template>
  <div class="p-[16px]">
    <MsAdvanceFilter
      ref="msAdvanceFilterRef"
      v-model:keyword="keyword"
      :view-type="ViewTypeEnum.PLAN_BUG"
      :filter-config-list="filterConfigList"
      :custom-fields-config-list="searchCustomFields"
      :search-placeholder="t('common.searchByIdName')"
      @keyword-search="getFetch()"
      @adv-search="handleAdvSearch"
      @refresh="getFetch()"
    >
      <template #left>
        <div
          >{{ t('testPlan.bugManagement.bug') }}
          <span class="text-[var(--color-text-4)]">({{ addCommasToNumber(count) }})</span>
        </div>
      </template>
    </MsAdvanceFilter>
    <MsBaseTable
      ref="tableRef"
      class="mt-[16px]"
      :not-show-table-filter="isAdvancedSearchMode"
      v-bind="propsRes"
      v-on="propsEvent"
    >
      <template v-if="props.canEdit" #num="{ record }">
        <MsButton type="text" @click="handleShowDetail(record.id)">{{ record.num }}</MsButton>
      </template>
      <template #name="{ record }">
        <BugNamePopover :name="record.title" :content="record.content" />
      </template>
      <template #linkCase="{ record }">
        <CaseCountPopover :bug-item="record" />
      </template>
    </MsBaseTable>
  </div>
  <BugDetailDrawer
    v-model:visible="detailVisible"
    :detail-id="activeDetailId"
    detail-default-tab="detail"
    :current-platform="currentPlatform"
    @submit="refresh"
  />
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { useRoute } from 'vue-router';

  import { getFilterCustomFields, MsAdvanceFilter } from '@/components/pure/ms-advance-filter';
  import { FilterFormItem, FilterResult } from '@/components/pure/ms-advance-filter/type';
  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import CaseCountPopover from './caseCountPopover.vue';
  import BugDetailDrawer from '@/views/bug-management/components/bug-detail-drawer.vue';
  import BugNamePopover from '@/views/case-management/caseManagementFeature/components/tabContent/tabBug/bugNamePopover.vue';

  import { getCustomFieldHeader, getCustomOptionHeader, getPlatform } from '@/api/modules/bug-management';
  import { getPlatformOptions } from '@/api/modules/project-management/menuManagement';
  import { planDetailBugPage } from '@/api/modules/test-plan/testPlan';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';
  import { addCommasToNumber } from '@/utils';
  import { hasAnyPermission } from '@/utils/permission';

  import { BugEditCustomField, BugOptionItem } from '@/models/bug-management';
  import { PoolOption } from '@/models/projectManagement/menuManagement';
  import { FilterType, ViewTypeEnum } from '@/enums/advancedFilterEnum';
  import { MenuEnum } from '@/enums/commonEnum';
  import { TableKeyEnum } from '@/enums/tableEnum';

  import { makeColumns } from '@/views/case-management/caseManagementFeature/components/utils';

  const props = defineProps<{
    canEdit: boolean;
  }>();
  const emit = defineEmits<{
    (e: 'refresh'): void;
  }>();

  const { t } = useI18n();
  const route = useRoute();
  const appStore = useAppStore();

  const keyword = ref<string>('');
  const planId = ref(route.query.id as string);

  const columns = ref<MsTableColumn>([
    {
      title: 'ID',
      dataIndex: 'num',
      slotName: 'num',
      sortIndex: 1,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      showTooltip: true,
      width: 100,
    },
    {
      title: 'testPlan.bugManagement.bugName',
      slotName: 'name',
      dataIndex: 'title',
      showInTable: true,
      width: 300,
      showDrag: false,
    },
    {
      title: 'caseManagement.featureCase.defectState',
      dataIndex: 'status',
      filterConfig: {
        options: [],
        labelKey: 'text',
      },
      showInTable: true,
      width: 150,
      showDrag: false,
    },
    {
      title: 'caseManagement.featureCase.linkCase',
      slotName: 'linkCase',
      dataIndex: 'linkCase',
      showInTable: true,
      width: 150,
    },
    {
      title: 'caseManagement.featureCase.updateUser',
      slotName: 'handleUser',
      dataIndex: 'handleUser',
      titleSlotName: 'handleUserFilter',
      showInTable: true,
      showTooltip: true,
      width: 300,
    },
    {
      title: 'common.createTime',
      slotName: 'createTime',
      dataIndex: 'createTime',
      showInTable: true,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      width: 200,
      showDrag: true,
    },
  ]);
  const { propsRes, propsEvent, viewId, advanceFilter, setAdvanceFilter, loadList, setLoadListParams } = useTable(
    planDetailBugPage,
    {
      columns: columns.value,
      tableKey: TableKeyEnum.TEST_PLAN_DETAIL_BUG_TABLE,
      scroll: { x: '100%' },
      showSelectorAll: false,
    }
  );

  const count = computed(() => {
    return propsRes.value.msPagination?.total || 0;
  });

  function getFetch() {
    setLoadListParams({
      planId: planId.value,
      keyword: keyword.value,
      projectId: appStore.currentProjectId,
      viewId: viewId.value,
      combineSearch: advanceFilter,
    });
    loadList();
  }

  const msAdvanceFilterRef = ref<InstanceType<typeof MsAdvanceFilter>>();
  const isAdvancedSearchMode = computed(() => msAdvanceFilterRef.value?.isAdvancedSearchMode);

  // 获取自定义字段
  const searchCustomFields = ref<FilterFormItem[]>([]);
  const getCustomFieldColumns = async () => {
    try {
      const res = await getCustomFieldHeader(appStore.currentProjectId);
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
    setAdvanceFilter(filter, id);
    getFetch(); // 基础筛选都清空
  };

  const tableRef = ref<InstanceType<typeof MsBaseTable>>();
  async function initFilterOptions() {
    if (hasAnyPermission(['PROJECT_BUG:READ'])) {
      const res = await getCustomOptionHeader(appStore.currentProjectId);
      statusOption.value = res.statusOption;
      handleUserOption.value = res.handleUserOption;
      const optionsMap: Record<string, any> = {
        status: res.statusOption,
      };
      columns.value = makeColumns(optionsMap, columns.value);
    }
    tableRef.value?.initColumn(columns.value);
  }

  const detailVisible = ref(false);
  const activeDetailId = ref<string>('');
  const currentPlatform = ref('Local');
  const handleShowDetail = (id: string) => {
    activeDetailId.value = id;
    detailVisible.value = true;
  };
  const setCurrentPlatform = async () => {
    const res = await getPlatform(appStore.currentProjectId);
    currentPlatform.value = res;
  };

  function refresh() {
    loadList();
    emit('refresh');
  }

  onBeforeMount(() => {
    initFilterOptions();
    getFetch();
    getCustomFieldColumns();
    initPlatformOption();
    setCurrentPlatform();
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
