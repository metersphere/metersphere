<template>
  <div>
    <div class="mb-[16px] flex items-center justify-between">
      <div>
        <a-radio-group v-model:model-value="showType" type="button" size="medium">
          <a-radio value="link" class="show-type-icon p-[2px]">
            {{ t('caseManagement.featureCase.directLink') }}
          </a-radio>
          <a-radio value="testPlan" class="show-type-icon p-[2px]">
            {{ t('caseManagement.featureCase.testPlan') }}
          </a-radio>
        </a-radio-group>
        <a-input-search
          v-model:model-value="keyword"
          :placeholder="t('caseManagement.featureCase.searchByName')"
          allow-clear
          class="mx-[8px] w-[240px]"
          @search="getFetch"
          @press-enter="getFetch"
          @clear="resetFetch"
          @input="changeHandler"
        ></a-input-search>
      </div>
      <div v-if="showType === 'link'" class="flex items-center">
        <a-tooltip :disabled="!!total">
          <template #content>
            {{ t('caseManagement.featureCase.noAssociatedDefect') }}
            <span v-permission="['PROJECT_BUG:READ+ADD']" class="text-[rgb(var(--primary-4))]" @click="createDefect">{{
              t('testPlan.featureCase.noBugDataNewBug')
            }}</span>
          </template>
          <a-button
            v-permission="['FUNCTIONAL_CASE:READ+UPDATE']"
            :disabled="!total"
            class="mr-3"
            type="primary"
            @click="linkDefect"
          >
            {{ t('caseManagement.featureCase.linkDefect') }}
          </a-button>
        </a-tooltip>
        <a-button v-permission="['PROJECT_BUG:READ+ADD']" type="outline" @click="createDefect"
          >{{ t('testPlan.featureCase.noBugDataNewBug') }}
        </a-button>
      </div>
    </div>
    <BugList
      v-if="showType === 'link'"
      ref="bugTableListRef"
      v-model:keyword="keyword"
      :case-id="props.caseId"
      :bug-total="total"
      :bug-columns="columns"
      :load-bug-list-api="getLinkedCaseBugList"
      :load-params="{
        caseId: props.caseId,
      }"
      :can-edit="true"
      @link="linkDefect"
      @new="createDefect"
      @cancel-link="cancelLink"
    />
    <ms-base-table v-else v-bind="testPlanPropsRes" ref="planTableRef" v-on="testPlanTableEvent">
      <template #name="{ record }">
        <BugNamePopover :name="record.name" :content="record.content" />
      </template>
      <template #handleUserName="{ record }">
        <a-tooltip :content="record.handleUserName">
          <div class="one-line-text">{{ record.handleUserName || '-' }}</div>
        </a-tooltip>
      </template>
      <template #createUserName="{ record }">
        <a-tooltip :content="record.handleUserName">
          <div class="one-line-text">{{ record.createUserName || '-' }}</div>
        </a-tooltip>
      </template>
      <template #testPlanName="{ record }">
        <a-button type="text" class="px-0" @click="goToPlan(record)">{{ record.testPlanName }}</a-button>
      </template>

      <template #severityFilter="{ columnConfig }">
        <TableFilter
          v-model:visible="severityFilterVisible"
          v-model:status-filters="severityFilterValue"
          :title="(columnConfig.title as string)"
          :list="severityFilterOptions"
          value-key="value"
          @search="getFetch()"
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
        <div class="flex w-full items-center justify-center text-[var(--color-text-4)]">
          {{ t('caseManagement.caseReview.tableNoDataNoPermission') }}
        </div>
      </template>
    </ms-base-table>
    <AddDefectDrawer v-model:visible="showDrawer" :extra-params="{ caseId: props.caseId }" @success="getFetch()" />
    <LinkDefectDrawer
      v-model:visible="showLinkDrawer"
      :case-id="props.caseId"
      :load-api="AssociatedBugApiTypeEnum.FUNCTIONAL_BUG_LIST"
      :drawer-loading="drawerLoading"
      :show-selector-all="false"
      @save="saveHandler"
    />
  </div>
</template>

<script setup lang="ts">
  /**
   * @description 用例管理-详情抽屉-tab-缺陷
   */
  import { ref } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import { Message } from '@arco-design/web-vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import BugList from './bugList.vue';
  import BugNamePopover from '@/views/case-management/caseManagementFeature/components/tabContent/tabBug/bugNamePopover.vue';
  import TableFilter from '@/views/case-management/caseManagementFeature/components/tableFilter.vue';
  import AddDefectDrawer from '@/views/case-management/components/addDefectDrawer/index.vue';
  import LinkDefectDrawer from '@/views/case-management/components/linkDefectDrawer.vue';

  import { getBugList, getCustomOptionHeader } from '@/api/modules/bug-management';
  import {
    associatedDebug,
    cancelAssociatedDebug,
    getLinkedCaseBugList,
  } from '@/api/modules/case-management/featureCase';
  import { useI18n } from '@/hooks/useI18n';
  import { useAppStore } from '@/store';
  import useFeatureCaseStore from '@/store/modules/case/featureCase';
  import { hasAnyPermission } from '@/utils/permission';

  import { BugListItem, BugOptionItem } from '@/models/bug-management';
  import type { TableQueryParams } from '@/models/common';
  import { AssociatedBugApiTypeEnum } from '@/enums/associateBugEnum';
  import { TestPlanRouteEnum } from '@/enums/routeEnum';

  import { makeColumns } from '@/views/case-management/caseManagementFeature/components/utils';

  const featureCaseStore = useFeatureCaseStore();

  const appStore = useAppStore();
  const { t } = useI18n();

  const props = defineProps<{
    caseId: string;
  }>();

  const showType = ref('link');

  const keyword = ref<string>('');
  const handleUserFilterValue = ref<string[]>([]);
  const handleUserFilterOptions = ref<BugOptionItem[]>([]);
  const statusFilterValue = ref<string[]>([]);
  const statusFilterOptions = ref<BugOptionItem[]>([]);
  const severityFilterOptions = ref<BugOptionItem[]>([]);
  const severityFilterVisible = ref(false);
  const severityFilterValue = ref<string[]>([]);
  const severityColumnId = ref('');

  const router = useRouter();
  const route = useRoute();

  const columns = ref<MsTableColumn>([
    {
      title: 'caseManagement.featureCase.tableColumnID',
      dataIndex: 'num',
      width: 100,
      showTooltip: true,
      fixed: 'left',
    },
    {
      title: 'caseManagement.featureCase.defectName',
      slotName: 'name',
      dataIndex: 'name',
      width: 300,
    },
    {
      title: 'caseManagement.featureCase.defectState',
      slotName: 'statusName',
      dataIndex: 'status',
      filterConfig: {
        options: [],
        labelKey: 'text',
      },
      width: 150,
    },
    {
      title: 'common.creator',
      slotName: 'createUserName',
      dataIndex: 'createUserName',
      width: 200,
    },
    {
      title: 'caseManagement.featureCase.updateUser',
      slotName: 'handleUserName',
      dataIndex: 'handleUser',
      filterConfig: {
        options: [],
        labelKey: 'text',
      },
      width: 200,
    },
    {
      title: 'caseManagement.featureCase.defectSource',
      slotName: 'source',
      dataIndex: 'source',
      showTooltip: true,
      width: 100,
    },
    {
      title: 'caseManagement.featureCase.tableColumnActions',
      slotName: 'operation',
      dataIndex: 'operation',
      fixed: 'right',
      width: 100,
    },
  ]);

  const testPlanColumns: MsTableColumn = [
    {
      title: 'caseManagement.featureCase.tableColumnID',
      dataIndex: 'num',
      width: 100,
      showInTable: true,
      showTooltip: true,
      showDrag: false,
    },
    {
      title: 'caseManagement.featureCase.defectName',
      slotName: 'name',
      dataIndex: 'name',
      width: 250,
    },
    {
      title: 'caseManagement.featureCase.planName',
      slotName: 'testPlanName',
      dataIndex: 'testPlanName',
      showTooltip: true,
      width: 200,
    },
    {
      title: 'caseManagement.featureCase.defectState',
      slotName: 'statusName',
      dataIndex: 'statusName',
      showTooltip: true,
      width: 150,
    },
    {
      title: 'caseManagement.featureCase.updateUser',
      slotName: 'handleUserName',
      dataIndex: 'handleUser',
      filterConfig: {
        options: [],
        labelKey: 'text',
      },
      width: 200,
    },
  ];

  const {
    propsRes: testPlanPropsRes,
    propsEvent: testPlanTableEvent,
    loadList: testPlanLinkList,
    setLoadListParams: setTestPlanListParams,
  } = useTable(getLinkedCaseBugList, {
    columns: testPlanColumns,
    heightUsed: 354,
  });

  function initTableParams() {
    const filterParams: Record<string, any> = {
      status: statusFilterValue.value,
      handleUser: handleUserFilterValue.value,
    };
    // TODO 不知道干啥的 要和后台同学确认一下
    filterParams[severityColumnId.value] = severityFilterValue.value;
    return {
      keyword: keyword.value,
      testPlanCaseId: props.caseId,
      projectId: appStore.currentProjectId,
      condition: {
        keyword: keyword.value,
        filter: testPlanPropsRes.value.filter,
      },
    };
  }
  const bugTableListRef = ref();
  const planTableRef = ref();

  async function initFilterOptions() {
    if (hasAnyPermission(['PROJECT_BUG:READ'])) {
      const res = await getCustomOptionHeader(appStore.currentProjectId);
      handleUserFilterOptions.value = res.handleUserOption;
      statusFilterOptions.value = res.statusOption;
      const optionsMap: Record<string, any> = {
        status: statusFilterOptions.value,
        handleUser: handleUserFilterOptions.value,
      };
      if (showType.value === 'link') {
        columns.value = makeColumns(optionsMap, columns.value);
      } else {
        const planColumnList = makeColumns(optionsMap, testPlanColumns);
        planTableRef.value.initColumn(planColumnList);
      }
    }
  }

  async function getFetch() {
    if (!hasAnyPermission(['FUNCTIONAL_CASE:READ', 'FUNCTIONAL_CASE:READ+UPDATE', 'FUNCTIONAL_CASE:READ+DELETE'])) {
      return;
    }
    if (showType.value === 'link') {
      bugTableListRef.value?.searchData(keyword.value);
    } else {
      setTestPlanListParams(initTableParams());
      await testPlanLinkList();
    }
    featureCaseStore.getCaseCounts(props.caseId);
  }
  async function resetFetch() {
    if (showType.value === 'link') {
      bugTableListRef.value?.searchData(keyword.value);
    } else {
      setTestPlanListParams({ keyword: '', projectId: appStore.currentProjectId, testPlanCaseId: props.caseId });
      await testPlanLinkList();
    }
    featureCaseStore.getCaseCounts(props.caseId);
  }
  const cancelLoading = ref<boolean>(false);
  // 取消关联
  async function cancelLink(id: string) {
    cancelLoading.value = true;
    try {
      if (showType.value === 'link') {
        await cancelAssociatedDebug(id);
        Message.success(t('caseManagement.featureCase.cancelLinkSuccess'));
        getFetch();
      }
    } catch (error) {
      // eslint-disable-next-line no-console
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
      await associatedDebug(params);
      Message.success(t('caseManagement.featureCase.associatedSuccess'));
      getFetch();
      showLinkDrawer.value = false;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      drawerLoading.value = false;
    }
  }

  watch(
    () => showType.value,
    (val) => {
      if (val) {
        initFilterOptions();
        getFetch();
      }
    }
  );

  const total = ref<number>(0);
  async function initBugList() {
    if (!hasAnyPermission(['PROJECT_BUG:READ'])) {
      return;
    }
    const res = await getBugList({
      current: 1,
      pageSize: 10,
      sort: {},
      filter: {},
      keyword: '',
      combine: {},
      searchMode: 'AND',
      projectId: appStore.currentProjectId,
    });
    total.value = res.total;
  }

  function changeHandler() {
    if (keyword.value.trim().length === 0) {
      getFetch();
    }
  }

  // 去测试计划页面
  function goToPlan(record: BugListItem) {
    router.push({
      name: TestPlanRouteEnum.TEST_PLAN_INDEX_DETAIL,
      query: {
        ...route.query,
        id: record.testPlanId,
      },
      state: {
        params: JSON.stringify(setTestPlanListParams()),
      },
    });
  }

  watch(
    () => props.caseId,
    (val) => {
      if (val) {
        getFetch();
      }
    }
  );

  onBeforeMount(() => {
    initFilterOptions();
    getFetch();
    initBugList();
  });
</script>

<style scoped></style>
