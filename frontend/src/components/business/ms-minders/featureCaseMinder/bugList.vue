<template>
  <a-spin :loading="bugListLoading" class="block h-full pl-[16px]">
    <div class="flex items-center justify-between gap-[24px]">
      <a-radio-group
        v-model:model-value="showType"
        type="button"
        class="ml-[4px]"
        size="medium"
        @change="handleShowTypeChange"
      >
        <a-radio value="link" class="show-type-icon p-[2px]">
          {{ t('caseManagement.featureCase.directLink') }}
        </a-radio>
        <a-radio value="testPlan" class="show-type-icon p-[2px]">
          {{ t('caseManagement.featureCase.testPlan') }}
        </a-radio>
      </a-radio-group>
      <div v-if="showType === 'link'" class="flex items-center justify-between">
        <a-tooltip :disabled="!!bugTotal">
          <template #content>
            {{ t('caseManagement.featureCase.noAssociatedDefect') }}
            <span v-permission="['PROJECT_BUG:READ+ADD']" class="text-[rgb(var(--primary-4))]" @click="createBug">
              {{ t('testPlan.featureCase.noBugDataNewBug') }}
            </span>
          </template>
          <a-button v-if="hasEditPermission" :disabled="!bugTotal" class="mr-3" type="primary" @click="linkBug">
            {{ t('caseManagement.featureCase.linkDefect') }}
          </a-button>
        </a-tooltip>
        <a-button v-permission="['PROJECT_BUG:READ+ADD']" type="outline" @click="createBug">
          {{ t('caseManagement.featureCase.createDefect') }}
        </a-button>
      </div>
    </div>
    <MsList
      v-model:data="bugList"
      mode="remote"
      item-key-field="id"
      :item-border="false"
      class="my-[16px] h-[calc(100%-64px)] w-full rounded-[var(--border-radius-small)]"
      :no-more-data="noMoreData"
      :virtual-list-props="{
        height: '100%',
      }"
      @reach-bottom="handleReachBottom"
    >
      <template #item="{ item }">
        <div class="bug-item">
          <div class="mb-[4px] flex items-center justify-between">
            <MsButton type="text" @click="goBug(item.bugId)">{{ item.num }}</MsButton>
            <MsButton v-if="hasEditPermission && showType === 'link'" type="text" @click="disassociateBug(item.id)">
              {{ t('ms.add.attachment.cancelAssociate') }}
            </MsButton>
          </div>
          <a-tooltip :content="item.name" position="tl">
            <div class="one-line-text">{{ item.name }}</div>
          </a-tooltip>
        </div>
      </template>
    </MsList>
  </a-spin>
  <AddDefectDrawer
    v-if="activeCase.id"
    v-model:visible="showCreateBugDrawer"
    :case-id="activeCase.id"
    :extra-params="{ caseId: activeCase.id }"
    @success="loadBugList"
  />
  <LinkDefectDrawer
    v-if="activeCase.id"
    v-model:visible="showLinkBugDrawer"
    :case-id="activeCase.id"
    :drawer-loading="drawerLoading"
    :load-api="AssociatedBugApiTypeEnum.FUNCTIONAL_BUG_LIST"
    :show-selector-all="false"
    @save="saveHandler"
  />
</template>

<script setup lang="ts">
  import { Message } from '@arco-design/web-vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsList from '@/components/pure/ms-list/index.vue';

  import { getBugList } from '@/api/modules/bug-management';
  import {
    associatedDebug,
    cancelAssociatedDebug,
    getLinkedCaseBugList,
  } from '@/api/modules/case-management/featureCase';
  import { useI18n } from '@/hooks/useI18n';
  import useOpenNewPage from '@/hooks/useOpenNewPage';
  import useAppStore from '@/store/modules/app';
  import { hasAnyPermission } from '@/utils/permission';

  import { TableQueryParams } from '@/models/common';
  import { AssociatedBugApiTypeEnum } from '@/enums/associateBugEnum';
  import { BugManagementRouteEnum } from '@/enums/routeEnum';

  const AddDefectDrawer = defineAsyncComponent(
    () => import('@/views/case-management/components/addDefectDrawer/index.vue')
  );
  const LinkDefectDrawer = defineAsyncComponent(
    () => import('@/views/case-management/components/linkDefectDrawer.vue')
  );

  const props = defineProps<{
    activeCase: Record<string, any>;
  }>();

  const { openNewPage } = useOpenNewPage();
  const appStore = useAppStore();
  const { t } = useI18n();

  const hasEditPermission = hasAnyPermission(['FUNCTIONAL_CASE:READ+UPDATE']);
  const bugList = ref<any[]>([]);
  const noMoreData = ref(false);
  const pageNation = ref({
    total: 0,
    pageSize: 10,
    current: 1,
  });
  const showType = ref<'link' | 'testPlan'>('link');
  const bugListLoading = ref(false);

  async function loadBugList() {
    try {
      bugListLoading.value = true;
      const res = await getLinkedCaseBugList({
        keyword: '',
        projectId: appStore.currentProjectId,
        testPlanCaseId: showType.value === 'testPlan' ? props.activeCase.id : undefined,
        caseId: showType.value === 'link' ? props.activeCase.id : undefined,
        current: pageNation.value.current || 1,
        pageSize: pageNation.value.pageSize,
      });
      if (pageNation.value.current === 1) {
        bugList.value = res.list;
      } else {
        bugList.value = bugList.value.concat(res.list);
      }
      pageNation.value.total = res.total;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      bugListLoading.value = false;
    }
  }

  function handleShowTypeChange() {
    pageNation.value.current = 1;
    loadBugList();
  }

  // 滚动翻页
  function handleReachBottom() {
    pageNation.value.current += 1;
    if (pageNation.value.current > Math.ceil(pageNation.value.total / pageNation.value.pageSize)) {
      return;
    }
    loadBugList();
  }

  const cancelLoading = ref<boolean>(false);
  // 取消关联
  async function disassociateBug(id: string) {
    cancelLoading.value = true;
    try {
      await cancelAssociatedDebug(id);
      bugList.value = bugList.value.filter((item) => item.id !== id);
      Message.success(t('caseManagement.featureCase.cancelLinkSuccess'));
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      cancelLoading.value = false;
    }
  }

  function goBug(id: string) {
    openNewPage(BugManagementRouteEnum.BUG_MANAGEMENT_INDEX, {
      id,
    });
  }

  const showCreateBugDrawer = ref<boolean>(false);
  function createBug() {
    showCreateBugDrawer.value = true;
  }

  const showLinkBugDrawer = ref<boolean>(false);

  function linkBug() {
    showLinkBugDrawer.value = true;
  }

  const drawerLoading = ref<boolean>(false);
  async function saveHandler(params: TableQueryParams) {
    try {
      drawerLoading.value = true;
      await associatedDebug(params);
      Message.success(t('caseManagement.featureCase.associatedSuccess'));
      loadBugList();
      showLinkBugDrawer.value = false;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      drawerLoading.value = false;
    }
  }

  const bugTotal = ref<number>(0);
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
    bugTotal.value = res.total;
  }

  onBeforeMount(() => {
    loadBugList();
    initBugList();
  });
</script>

<style lang="less" scoped>
  .bug-item {
    @apply cursor-pointer;
    &:not(:last-child) {
      margin-bottom: 8px;
    }

    padding: 8px;
    border: 1px solid var(--color-text-n8);
    border-radius: var(--border-radius-small);
    background-color: var(--color-text-fff);
    &:hover {
      @apply relative;

      background: var(--color-text-n9);
      box-shadow: inset 0 0 0.5px 0.5px rgb(var(--primary-5));
    }
  }
</style>
