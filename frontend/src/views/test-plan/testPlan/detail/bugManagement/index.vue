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
      <template #num="{ record }">
        <MsButton type="text" @click="toDetail(record.id)">{{ record.num }}</MsButton>
      </template>
      <template #name="{ record }">
        <a-tooltip :content="record.title">
          <div class="one-line-text max-w-[calc(100%-32px)]"> {{ record.title }}</div>
        </a-tooltip>
        <a-popover class="bug-content-popover" title="" position="right" style="width: 480px">
          <span class="ml-1 text-[rgb(var(--primary-5))]">{{ t('caseManagement.featureCase.preview') }}</span>
          <template #content>
            <div v-dompurify-html="record.content" class="markdown-body bug-content"> </div>
          </template>
        </a-popover>
      </template>
      <template #linkCase="{ record }">
        <CaseCountPopover :bug-item="record" />
      </template>
    </MsBaseTable>
  </div>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { useRoute } from 'vue-router';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import CaseCountPopover from './caseCountPopover.vue';

  import { getCustomOptionHeader } from '@/api/modules/bug-management';
  import { planDetailBugPage } from '@/api/modules/test-plan/testPlan';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';
  import { addCommasToNumber } from '@/utils';
  import { hasAnyPermission } from '@/utils/permission';

  // import { BugManagementRouteEnum } from '@/enums/routeEnum';
  import { TableKeyEnum } from '@/enums/tableEnum';

  import { makeColumns } from '@/views/case-management/caseManagementFeature/components/utils';

  const { t } = useI18n();
  const route = useRoute();
  // const router = useRouter();
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

  function toDetail(id: string) {
    // eslint-disable-next-line no-console
    console.log('id', id);
    // TODO: 查看详情
    // window.open(
    //   `${window.location.origin}#${
    //     router.resolve({ name: BugManagementRouteEnum.BUG_MANAGEMENT_INDEX }).fullPath
    //   }?id=${id}&orgId=${appStore.currentOrgId}&pId=${appStore.currentProjectId}`
    // );
  }

  onBeforeMount(() => {
    initFilterOptions();
    getFetch();
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
