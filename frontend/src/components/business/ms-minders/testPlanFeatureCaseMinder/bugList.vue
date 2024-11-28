<template>
  <a-spin :loading="bugListLoading" class="block h-full pl-[16px]">
    <MsList
      v-model:data="bugList"
      mode="remote"
      item-key-field="id"
      :item-border="false"
      :no-more-data="false"
      :virtual-list-props="{
        height: '100%',
      }"
      @reach-bottom="handleReachBottom"
    >
      <template #item="{ item }">
        <div class="bug-item">
          <div class="mb-[4px] flex items-center justify-between">
            <MsButton type="text" @click="goBug(item.bugId)">{{ item.num }}</MsButton>
            <MsButton v-if="props.showDisassociateButton" type="text" @click="disassociateBug(item.id)">
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
</template>

<script setup lang="ts">
  import { Message } from '@arco-design/web-vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsList from '@/components/pure/ms-list/index.vue';

  import { associatedBugPage, testPlanCancelBug } from '@/api/modules/test-plan/testPlan';
  import { useI18n } from '@/hooks/useI18n';
  import useOpenNewPage from '@/hooks/useOpenNewPage';
  import useAppStore from '@/store/modules/app';

  import { BugManagementRouteEnum } from '@/enums/routeEnum';

  const props = defineProps<{
    activeCase: Record<string, any>;
    testPlanCaseId: string;
    showDisassociateButton?: boolean;
  }>();

  const emit = defineEmits<{
    (e: 'disassociateBugDone'): void;
  }>();

  const { openNewPage } = useOpenNewPage();
  const appStore = useAppStore();
  const { t } = useI18n();

  const bugList = ref<any[]>([]);
  const bugListLoading = ref(false);
  const pageNation = ref({
    total: 0,
    pageSize: 10,
    current: 1,
  });

  async function loadBugList() {
    try {
      bugListLoading.value = true;
      const res = await associatedBugPage({
        caseId: props.activeCase.id,
        testPlanCaseId: props.testPlanCaseId,
        projectId: appStore.currentProjectId,
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

  // 滚动翻页
  function handleReachBottom() {
    pageNation.value.current += 1;
    if (pageNation.value.current > Math.ceil(pageNation.value.total / pageNation.value.pageSize)) {
      return;
    }
    loadBugList();
  }

  // 取消关联缺陷
  async function disassociateBug(id: string) {
    try {
      await testPlanCancelBug(id);
      bugList.value = bugList.value.filter((item) => item.id !== id);
      Message.success(t('caseManagement.featureCase.cancelLinkSuccess'));
      emit('disassociateBugDone');
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  function goBug(id: string) {
    openNewPage(BugManagementRouteEnum.BUG_MANAGEMENT_INDEX, {
      id,
    });
  }

  onBeforeMount(() => {
    loadBugList();
  });

  defineExpose({
    loadBugList,
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
