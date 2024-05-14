<template>
  <div>
    <div class="mb-4 flex items-center justify-between">
      <a-dropdown-button
        v-if="hasAnyPermission(['PROJECT_TEST_PLAN:READ+UPDATE']) && total"
        type="primary"
        @click="handleSelect('associated')"
      >
        {{ t('common.associated') }}
        <template #icon>
          <icon-down />
        </template>
        <template #content>
          <a-doption value="new" @click="handleSelect('new')">
            {{ t('common.newCreate') }}
          </a-doption>
        </template>
      </a-dropdown-button>
      <a-dropdown-button
        v-if="hasAnyPermission(['PROJECT_TEST_PLAN:READ+UPDATE']) && !total"
        type="primary"
        @click="handleSelect('new')"
      >
        {{ t('common.newCreate') }}
        <template #icon>
          <icon-down />
        </template>
        <template #content>
          <a-popover title="" position="right">
            <a-doption value="associated" :disabled="!total" @click="handleSelect('associated')">
              {{ t('common.associated') }}
            </a-doption>
            <template #content>
              <div class="flex items-center text-[14px]">
                <span class="text-[var(--color-text-4)]">{{ t('testPlan.featureCase.noBugDataTooltip') }}</span>
                <MsButton type="text" @click="handleSelect('new')">
                  {{ t('testPlan.featureCase.noBugDataNewBug') }}
                </MsButton>
              </div>
            </template>
          </a-popover>
        </template>
      </a-dropdown-button>
      <a-input-search
        v-model:model-value="keyword"
        :placeholder="t('caseManagement.featureCase.searchByName')"
        allow-clear
        class="mx-[8px] w-[240px]"
        @search="initData"
        @press-enter="initData"
        @clear="initData"
      />
    </div>
    <BugList
      ref="bugTableListRef"
      :case-id="props.caseId || '653745575542786'"
      :keyword="keyword"
      :bug-total="total"
      :bug-columns="columns"
      @link="linkDefect"
      @new="createDefect"
      @cancel-link="cancelLink"
    />
    <LinkDefectDrawer
      v-model:visible="showLinkDrawer"
      :case-id="props.caseId || '653745575542786'"
      :drawer-loading="drawerLoading"
      @save="saveHandler"
    />
    <AddDefectDrawer v-model:visible="showDrawer" :case-id="props.caseId || '653745575542786'" @success="initData()" />
  </div>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { Message } from '@arco-design/web-vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import type { MsTableColumn } from '@/components/pure/ms-table/type';
  import AddDefectDrawer from '@/views/case-management/caseManagementFeature/components/tabContent/tabBug/addDefectDrawer.vue';
  import BugList from '@/views/case-management/caseManagementFeature/components/tabContent/tabBug/bugList.vue';
  import LinkDefectDrawer from '@/views/case-management/caseManagementFeature/components/tabContent/tabBug/linkDefectDrawer.vue';

  import { getBugList, getCustomOptionHeader } from '@/api/modules/bug-management';
  import { associatedDrawerDebug, cancelAssociatedDebug } from '@/api/modules/case-management/featureCase';
  import { useI18n } from '@/hooks/useI18n';
  import { useAppStore } from '@/store';
  import { hasAnyPermission } from '@/utils/permission';

  import { BugOptionItem } from '@/models/bug-management';
  import type { TableQueryParams } from '@/models/common';

  import { makeColumns } from '@/views/case-management/caseManagementFeature/components/utils';

  const { t } = useI18n();
  const appStore = useAppStore();
  const props = defineProps<{
    caseId: string;
  }>();

  const keyword = ref<string>('');

  const columns = ref<MsTableColumn>([
    {
      title: 'caseManagement.featureCase.tableColumnID',
      dataIndex: 'num',
      width: 200,
      showInTable: true,
      showTooltip: true,
      showDrag: false,
      fixed: 'left',
    },
    {
      title: 'caseManagement.featureCase.defectName',
      slotName: 'name',
      dataIndex: 'name',
      showInTable: true,
      showTooltip: false,
      width: 200,
      ellipsis: true,
      showDrag: false,
    },
    {
      title: 'caseManagement.featureCase.defectState',
      slotName: 'statusName',
      dataIndex: 'status',
      filterConfig: {
        options: [],
        labelKey: 'text',
      },
      showInTable: true,
      width: 150,
      ellipsis: true,
      showDrag: false,
    },
    {
      title: 'caseManagement.featureCase.updateUser',
      slotName: 'handleUserName',
      dataIndex: 'handleUser',
      filterConfig: {
        options: [],
        labelKey: 'text',
      },
      showInTable: true,
      width: 200,
      ellipsis: true,
    },
    {
      title: 'caseManagement.featureCase.defectSource',
      slotName: 'source',
      dataIndex: 'source',
      showInTable: true,
      showTooltip: true,
      width: 100,
      ellipsis: true,
      showDrag: false,
    },
    {
      title: 'caseManagement.featureCase.tableColumnActions',
      slotName: 'operation',
      dataIndex: 'operation',
      fixed: 'right',
      width: 100,
      showInTable: true,
      showDrag: false,
    },
  ]);

  const bugTableListRef = ref();

  async function initData() {
    if (!hasAnyPermission(['FUNCTIONAL_CASE:READ', 'FUNCTIONAL_CASE:READ+UPDATE', 'FUNCTIONAL_CASE:READ+DELETE'])) {
      return;
    }
    bugTableListRef.value?.searchData();
  }

  const showLinkDrawer = ref<boolean>(false);
  function linkDefect() {
    showLinkDrawer.value = true;
  }

  const showDrawer = ref<boolean>(false);
  function createDefect() {
    showDrawer.value = true;
  }

  function handleSelect(value: string | number | Record<string, any> | undefined) {
    switch (value) {
      case 'associated':
        linkDefect();
        break;
      default:
        createDefect();
        break;
    }
  }

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

  const cancelLoading = ref<boolean>(false);
  // 取消关联
  async function cancelLink(id: string) {
    cancelLoading.value = true;
    try {
      await cancelAssociatedDebug(id);
      Message.success(t('caseManagement.featureCase.cancelLinkSuccess'));
      initData();
    } catch (error) {
      console.log(error);
    } finally {
      cancelLoading.value = false;
    }
  }
  const handleUserFilterOptions = ref<BugOptionItem[]>([]);
  const statusFilterOptions = ref<BugOptionItem[]>([]);

  async function initFilterOptions() {
    if (hasAnyPermission(['PROJECT_BUG:READ'])) {
      const res = await getCustomOptionHeader(appStore.currentProjectId);
      handleUserFilterOptions.value = res.handleUserOption;
      statusFilterOptions.value = res.statusOption;
      const optionsMap: Record<string, any> = {
        status: statusFilterOptions.value,
        handleUser: handleUserFilterOptions.value,
      };
      columns.value = makeColumns(optionsMap, columns.value);
    }
  }

  const drawerLoading = ref<boolean>(false);
  async function saveHandler(params: TableQueryParams) {
    try {
      drawerLoading.value = true;
      await associatedDrawerDebug(params);
      Message.success(t('caseManagement.featureCase.associatedSuccess'));
      initData();
      showLinkDrawer.value = false;
    } catch (error) {
      console.log(error);
    } finally {
      drawerLoading.value = false;
    }
  }

  onBeforeMount(() => {
    initFilterOptions();
    initData();
    initBugList();
  });
</script>

<style scoped></style>
