<template>
  <a-scrollbar class="h-full overflow-y-auto">
    <MsFormTable
      :data="tableData"
      :columns="columns"
      :selectable="false"
      :scroll="props.scroll"
      @filter-change="handleFilterChange"
      @sorter-change="handleSortChange"
    >
      <template #assertionItem="{ record }">
        <a-tooltip :content="record.name">
          <div class="flex items-center gap-[4px]">
            【{{
              t(
                responseAssertionTypeMap[(record as ResponseAssertionTableItem).assertionType] ||
                  'apiTestDebug.responseBody'
              )
            }}】
            {{ record.name }}
          </div>
        </a-tooltip>
      </template>
      <template #condition="{ record }">
        <div class="pl-[8px]">
          {{
            record.assertionType === FullResponseAssertionType.RESPONSE_TIME
              ? t('advanceFilter.operator.le')
              : t(statusCodeOptions.find((item) => item.value === record.condition)?.label || '-')
          }}
        </div>
      </template>
      <template #status="{ record }">
        <MsTag :type="record.pass === true ? 'success' : 'danger'" theme="light">
          {{ record.pass === true ? t('common.success') : t('common.fail') }}
        </MsTag>
      </template>
      <template #[FilterSlotNameEnum.API_TEST_API_RESPONSE_ASSERTION_STATUS]="{ filterContent }">
        <MsTag :type="filterContent.value === true ? 'success' : 'danger'" theme="light" tooltip-disabled>
          {{ filterContent.value === true ? t('common.success') : t('common.fail') }}
        </MsTag>
      </template>
    </MsFormTable>
  </a-scrollbar>
</template>

<script setup lang="ts">
  import { statusCodeOptions } from '@/components/pure/ms-advance-filter/index';
  import MsFormTable from '@/components/pure/ms-form-table/index.vue';
  import { MsTableColumn } from '@/components/pure/ms-table/type';
  import MsTag from '@/components/pure/ms-tag/ms-tag.vue';

  import { useI18n } from '@/hooks/useI18n';

  import { RequestResult, ResponseAssertionTableItem } from '@/models/apiTest/common';
  import { FullResponseAssertionType } from '@/enums/apiEnum';
  import { FilterSlotNameEnum } from '@/enums/tableFilterEnum';

  import { responseAssertionTypeMap } from '@/views/api-test/components/config';

  const { t } = useI18n();
  const props = defineProps<{
    requestResult?: RequestResult;
    scroll?: {
      x?: number | string;
      y?: number | string;
      maxHeight?: number | string;
      minWidth?: number | string;
    };
  }>();

  const columns: MsTableColumn = [
    {
      title: 'apiTestDebug.assertionItem',
      dataIndex: 'assertionItem',
      slotName: 'assertionItem',
      width: 200,
    },
    {
      title: 'apiTestDebug.actualValue',
      dataIndex: 'actualValue',
      slotName: 'actualValue',
      showTooltip: true,
      inputType: 'text',
      width: 200,
    },
    {
      title: 'apiTestDebug.condition',
      dataIndex: 'condition',
      slotName: 'condition',
      width: 120,
    },
    {
      title: 'apiTestDebug.expectedValue',
      dataIndex: 'expectedValue',
      slotName: 'expectedValue',
      showTooltip: true,
      inputType: 'text',
      width: 200,
    },
    {
      title: 'apiTestDebug.status',
      dataIndex: 'pass',
      slotName: 'status',
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      filterConfig: {
        options: [
          {
            value: true,
            label: t('common.success'),
          },
          {
            value: false,
            label: t('common.fail'),
          },
        ],
        filterSlotName: FilterSlotNameEnum.API_TEST_API_RESPONSE_ASSERTION_STATUS,
      },
      width: 120,
    },
    {
      title: 'apiTestDebug.reason',
      dataIndex: 'message',
      slotName: 'message',
      showTooltip: true,
      inputType: 'text',
      width: 300,
    },
  ];

  const tableData = ref(props.requestResult?.responseResult.assertions || []);
  const tableFilters = ref<string[] | (string | number | boolean)[]>([]);

  function handleFilterChange(dataIndex: string, value: string[] | (string | number | boolean)[] | undefined) {
    if (value && value.length > 0) {
      tableFilters.value = value;
      tableData.value =
        props.requestResult?.responseResult.assertions.filter((item) => {
          return (value as boolean[]).includes(item.pass);
        }) || [];
    } else {
      tableData.value = props.requestResult?.responseResult.assertions || [];
      tableFilters.value = [];
    }
  }

  function handleSortChange(sorter: { [key: string]: string }) {
    if (Object.keys(sorter).length > 0) {
      const dataIndex = Object.keys(sorter)[0] as keyof ResponseAssertionTableItem;
      const copyArray =
        tableFilters.value.length > 0
          ? [...tableData.value]
          : [...(props.requestResult?.responseResult.assertions || [])];
      tableData.value = copyArray.sort((a, b) => {
        const sortResult = a[dataIndex] > b[dataIndex] ? -1 : 1;
        return sorter[dataIndex] === 'asc' ? sortResult : -sortResult;
      });
    } else if (tableFilters.value.length > 0) {
      handleFilterChange('pass', tableFilters.value);
    } else {
      tableData.value = props.requestResult?.responseResult.assertions || [];
    }
  }

  watch(
    () => props.requestResult?.responseResult.assertions,
    () => {
      tableData.value = props.requestResult?.responseResult.assertions || [];
    }
  );
</script>

<style lang="less" scoped>
  .arco-scrollbar {
    @apply h-full;
  }
</style>
