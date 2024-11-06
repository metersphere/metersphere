<template>
  <div class="card-wrapper">
    <div class="flex items-center justify-between">
      <div class="title">
        {{ t('workbench.homePage.interfaceChange') }}
      </div>
      <div>
        <MsSelect
          v-model:model-value="projectIds"
          :options="projectOptions"
          :allow-search="false"
          allow-clear
          class="!w-[240px]"
          :prefix="t('workbench.homePage.project')"
          :has-all-select="true"
          :default-all-select="true"
        >
        </MsSelect>
      </div>
    </div>
    <div class="mt-[16px]">
      <MsBaseTable
        v-bind="propsRes"
        :action-config="{
          baseAction: [],
          moreAction: [],
        }"
        class="mt-[16px]"
        v-on="propsEvent"
      >
        <template #num="{ record }">
          <MsButton type="text">{{ record.num }}</MsButton>
        </template>
      </MsBaseTable>
    </div>
  </div>
</template>

<script setup lang="ts">
  /** *
   * @desc 接口变更列表
   */
  import { ref } from 'vue';
  import dayjs from 'dayjs';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsSelect from '@/components/business/ms-select';

  import { useI18n } from '@/hooks/useI18n';

  import type { SelectOptionData } from '@arco-design/web-vue';

  const { t } = useI18n();

  const projectIds = ref('');
  const projectOptions = ref<SelectOptionData[]>([]);

  const columns: MsTableColumn = [
    {
      title: 'ID',
      slotName: 'num',
      dataIndex: 'num',
      showTooltip: true,
      width: 200,
    },
    {
      title: 'project.commonScript.apiName',
      slotName: 'apiName',
      dataIndex: 'apiName',
      width: 200,
    },
    {
      title: 'workbench.homePage.associationCASE',
      slotName: 'case',
      dataIndex: 'case',
      width: 200,
    },
    {
      title: 'workbench.homePage.associatedScene',
      slotName: 'associatedScene',
      dataIndex: 'associatedScene',
      showDrag: true,
      width: 100,
    },
    {
      title: 'common.updateTime',
      dataIndex: 'updateTime',
      showInTable: false,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      showDrag: true,
      width: 189,
    },
  ];
  const { propsRes, propsEvent, loadList, setLoadListParams } = useTable(
    undefined,
    {
      columns,
      scroll: { x: '100%' },
      selectable: false,
      heightUsed: 272,
      showSelectAll: false,
    },
    (item) => ({
      ...item,
      updateTime: dayjs(item.updateTime).format('YYYY-MM-DD HH:mm:ss'),
    })
  );

  onMounted(() => {
    setLoadListParams({});
    loadList();
  });
</script>

<style scoped></style>
