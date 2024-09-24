<template>
  <a-popover position="left" content-class="bug-count-popover">
    <div class="one-line-text cursor-pointer px-0 text-[rgb(var(--primary-5))]">{{ props.bugCount ?? 0 }}</div>
    <template #content>
      <div class="w-[500px]">
        <MsBaseTable ref="tableRef" v-bind="propsRes" v-on="propsEvent">
          <template #operation="{ record }">
            <MsButton v-permission="['FUNCTIONAL_CASE:READ+UPDATE']" size="mini" @click="cancelLink(record.id)">{{
              t('common.cancelLink')
            }}</MsButton>
          </template>
        </MsBaseTable>
      </div>
    </template>
  </a-popover>
</template>

<script setup lang="ts">
  import { Message } from '@arco-design/web-vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { MsTableColumn, MsTableProps } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';

  import { cancelBugFromApiCase, cancelBugFromScenarioCase, testPlanCancelBug } from '@/api/modules/test-plan/testPlan';
  import { useI18n } from '@/hooks/useI18n';

  import type { CaseBugItem } from '@/models/testPlan/testPlan';
  import { CaseLinkEnum } from '@/enums/caseEnum';
  import { TableKeyEnum } from '@/enums/tableEnum';

  const props = defineProps<{
    bugList?: CaseBugItem[]; // 缺陷列表
    canEdit: boolean;
    bugCount: number;
    caseType: CaseLinkEnum;
  }>();

  const emit = defineEmits<{
    (e: 'loadList'): void;
  }>();

  const { t } = useI18n();

  const columns = computed<MsTableColumn>(() => [
    {
      title: 'caseManagement.featureCase.tableColumnID',
      dataIndex: 'num',
      width: 100,
      showTooltip: true,
    },
    {
      title: 'caseManagement.featureCase.defectName',
      dataIndex: 'title',
      showTooltip: true,
      width: 180,
    },
    {
      title: 'caseManagement.featureCase.defectState',
      dataIndex: 'status',
      width: 100,
    },
    ...(!props.canEdit
      ? []
      : [
          {
            title: 'common.operation',
            slotName: 'operation',
            dataIndex: 'operation',
            width: 100,
          },
        ]),
  ]);
  const tableProps = ref<Partial<MsTableProps<CaseBugItem[]>>>({
    columns: columns.value,
    size: 'mini',
    tableKey: TableKeyEnum.TEST_PLAN_DETAIL_CASE_TABLE_BUG_COUNT,
    scroll: { x: '100%', y: 320 },
    showSelectorAll: false,
    showPagination: false,
  });
  const { propsRes, propsEvent, setLoading } = useTable(undefined, tableProps.value);

  watchEffect(() => {
    propsRes.value.data = props.bugList || [];
  });

  const cancelBugMap: Record<string, (id: string) => Promise<any>> = {
    FUNCTIONAL: testPlanCancelBug,
    API: cancelBugFromApiCase,
    SCENARIO: cancelBugFromScenarioCase,
  };

  // 取消关联缺陷
  async function cancelLink(id: string) {
    try {
      setLoading(true);
      await cancelBugMap[props.caseType](id);
      Message.success(t('common.unLinkSuccess'));
      emit('loadList');
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      setLoading(false);
    }
  }
</script>

<style lang="less">
  .bug-count-popover {
    width: 540px;
    height: 400px;
    .arco-popover-content {
      @apply h-full;
    }
  }
</style>
