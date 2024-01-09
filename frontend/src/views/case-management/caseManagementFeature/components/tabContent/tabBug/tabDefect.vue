<template>
  <div>
    <div class="flex items-center justify-between">
      <div v-if="showType === 'link'" class="flex">
        <a-tooltip>
          <template #content>
            {{ t('caseManagement.featureCase.noAssociatedDefect') }}
            <span class="text-[rgb(var(--primary-4))]" @click="createDefect">{{
              t('caseManagement.featureCase.createDefect')
            }}</span>
          </template>
          <a-button class="mr-3" type="primary" @click="linkDefect">
            {{ t('caseManagement.featureCase.linkDefect') }}
          </a-button>
        </a-tooltip>
        <a-button type="outline" @click="createDefect">{{ t('caseManagement.featureCase.createDefect') }} </a-button>
      </div>
      <div v-else class="font-medium">{{ t('caseManagement.featureCase.testPlanLinkList') }}</div>
      <div class="mb-4">
        <a-radio-group v-model:model-value="showType" type="button" class="file-show-type ml-[4px]">
          <a-radio value="link" class="show-type-icon p-[2px]">{{
            t('caseManagement.featureCase.directLink')
          }}</a-radio>
          <a-radio value="testPlan" class="show-type-icon p-[2px]">{{
            t('caseManagement.featureCase.testPlan')
          }}</a-radio>
        </a-radio-group>
        <a-input-search
          v-model:model-value="keyword"
          :placeholder="t('caseManagement.featureCase.searchByNameAndId')"
          allow-clear
          class="mx-[8px] w-[240px]"
        ></a-input-search>
      </div>
    </div>
    <ms-base-table v-if="showType === 'link'" ref="tableRef" v-bind="linkPropsRes" v-on="linkTableEvent">
      <template #title="{ record }">
        <span class="one-line-text max-w[300px]"> {{ record.title }}</span>
        <a-popover title="" position="right">
          <span class="ml-1 text-[rgb(var(--primary-5))]">{{ t('caseManagement.featureCase.preview') }}</span>
          <template #content>
            <div class="min-w-[300px] text-[14px] text-[var(--color-text-1)]">
              {{ record.title }}
            </div>
          </template>
        </a-popover>
      </template>
      <template #operation="{ record }">
        <MsButton @click="cancelLink(record)">{{ t('caseManagement.featureCase.cancelLink') }}</MsButton>
      </template>
      <template v-if="(keyword || '').trim() === ''" #empty>
        <div class="flex items-center justify-center">
          {{ t('caseManagement.caseReview.tableNoData') }}
          <MsButton class="ml-[8px]" @click="linkDefect">
            {{ t('caseManagement.featureCase.linkDefect') }}
          </MsButton>
          {{ t('caseManagement.featureCase.or') }}
          <MsButton class="ml-[8px]" @click="createDefect">
            {{ t('caseManagement.featureCase.createDefect') }}
          </MsButton>
        </div>
      </template>
    </ms-base-table>
    <ms-base-table v-else v-bind="testPlanPropsRes" v-on="testPlanTableEvent">
      <template #defectName="{ record }">
        <span class="one-line-text max-w[300px]"> {{ record.title }}</span
        ><span class="ml-1 text-[rgb(var(--primary-5))]">{{ t('caseManagement.featureCase.preview') }}</span>
      </template>
      <template #operation="{ record }">
        <MsButton @click="cancelLink(record)">{{ t('caseManagement.featureCase.cancelLink') }}</MsButton>
      </template>
      <template v-if="(keyword || '').trim() === ''" #empty>
        <div class="flex items-center justify-center">
          {{ t('caseManagement.caseReview.tableNoData') }}
          <MsButton class="ml-[8px]" @click="createDefect">
            {{ t('caseManagement.featureCase.createDefect') }}
          </MsButton>
        </div>
      </template>
    </ms-base-table>
    <AddDefectDrawer v-model:visible="showDrawer" />
    <LinkDefectDrawer v-model:visible="showLinkDrawer" />
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

  import { getBugList } from '@/api/modules/bug-management/index';
  import { useI18n } from '@/hooks/useI18n';
  import { useAppStore } from '@/store';

  import { TableKeyEnum } from '@/enums/tableEnum';

  const appStore = useAppStore();
  const { t } = useI18n();

  const showType = ref('link');

  const keyword = ref<string>('');

  const columns: MsTableColumn = [
    {
      title: 'caseManagement.featureCase.tableColumnID',
      dataIndex: 'id',
      width: 200,
      showInTable: true,
      showTooltip: true,
      showDrag: false,
    },
    {
      title: 'caseManagement.featureCase.defectName',
      slotName: 'title',
      dataIndex: 'title',
      showInTable: true,
      showTooltip: false,
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
    {
      title: 'caseManagement.featureCase.updateUser',
      slotName: 'name',
      dataIndex: 'updateUser',
      showInTable: true,
      showTooltip: true,
      width: 300,
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
  } = useTable(getBugList, {
    columns,
    scroll: { x: '100%' },
    heightUsed: 340,
    enableDrag: true,
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
      slotName: 'defectName',
      dataIndex: 'defectName',
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
    propsRes: testPlanPropsRes,
    propsEvent: testPlanTableEvent,
    loadList: testPlanLinkList,
    setLoadListParams: setTestPlanListParams,
  } = useTable(getBugList, {
    columns: testPlanColumns,
    scroll: { x: '100%' },
    heightUsed: 340,
    enableDrag: true,
  });

  function getFetch() {
    if (showType.value === 'link') {
      setLinkListParams({ keyword: keyword.value, projectId: appStore.currentProjectId });
      loadLinkList();
    } else {
      setTestPlanListParams({ keyword: keyword.value, projectId: appStore.currentProjectId });
      testPlanLinkList();
    }
  }
  // 取消关联
  function cancelLink(record: any) {}

  const showDrawer = ref<boolean>(false);
  function createDefect() {
    showDrawer.value = true;
  }

  const showLinkDrawer = ref<boolean>(false);

  function linkDefect() {
    showLinkDrawer.value = true;
  }

  watch(
    () => showType.value,
    (val) => {
      if (val) {
        getFetch();
      }
    }
  );

  onMounted(() => {
    getFetch();
  });
</script>

<style scoped></style>
