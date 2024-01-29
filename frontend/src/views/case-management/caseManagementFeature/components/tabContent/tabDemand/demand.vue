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
      :mask="false"
      :title="t('caseManagement.featureCase.associatedDemand')"
      :ok-text="t('caseManagement.featureCase.associated')"
      :ok-loading="drawerLoading"
      :ok-disabled="tableSelected.length < 1"
      :width="960"
      unmount-on-close
      :show-continue="false"
      @confirm="handleDrawerConfirm"
      @cancel="handleDrawerCancel"
    >
      <div class="flex items-center justify-between">
        <div
          ><span class="font-medium">XXXXXXXXX</span
          ><span class="ml-1 text-[var(--color-text-4)]">({{ propsRes.value }})</span></div
        >
        <a-input-search
          v-model="platformKeyword"
          :max-length="250"
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
      </ms-base-table>
    </MsDrawer>
  </div>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { debounce } from 'lodash-es';

  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import AddDemandModal from './addDemandModal.vue';
  import AssociatedDemandTable from './associatedDemandTable.vue';

  import { batchAssociationDemand, getThirdDemandList } from '@/api/modules/case-management/featureCase';
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
      showInTable: true,
      width: 200,
    },
    {
      title: 'caseManagement.featureCase.tableColumnName',
      slotName: 'demandName',
      dataIndex: 'demandName',
      width: 300,
    },
    {
      title: 'caseManagement.featureCase.platformDemandState',
      width: 300,
      dataIndex: 'status',
      showInTable: true,
      showTooltip: true,
      ellipsis: true,
    },
    {
      title: 'caseManagement.featureCase.platformDemandHandler',
      width: 300,
      dataIndex: 'handler',
      showInTable: true,
      showTooltip: true,
      ellipsis: true,
    },
    {
      title: 'caseManagement.featureCase.IterationPlan',
      width: 300,
      dataIndex: 'iterationPlan',
      showInTable: true,
      showTooltip: true,
      ellipsis: true,
    },
  ];

  const { propsRes, propsEvent, loadList, setLoadListParams, resetSelector } = useTable(getThirdDemandList, {
    tableKey: TableKeyEnum.CASE_MANAGEMENT_TAB_DEMAND_PLATFORM,
    columns,
    rowKey: 'id',
    scroll: { x: '100%' },
    selectable: false,
    showSetting: false,
  });

  const showDrawer = ref<boolean>(false);

  const drawerLoading = ref<boolean>(false);

  const tableSelected = computed(() => {
    const selectIds = [...propsRes.value.selectedKeys];
    return propsRes.value.data.filter((item: any) => selectIds.indexOf(item.id) > -1);
  });

  async function handleDrawerConfirm() {
    const params = {
      id: '',
      caseId: props.caseId,
      demandPlatform: '',
      demandList: [
        {
          demandId: 'string',
          parent: 'string',
          demandName: 'string',
          demandUrl: 'string',
        },
      ],
    };
    try {
      drawerLoading.value = true;
      await batchAssociationDemand(params);
    } catch (error) {
      console.log(error);
    } finally {
      drawerLoading.value = false;
    }
  }

  function handleDrawerCancel() {
    showDrawer.value = false;
  }

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

  watch(
    () => linkDemandDrawer.value,
    (val) => {
      if (val) {
        resetSelector();
        initData();
      }
    }
  );
  // onMounted(() => {
  //   resetSelector();
  //   initData();
  // });
</script>

<style scoped></style>
