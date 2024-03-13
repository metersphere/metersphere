<template>
  <div>
    <div class="flex items-center justify-between">
      <div v-if="showType === 'link'" class="flex">
        <a-tooltip>
          <template #content>
            {{ t('caseManagement.featureCase.noAssociatedDefect') }}
            <span v-permission="['PROJECT_BUG:READ+ADD']" class="text-[rgb(var(--primary-4))]" @click="createDefect">{{
              t('caseManagement.featureCase.createDefect')
            }}</span>
          </template>
          <a-button v-permission="['FUNCTIONAL_CASE:READ+UPDATE']" class="mr-3" type="primary" @click="linkDefect">
            {{ t('caseManagement.featureCase.linkDefect') }}
          </a-button>
        </a-tooltip>
        <a-button v-permission="['PROJECT_BUG:READ+ADD']" type="outline" @click="createDefect"
          >{{ t('caseManagement.featureCase.createDefect') }}
        </a-button>
      </div>
      <div v-else v-permission="['FUNCTIONAL_CASE:READ+UPDATE']" class="font-medium">{{
        t('caseManagement.featureCase.testPlanLinkList')
      }}</div>
      <div class="mb-4">
        <a-radio-group v-model:model-value="showType" type="button" class="file-show-type ml-[4px]">
          <a-radio value="link" class="show-type-icon p-[2px]">{{
            t('caseManagement.featureCase.directLink')
          }}</a-radio>
          <!-- <a-radio value="testPlan" class="show-type-icon p-[2px]">{{
            t('caseManagement.featureCase.testPlan')
          }}</a-radio> -->
        </a-radio-group>
        <a-input-search
          v-model:model-value="keyword"
          :placeholder="t('caseManagement.featureCase.searchByName')"
          allow-clear
          class="mx-[8px] w-[240px]"
          @search="getFetch"
          @press-enter="getFetch"
          @clear="resetFetch"
        ></a-input-search>
      </div>
    </div>
    <ms-base-table v-if="showType === 'link'" ref="tableRef" v-bind="linkPropsRes" v-on="linkTableEvent">
      <template #name="{ record }">
        <span class="one-line-text max-w-[300px]"> {{ record.name }}</span>
        <a-popover title="" position="right">
          <span class="ml-1 text-[rgb(var(--primary-5))]">{{ t('caseManagement.featureCase.preview') }}</span>
          <template #content>
            <div class="max-w-[600px] text-[14px] text-[var(--color-text-1)]">
              {{ record.content }}
            </div>
          </template>
        </a-popover>
      </template>
      <template #handleUserFilter="{ columnConfig }">
        <TableFilter
          v-model:visible="handleUserFilterVisible"
          v-model:status-filters="handleUserFilterValue"
          :title="(columnConfig.title as string)"
          :list="handleUserFilterOptions"
          value-key="value"
          @search="searchData()"
        >
          <template #item="{ item }">
            {{ item.text }}
          </template>
        </TableFilter>
      </template>

      <template #statusFilter="{ columnConfig }">
        <TableFilter
          v-model:visible="statusFilterVisible"
          v-model:status-filters="statusFilterValue"
          :title="(columnConfig.title as string)"
          :list="statusFilterOptions"
          value-key="value"
          @search="searchData()"
        >
          <template #item="{ item }">
            {{ item.text }}
          </template>
        </TableFilter>
      </template>

      <template #severityFilter="{ columnConfig }">
        <TableFilter
          v-model:visible="severityFilterVisible"
          v-model:status-filters="severityFilterValue"
          :title="(columnConfig.title as string)"
          :list="severityFilterOptions"
          value-key="value"
          @search="searchData()"
        >
          <template #item="{ item }">
            {{ item.text }}
          </template>
        </TableFilter>
      </template>

      <template #operation="{ record }">
        <MsButton v-permission="['FUNCTIONAL_CASE:READ+UPDATE']" @click="cancelLink(record.id)">{{
          t('caseManagement.featureCase.cancelLink')
        }}</MsButton>
      </template>
      <template v-if="(keyword || '').trim() === ''" #empty>
        <div class="flex w-full items-center justify-center">
          {{ t('caseManagement.featureCase.tableNoDataWidthComma') }}
          <span v-permission="['FUNCTIONAL_CASE:READ+UPDATE', 'PROJECT_BUG:READ+ADD']">{{
            t('caseManagement.featureCase.please')
          }}</span>
          <MsButton v-permission="['FUNCTIONAL_CASE:READ+UPDATE']" class="ml-[8px]" @click="linkDefect">
            {{ t('caseManagement.featureCase.linkDefect') }}
          </MsButton>
          <span v-permission="['FUNCTIONAL_CASE:READ+UPDATE', 'PROJECT_BUG:READ+ADD']">{{
            t('caseManagement.featureCase.or')
          }}</span>
          <MsButton v-permission="['PROJECT_BUG:READ+ADD']" class="ml-[8px]" @click="createDefect">
            {{ t('caseManagement.featureCase.createDefect') }}
          </MsButton>
        </div>
      </template>
    </ms-base-table>
    <ms-base-table v-else v-bind="testPlanPropsRes" v-on="testPlanTableEvent">
      <template #name="{ record }">
        <span class="one-line-text max-w-[300px]"> {{ record.name }}</span>
        <a-popover title="" position="right">
          <span class="ml-1 text-[rgb(var(--primary-5))]">{{ t('caseManagement.featureCase.preview') }}</span>
          <template #content>
            <div class="max-w-[600px] text-[14px] text-[var(--color-text-1)]">
              {{ record.content }}
            </div>
          </template>
        </a-popover>
      </template>
      <template #handleUserFilter="{ columnConfig }">
        <TableFilter
            v-model:visible="handleUserFilterVisible"
            v-model:status-filters="handleUserFilterValue"
            :title="(columnConfig.title as string)"
            :list="handleUserFilterOptions"
            value-key="value"
            @search="searchData()"
        >
          <template #item="{ item }">
            {{ item.text }}
          </template>
        </TableFilter>
      </template>
      <template #statusFilter="{ columnConfig }">
        <TableFilter
            v-model:visible="statusFilterVisible"
            v-model:status-filters="statusFilterValue"
            :title="(columnConfig.title as string)"
            :list="statusFilterOptions"
            value-key="value"
            @search="searchData()"
        >
          <template #item="{ item }">
            {{ item.text }}
          </template>
        </TableFilter>
      </template>

      <template #severityFilter="{ columnConfig }">
        <TableFilter
            v-model:visible="severityFilterVisible"
            v-model:status-filters="severityFilterValue"
            :title="(columnConfig.title as string)"
            :list="severityFilterOptions"
            value-key="value"
            @search="searchData()"
        >
          <template #item="{ item }">
            {{ item.text }}
          </template>
        </TableFilter>
      </template>
      <template #operation="{ record }">
        <MsButton @click="cancelLink(record.id)">{{ t('caseManagement.featureCase.cancelLink') }}</MsButton>
      </template>
      <template v-if="(keyword || '').trim() === ''" #empty>
        <div class="flex w-full items-center justify-center">
          {{ t('caseManagement.caseReview.tableNoData') }}
          <MsButton class="ml-[8px]" @click="createDefect">
            {{ t('caseManagement.featureCase.createDefect') }}
          </MsButton>
        </div>
      </template>
    </ms-base-table>
    <AddDefectDrawer v-model:visible="showDrawer" :case-id="props.caseId" @success="getFetch()" />
    <LinkDefectDrawer
      v-model:visible="showLinkDrawer"
      :case-id="props.caseId"
      :drawer-loading="drawerLoading"
      @save="saveHandler"
    />
  </div>
</template>

<script setup lang="ts">
  /**
   * @description 用例管理-详情抽屉-tab-缺陷
   */
  import { ref } from 'vue';
  import { Message } from '@arco-design/web-vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import AddDefectDrawer from './addDefectDrawer.vue';
  import LinkDefectDrawer from './linkDefectDrawer.vue';
  import TableFilter from '@/views/case-management/caseManagementFeature/components/tableFilter.vue';

  import { getCustomOptionHeader } from '@/api/modules/bug-management';
  import {
    associatedDrawerDebug,
    cancelAssociatedDebug,
    getLinkedCaseBugList,
  } from '@/api/modules/case-management/featureCase';
  import { useI18n } from '@/hooks/useI18n';
  import { useAppStore } from '@/store';
  import useFeatureCaseStore from '@/store/modules/case/featureCase';

  import { BugOptionItem } from '@/models/bug-management';
  import type { TableQueryParams } from '@/models/common';

  const featureCaseStore = useFeatureCaseStore();

  const appStore = useAppStore();
  const { t } = useI18n();

  const props = defineProps<{
    caseId: string;
  }>();
  // const activeTab = computed(() => featureCaseStore.activeTab);
  const showType = ref('link');

  const keyword = ref<string>('');
  const handleUserFilterVisible = ref(false);
  const handleUserFilterValue = ref<string[]>([]);
  const handleUserFilterOptions = ref<BugOptionItem[]>([]);
  const statusFilterVisible = ref(false);
  const statusFilterValue = ref<string[]>([]);
  const statusFilterOptions = ref<BugOptionItem[]>([]);
  const severityFilterOptions = ref<BugOptionItem[]>([]);
  const severityFilterVisible = ref(false);
  const severityFilterValue = ref<string[]>([]);
  const severityColumnId = ref('');

  const columns: MsTableColumn = [
    {
      title: 'caseManagement.featureCase.tableColumnID',
      dataIndex: 'num',
      width: 200,
      showInTable: true,
      showTooltip: true,
      showDrag: false,
    },
    {
      title: 'caseManagement.featureCase.defectName',
      slotName: 'name',
      dataIndex: 'name',
      showInTable: true,
      showTooltip: false,
      width: 300,
      ellipsis: true,
      showDrag: false,
    },
    {
      title: 'caseManagement.featureCase.defectState',
      slotName: 'statusName',
      dataIndex: 'statusName',
      titleSlotName: 'statusFilter',
      showInTable: true,
      showTooltip: true,
      width: 200,
      ellipsis: true,
      showDrag: false,
    },
    {
      title: 'caseManagement.featureCase.updateUser',
      slotName: 'handleUserName',
      dataIndex: 'handleUserName',
      titleSlotName: 'handleUserFilter',
      showInTable: true,
      showTooltip: true,
      width: 300,
      ellipsis: true,
    },
    {
      title: 'caseManagement.featureCase.defectSource',
      slotName: 'source',
      dataIndex: 'source',
      showInTable: true,
      showTooltip: true,
      width: 200,
      ellipsis: true,
      showDrag: false,
    },
    {
      title: 'caseManagement.featureCase.tableColumnActions',
      slotName: 'operation',
      dataIndex: 'operation',
      fixed: 'right',
      width: 140,
      showInTable: true,
      showDrag: false,
    },
  ];
  const {
    propsRes: linkPropsRes,
    propsEvent: linkTableEvent,
    loadList: loadLinkList,
    setLoadListParams: setLinkListParams,
  } = useTable(getLinkedCaseBugList, {
    columns,
    scroll: { x: 'auto' },
    heightUsed: 340,
    enableDrag: false,
  });

  const testPlanColumns: MsTableColumn = [
    {
      title: 'caseManagement.featureCase.tableColumnID',
      dataIndex: 'id',
      width: 200,
      showInTable: true,
      showTooltip: true,
      ellipsis: true,
      showDrag: false,
    },
    {
      title: 'caseManagement.featureCase.defectName',
      slotName: 'name',
      dataIndex: 'name',
      showInTable: true,
      showTooltip: true,
      width: 300,
      ellipsis: true,
      showDrag: false,
    },
    {
      title: 'caseManagement.featureCase.planName',
      slotName: 'testPlan',
      dataIndex: 'testPlan',
      showInTable: true,
      showTooltip: true,
      width: 300,
      ellipsis: true,
      showDrag: false,
    },
    {
      title: 'caseManagement.featureCase.defectState',
      slotName: 'defectState',
      dataIndex: 'defectState',
      showInTable: true,
      showTooltip: true,
      width: 300,
      ellipsis: true,
      showDrag: false,
    },
  ];

  const {
    propsRes: testPlanPropsRes,
    propsEvent: testPlanTableEvent,
    loadList: testPlanLinkList,
    setLoadListParams: setTestPlanListParams,
  } = useTable(getLinkedCaseBugList, {
    columns: testPlanColumns,
    scroll: { x: '100%' },
    heightUsed: 340,
    enableDrag: true,
  });

  function initTableParams() {
    const filterParams = {
      status: statusFilterValue.value,
      handleUser: handleUserFilterValue.value,
    };
    filterParams[severityColumnId.value] = severityFilterValue.value;
    return {
      keyword: keyword.value,
      caseId: props.caseId,
      projectId: appStore.currentProjectId,
      filter: { ...filterParams },
      condition: {
        keyword: keyword.value,
        filter: showType.value === 'link' ? linkPropsRes : 'testPlanPropsRes',
      },
    };
  }

  function searchData() {
    if (showType.value === 'link') {
      setLinkListParams(initTableParams());
      loadLinkList();
    } else {
      setTestPlanListParams(initTableParams());
      testPlanLinkList();
    }
  }

  async function initFilterOptions() {
    const res = await getCustomOptionHeader(appStore.currentProjectId);
    handleUserFilterOptions.value = res.handleUserOption;
    statusFilterOptions.value = res.statusOption;
  }

  async function getFetch() {
    if (showType.value === 'link') {
      setLinkListParams({ keyword: keyword.value, projectId: appStore.currentProjectId, caseId: props.caseId });
      await loadLinkList();
      const { msPagination } = linkPropsRes.value;
      featureCaseStore.setListCount(featureCaseStore.activeTab, msPagination?.total || 0);
    } else {
      setTestPlanListParams({ keyword: keyword.value, projectId: appStore.currentProjectId, caseId: props.caseId });
      await testPlanLinkList();
      featureCaseStore.getCaseCounts(props.caseId);
    }
  }
  async function resetFetch() {
    if (showType.value === 'link') {
      setLinkListParams({ keyword: '', projectId: appStore.currentProjectId, caseId: props.caseId });
      await loadLinkList();
      const { msPagination } = linkPropsRes.value;
      featureCaseStore.setListCount(featureCaseStore.activeTab, msPagination?.total || 0);
    } else {
      setTestPlanListParams({ keyword: '', projectId: appStore.currentProjectId, caseId: props.caseId });
      await testPlanLinkList();
      featureCaseStore.getCaseCounts(props.caseId);
    }
  }
  const cancelLoading = ref<boolean>(false);
  // 取消关联
  async function cancelLink(id: string) {
    cancelLoading.value = true;
    try {
      if (showType.value === 'link') {
        await cancelAssociatedDebug(id);
        getFetch();
        Message.success(t('caseManagement.featureCase.cancelLinkSuccess'));
      }
    } catch (error) {
      console.log(error);
    } finally {
      cancelLoading.value = false;
    }
  }

  const showDrawer = ref<boolean>(false);
  function createDefect() {
    showDrawer.value = true;
  }

  const showLinkDrawer = ref<boolean>(false);

  function linkDefect() {
    showLinkDrawer.value = true;
  }

  const drawerLoading = ref<boolean>(false);
  async function saveHandler(params: TableQueryParams) {
    try {
      drawerLoading.value = true;
      await associatedDrawerDebug(params);
      Message.success(t('caseManagement.featureCase.associatedSuccess'));
      getFetch();
      showLinkDrawer.value = false;
    } catch (error) {
      console.log(error);
    } finally {
      drawerLoading.value = false;
    }
  }

  watch(
    () => showType.value,
    (val) => {
      if (val) {
        getFetch();
      }
    }
  );

  // watch(
  //   () => activeTab.value,
  //   (val) => {
  //     if (val === 'bug') {
  //       getFetch();
  //     }
  //   }
  // );

  onMounted(() => {
    getFetch();
    initFilterOptions();
  });
</script>

<style scoped></style>
