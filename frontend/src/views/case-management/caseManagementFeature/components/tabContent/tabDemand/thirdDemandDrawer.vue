<template>
  <MsDrawer
    v-model:visible="innerLinkDemandVisible"
    :ok-disabled="tableSelected.length < 1"
    :mask="false"
    :title="t('caseManagement.featureCase.associatedDemand')"
    :ok-text="t('caseManagement.featureCase.associated')"
    :ok-loading="props.drawerLoading"
    :width="960"
    unmount-on-close
    :show-continue="false"
    @confirm="handleDrawerConfirm"
    @cancel="handleDrawerCancel"
  >
    <div class="flex items-center justify-between">
      <div
        ><span class="font-medium">{{ getPlatName() }}</span
        ><span class="ml-1 text-[var(--color-text-4)]">({{ propsRes?.msPagination?.total || 0 }})</span></div
      >
      <a-input-search
        v-model="platformKeyword"
        :max-length="255"
        :placeholder="t('project.member.searchMember')"
        allow-clear
        class="mx-[8px] w-[240px]"
        @search="searchHandler"
        @press-enter="searchHandler"
      ></a-input-search>
    </div>
    <ms-base-table ref="tableRef" v-bind="propsRes" v-on="propsEvent">
      <template #demandName="{ record }">
        <span class="ml-1 text-[rgb(var(--primary-5))]">
          {{ record.demandName }}
          <span>({{ (record.children || []).length || 0 }})</span></span
        >
      </template>
      <template v-for="item in customFields" :key="item.slotName" #[item.dataIndex]="{ record }">
        <span> {{ getSlotName(record, item) }} </span>
      </template>
    </ms-base-table>
  </MsDrawer>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { useVModel } from '@vueuse/core';
  import { Message } from '@arco-design/web-vue';

  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { MsTableColumn, MsTableColumnData } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';

  import { getThirdDemandList } from '@/api/modules/case-management/featureCase';
  import { useI18n } from '@/hooks/useI18n';
  import { useAppStore } from '@/store';

  import type { CreateOrUpdateDemand, DemandItem } from '@/models/caseManagement/featureCase';
  import { TableKeyEnum } from '@/enums/tableEnum';

  const { t } = useI18n();
  const appStore = useAppStore();
  const currentProjectId = computed(() => appStore.currentProjectId);
  const props = defineProps<{
    visible: boolean;
    caseId: string;
    drawerLoading: boolean;
    platformInfo: Record<string, any>;
  }>();

  const emit = defineEmits<{
    (e: 'update:visible', visible: boolean): void;
    (e: 'save', params: CreateOrUpdateDemand): void;
  }>();

  const innerLinkDemandVisible = useVModel(props, 'visible', emit);

  const platformKeyword = ref<string>('');
  const columns: MsTableColumn = [
    {
      title: 'caseManagement.featureCase.tableColumnID',
      slotName: 'demandId',
      dataIndex: 'demandId',
      width: 200,
      showTooltip: true,
    },
    {
      title: 'caseManagement.featureCase.tableColumnName',
      slotName: 'demandName',
      dataIndex: 'demandName',
      width: 300,
      showTooltip: true,
    },
    // {
    //   title: 'caseManagement.featureCase.platformDemandState',
    //   width: 300,
    //   dataIndex: 'status',
    //   showTooltip: true,
    //   ellipsis: true,
    // },
    // {
    //   title: 'caseManagement.featureCase.platformDemandHandler',
    //   width: 300,
    //   dataIndex: 'handler',
    //   showTooltip: true,
    //   ellipsis: true,
    // },
  ];

  const fullColumns = ref<MsTableColumn>([...columns]);
  const customFields = ref<Record<string, any>[]>([]);

  const { propsRes, propsEvent, loadList, setLoadListParams, resetSelector } = useTable(getThirdDemandList, {
    tableKey: TableKeyEnum.CASE_MANAGEMENT_TAB_DEMAND_PLATFORM,
    columns: fullColumns.value,
    rowKey: 'demandId',
    scroll: { x: '100%' },
    selectable: true,
    showSetting: false,
  });

  const tableSelected = computed(() => {
    const selectedIds = [...propsRes.value.selectedKeys];
    const filteredData: DemandItem[] = [];

    function filterData(data: DemandItem[]) {
      for (let i = 0; i < data.length; i++) {
        const item: DemandItem = data[i];
        if (selectedIds.includes(item.demandId)) {
          filteredData.push(item);
        }
        if (item.children) {
          filterData(item.children);
        }
      }
    }
    filterData(propsRes.value.data);
    return filteredData;
  });

  // const platformInfo = ref<Record<string, any>>({});
  function getPlatName() {
    switch (props.platformInfo.platform_key) {
      case 'zentao':
        return t('caseManagement.featureCase.zentao');
      case 'jira':
        return t('caseManagement.featureCase.jira');
      default:
        break;
    }
  }

  async function handleDrawerConfirm() {
    const demandList = tableSelected.value.map((item) => {
      return {
        demandId: item.demandId,
        parent: item.parent,
        demandName: item.demandName,
        demandUrl: item.demandUrl,
      };
    });

    const params = {
      id: JSON.parse(props.platformInfo.demand_platform_config).zentaoId,
      caseId: props.caseId,
      demandPlatform: props.platformInfo.platform_key,
      demandList: propsRes.value.selectorStatus === 'all' ? [] : demandList,
      functionalDemandBatchRequest: {
        keyword: platformKeyword.value,
        excludeIds: [...propsRes.value.excludeKeys],
        selectAll: propsRes.value.selectorStatus === 'all',
      },
    };
    emit('save', params);
  }

  function handleDrawerCancel() {
    innerLinkDemandVisible.value = false;
  }

  function getSlotName(record: any, item: MsTableColumnData) {
    if (item?.options) {
      const currentRecord = {
        ...record,
        ...record.customFields,
      };
      const currentValue = currentRecord[item.dataIndex as string];
      const currentOptions = (JSON.parse(item.options) || []).find((it: any) => it.value === currentValue);
      if (currentOptions) {
        return currentOptions.text;
      }
    }
    return record.customFields[item.dataIndex as string] || '-';
  }

  const initData = async () => {
    setLoadListParams({ keyword: platformKeyword.value, projectId: currentProjectId.value });
    loadList();
  };

  const searchHandler = () => {
    initData();
    resetSelector();
  };

  const tableRef = ref();
  async function initColumn() {
    fullColumns.value = [...columns];
    try {
      const res = await getThirdDemandList({
        current: 1,
        pageSize: 10,
        projectId: currentProjectId.value,
      });
      if (res.data.customHeaders.length) {
        customFields.value = (res.data.customHeaders || []).map((item: any) => {
          return {
            title: item.name,
            slotName: item.id,
            dataIndex: item.id,
            width: 200,
            options: item.options,
          };
        }) as any;
      }

      fullColumns.value.push(...customFields.value);
    } catch (error) {
      console.log(error);
    }
  }

  // onBeforeMount(async () => {
  //   try {
  //     const result = await getCaseRelatedInfo(currentProjectId.value);
  //     if (result && result.platform_key) {
  //       platformInfo.value = { ...result };
  //       initColumn();
  //     }
  //   } catch (error) {
  //     console.log(error);
  //   }
  // });

  watch(
    () => innerLinkDemandVisible.value,
    async (val) => {
      if (val) {
        resetSelector();
        if (props.platformInfo.demand_platform_config) {
          initData();
        }
      }
    }
  );
</script>

<style scoped></style>
