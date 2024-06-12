<template>
  <div>
    <div class="mb-4 flex items-center justify-between">
      <div>
        <a-button
          v-if="caseEnable"
          v-permission="['FUNCTIONAL_CASE:READ+ADD', 'FUNCTIONAL_CASE:READ+UPDATE', 'FUNCTIONAL_CASE:READ+DELETE']"
          type="primary"
          class="mr-2"
          @click="associatedDemand"
        >
          {{ t('caseManagement.featureCase.associatedDemand') }}</a-button
        >
        <a-button
          v-permission="['FUNCTIONAL_CASE:READ+ADD', 'FUNCTIONAL_CASE:READ+UPDATE', 'FUNCTIONAL_CASE:READ+DELETE']"
          type="outline"
          @click="addDemand"
        >
          {{ t('caseManagement.featureCase.addDemand') }}</a-button
        >
      </div>

      <a-input-search
        v-model:model-value="keyword"
        :placeholder="t('caseManagement.featureCase.searchByName')"
        allow-clear
        class="mx-[8px] w-[240px]"
        @search="searchList"
        @press-enter="searchList"
        @clear="searchList"
      ></a-input-search>
    </div>
    <AssociatedDemandTable
      ref="demandRef"
      :fun-params="{ caseId: props.caseId, keyword, projectId: currentProjectId }"
      :show-empty="true"
      :case-enable="caseEnable"
      @update="updateDemand"
      @create="addDemand"
      @cancel="cancelLink"
      @open="openDemandUrl"
      @associate="linkDemandDrawer = true"
    ></AssociatedDemandTable>
    <AddDemandModal
      ref="demandModalRef"
      v-model:visible="showAddModel"
      v-model:form="modelForm"
      :case-id="props.caseId"
      :loading="confirmLoading"
      @save="saveHandler"
      @success="searchList()"
    />
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
          ><span class="font-medium">{{ platName }}</span
          ><span class="ml-1 text-[var(--color-text-4)]">({{ propsRes?.msPagination?.total || 0 }})</span></div
        >
        <a-input-search
          v-model="platformKeyword"
          :max-length="255"
          :placeholder="t('caseManagement.featureCase.searchByIdAndName')"
          allow-clear
          class="mx-[8px] w-[240px]"
          @search="searchHandler"
          @press-enter="searchHandler"
          @clear="searchHandler"
        ></a-input-search>
      </div>
      <ms-base-table ref="tableRef" v-bind="propsRes" v-on="propsEvent">
        <template #demandId="{ record }">
          <a-tooltip :content="record.demandId" :mouse-enter-delay="300">
            <div class="one-line-text max-w-[300px]">
              {{ record.demandId }}
            </div>
          </a-tooltip>
        </template>
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
    cancelAssociationDemand,
    getThirdDemandList,
    updateDemandReq,
  } from '@/api/modules/case-management/featureCase';
  import { getCaseRelatedInfo } from '@/api/modules/project-management/menuManagement';
  import { useI18n } from '@/hooks/useI18n';
  import { useAppStore } from '@/store';

  import type { CreateOrUpdateDemand, DemandItem } from '@/models/caseManagement/featureCase';
  import { TableKeyEnum } from '@/enums/tableEnum';

  import { getPlatName } from '@/views/case-management/caseManagementFeature/components/utils';

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

  const caseEnable = ref<boolean>(false);

  const initModelForm: DemandItem = {
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
  };

  const modelForm = ref<DemandItem>({
    ...initModelForm,
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
      title: 'caseManagement.featureCase.demandName',
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
  let fullColumns: MsTableColumn = [...columns];

  const { propsRes, propsEvent, loadList, setLoadListParams, resetSelector } = useTable(getThirdDemandList, {
    tableKey: TableKeyEnum.CASE_MANAGEMENT_TAB_DEMAND_PLATFORM,
    columns: fullColumns,
    rowKey: 'demandId',
    scroll: { x: '100%' },
    heightUsed: 290,
    selectable: true,
    showSelectorAll: false,
    showSetting: false,
  });

  const drawerLoading = ref<boolean>(false);

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

  const platformKeyword = ref<string>('');

  const tableRef = ref();
  const initData = async () => {
    tableRef.value?.initColumn(fullColumns);
    setLoadListParams({ keyword: platformKeyword.value, projectId: currentProjectId.value });
    loadList();
  };

  const customFields = ref<any[]>([]);
  async function initColumn() {
    fullColumns = [...columns];
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
      fullColumns = [...columns, ...customFields.value];
    } catch (error) {
      console.log(error);
    }
  }

  // 关联需求
  const linkDemandDrawer = ref<boolean>(false);
  function associatedDemand() {
    initColumn();
    linkDemandDrawer.value = true;
  }

  const searchHandler = () => {
    if (linkDemandDrawer.value) {
      initData();
      resetSelector();
    }
  };

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

  function openDemandUrl(record: DemandItem) {
    if (record.demandUrl) {
      window.open(record.demandUrl);
    }
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
      demandList: propsRes.value.selectorStatus === 'all' ? [] : demandList,
      functionalDemandBatchRequest: {
        keyword: platformKeyword.value,
        excludeIds: [...propsRes.value.excludeKeys],
        selectAll: propsRes.value.selectorStatus === 'all',
      },
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
    platformKeyword.value = '';
  }
  // 取消关联
  async function cancelLink(record: DemandItem) {
    try {
      await cancelAssociationDemand(record.id);
      Message.success(t('caseManagement.featureCase.cancelLinkSuccess'));
      demandRef.value.initData();
    } catch (error) {
      console.log(error);
    }
  }

  async function initPlatform() {
    try {
      const result = await getCaseRelatedInfo(currentProjectId.value);
      if (result && result.platform_key) {
        platformInfo.value = { ...result };
        caseEnable.value = platformInfo.value.case_enable !== 'false';
      }
    } catch (error) {
      console.log(error);
    }
  }

  watch(
    () => linkDemandDrawer.value,
    (val) => {
      if (val) {
        resetSelector();
        nextTick(async () => {
          await tableRef.value?.initColumn(fullColumns);
          initData();
        });
      }
    },
    {
      immediate: true,
    }
  );

  const confirmLoading = ref<boolean>(false);
  const demandModalRef = ref();

  async function saveHandler(param: CreateOrUpdateDemand, isContinue: boolean) {
    try {
      confirmLoading.value = true;
      if (param.id) {
        await updateDemandReq(param);
        Message.success(t('common.updateSuccess'));
      } else {
        await addDemandRequest(param);
        Message.success(t('common.addSuccess'));
      }
      if (!isContinue) {
        showAddModel.value = false;
      }
      demandModalRef.value.resetForm();
      demandRef.value.initData();
    } catch (error) {
      console.log(error);
    } finally {
      confirmLoading.value = false;
    }
  }

  function addDemand() {
    showAddModel.value = true;
    modelForm.value = { ...initModelForm };
  }

  onMounted(async () => {
    initPlatform();
  });

  const platName = computed(() => {
    return getPlatName(platformInfo.value.platform_key);
  });
</script>

<style scoped></style>
