<template>
  <div class="p-[16px]">
    <div class="mb-[16px] flex items-center justify-between">
      <div
        >{{ t('testPlan.bugManagement.bug') }}
        <span class="text-[var(--color-text-4)]">({{ addCommasToNumber(count) }})</span>
      </div>
      <a-input-search
        v-model:model-value="keyword"
        :placeholder="t('common.searchByIdName')"
        allow-clear
        class="w-[240px]"
        @search="getFetch"
        @press-enter="getFetch"
        @clear="getFetch"
      />
    </div>
    <MsBaseTable ref="tableRef" v-bind="propsRes" v-on="propsEvent">
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

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import CaseCountPopover from './caseCountPopover.vue';
  import BugDetailDrawer from '@/views/bug-management/components/bug-detail-drawer.vue';
  import BugNamePopover from '@/views/case-management/caseManagementFeature/components/tabContent/tabBug/bugNamePopover.vue';

  import { getCustomOptionHeader, getPlatform } from '@/api/modules/bug-management';
  import { planDetailBugPage } from '@/api/modules/test-plan/testPlan';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';
  import { addCommasToNumber } from '@/utils';
  import { hasAnyPermission } from '@/utils/permission';

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
      ellipsis: true,
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
      ellipsis: true,
      showDrag: false,
    },
    {
      title: 'caseManagement.featureCase.linkCase',
      slotName: 'linkCase',
      dataIndex: 'linkCase',
      showInTable: true,
      width: 150,
      ellipsis: true,
    },
    {
      title: 'caseManagement.featureCase.updateUser',
      slotName: 'handleUser',
      dataIndex: 'handleUser',
      titleSlotName: 'handleUserFilter',
      showInTable: true,
      showTooltip: true,
      width: 300,
      ellipsis: true,
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
  const { propsRes, propsEvent, loadList, setLoadListParams } = useTable(planDetailBugPage, {
    columns: columns.value,
    tableKey: TableKeyEnum.TEST_PLAN_DETAIL_BUG_TABLE,
    scroll: { x: '100%' },
    showSelectorAll: false,
    heightUsed: 340,
    enableDrag: false,
  });

  const count = computed(() => {
    return propsRes.value.msPagination?.total || 0;
  });

  function getFetch() {
    setLoadListParams({
      planId: planId.value,
      keyword: keyword.value,
      projectId: appStore.currentProjectId,
    });
    loadList();
  }

  const tableRef = ref<InstanceType<typeof MsBaseTable>>();
  async function initFilterOptions() {
    if (hasAnyPermission(['PROJECT_BUG:READ'])) {
      const res = await getCustomOptionHeader(appStore.currentProjectId);
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
  const handleShowDetail = async (id: string) => {
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
