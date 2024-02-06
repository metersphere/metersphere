<template>
  <div>
    <div class="mb-4 flex items-center justify-between">
      <div>
        <a-button type="primary" @click="associatedDemand">
          {{ t('caseManagement.featureCase.associatedDemand') }}</a-button
        >
        <a-button class="mx-3" type="outline" @click="addDemand">
          {{ t('caseManagement.featureCase.addDemand') }}</a-button
        >
      </div>

      <a-input-search
        v-model:model-value="keyword"
        :placeholder="t('caseManagement.featureCase.searchByNameAndId')"
        allow-clear
        class="mx-[8px] w-[240px]"
        @search="searchList"
        @press-enter="searchList"
      ></a-input-search>
    </div>
    <AssociatedDemandTable
      ref="demandRef"
      :fun-params="{ caseId: props.caseId, keyword, projectId: currentProjectId }"
      @update="updateDemand"
      @create="addDemand"
    ></AssociatedDemandTable>
    <AddDemandModal v-model:visible="showAddModel" :case-id="props.caseId" :form="modelForm" @success="searchList()" />
    <MsDrawer
      v-model:visible="linkDemandDrawer"
      :ok-disabled="tableSelected.length < 1"
      :mask="false"
      :title="t('caseManagement.featureCase.associatedDemand')"
      :ok-text="t('caseManagement.featureCase.associated')"
      :ok-loading="drawerLoading"
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
  </div>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { Message } from '@arco-design/web-vue';
  import { debounce } from 'lodash-es';

  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { MsTableColumn, MsTableColumnData } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import AddDemandModal from './addDemandModal.vue';
  import AssociatedDemandTable from './associatedDemandTable.vue';

  import {
    addDemandRequest,
    getThirdDemandList
  } from '@/api/modules/case-management/featureCase';
  import { getCaseRelatedInfo } from '@/api/modules/project-management/menuManagement';
  import { useI18n } from '@/hooks/useI18n';
  import { useAppStore } from '@/store';

  import type { DemandItem } from '@/models/caseManagement/featureCase';
  import { TableKeyEnum } from '@/enums/tableEnum';

  const { t } = useI18n();
  const appStore = useAppStore();

  const currentProjectId = computed(() => appStore.currentProjectId);

  const props = defineProps<{
    caseId: string;
  }>();

  const keyword = ref<string>('');
  const demandRef = ref();

  const searchList = debounce(() => {
    demandRef.value.initData();
  }, 100);

  const showAddModel = ref<boolean>(false);

  function addDemand() {
    showAddModel.value = true;
  }
  const modelForm = ref<DemandItem>({
    id: '',
    caseId: '', // 功能用例ID
    demandId: '', // 需求ID
    demandName: '', // 需求标题
    demandUrl: '', // 需求地址
    demandPlatform: '', // 需求所属平台
    createTime: '',
    updateTime: '',
    createUser: '',
    updateUser: '',
    children: [], // 平台下对应的需求
  });

  // 更新需求
  function updateDemand(record: DemandItem) {
    showAddModel.value = true;
    modelForm.value = { ...record };
  }

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
  const fullColumns = ref<MsTableColumn>([]);

  const { propsRes, propsEvent, loadList, setLoadListParams, resetSelector } = useTable(getThirdDemandList, {
    tableKey: TableKeyEnum.CASE_MANAGEMENT_TAB_DEMAND_PLATFORM,
    columns: fullColumns.value,
    rowKey: 'demandId',
    scroll: { x: '100%' },
    selectable: true,
    showSetting: false,
  });

  const drawerLoading = ref<boolean>(false);

  const tableSelected = computed(() => {
    const selectIds = [...propsRes.value.selectedKeys];
    return propsRes.value.data.filter((item: any) => selectIds.indexOf(item.demandId) > -1);
  });

  // 关联需求
  const linkDemandDrawer = ref<boolean>(false);
  function associatedDemand() {
    linkDemandDrawer.value = true;
  }

  const platformKeyword = ref<string>('');

  const initData = async () => {
    setLoadListParams({ keyword: platformKeyword.value, projectId: currentProjectId.value });
    loadList();
  };

  const searchHandler = () => {
    initData();
    resetSelector();
  };

  const tableRef = ref();
  const customFields = ref<any[]>([]);
  async function initColumn() {
    try {
      const res = await getThirdDemandList({
        current: 1,
        pageSize: 10,
        projectId: currentProjectId.value,
      });
      customFields.value = (res.data.customHeaders || []).map((item: any) => {
        return {
          title: item.name,
          slotName: item.id,
          dataIndex: item.id,
          width: 200,
          options: item.options,
        };
      }) as any;
      fullColumns.value = [...columns, ...customFields.value];
      tableRef.value.initColumn(fullColumns.value);
    } catch (error) {
      console.log(error);
    }
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

  const platformInfo = ref<Record<string, any>>({});
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
      id: JSON.parse(platformInfo.value.demand_platform_config).zentaoId,
      caseId: props.caseId,
      demandPlatform: platformInfo.value.platform_key,
      demandList,
    };
    try {
      drawerLoading.value = true;
      await addDemandRequest(params);
      Message.success(t('caseManagement.featureCase.associatedSuccess'));
      linkDemandDrawer.value = false;
      demandRef.value.initData();
    } catch (error) {
      console.log(error);
    } finally {
      drawerLoading.value = false;
    }
  }

  function handleDrawerCancel() {
    linkDemandDrawer.value = false;
  }

  watch(
    () => linkDemandDrawer.value,
    async (val) => {
      if (val) {
        resetSelector();
        await initColumn();
        initData();
      }
    },
    {
      immediate: true,
    }
  );

  onBeforeMount(async () => {
    try {
      const result = await getCaseRelatedInfo(currentProjectId.value);
      if (result && result.platform_key) {
        platformInfo.value = { ...result };
      }
    } catch (error) {
      console.log(error);
    }
  });

  function getPlatName() {
    switch (platformInfo.value.platform_key) {
      case 'zentao':
        return t('caseManagement.featureCase.zentao');
      default:
        break;
    }
  }
</script>

<style scoped></style>
