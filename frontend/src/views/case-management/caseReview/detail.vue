<template>
  <MsCard :min-width="1100" auto-height hide-footer no-bottom-radius no-content-padding hide-divider>
    <template #headerLeft>
      <a-tooltip :content="reviewDetail.name">
        <div class="one-line-text mr-[8px] max-w-[260px] font-medium text-[var(--color-text-000)]">
          {{ reviewDetail.name }}
        </div>
      </a-tooltip>
      <div
        class="rounded-[0_999px_999px_0] border border-solid border-[text-[rgb(var(--primary-5))]] px-[8px] py-[2px] text-[12px] leading-[16px] text-[rgb(var(--primary-5))]"
      >
        <MsIcon type="icon-icon-contacts" size="13" />
        {{ t('caseManagement.caseReview.single') }}
      </div>
      <statusTag :status="(reviewDetail.status as StatusMap)" class="mx-[16px]" />
      <MsPrevNextButton
        ref="prevNextButtonRef"
        v-model:loading="loading"
        :page-change="pageChange"
        :pagination="pagination"
        :get-detail-func="getDetailFunc"
        :detail-id="route.query.id as string"
        :detail-index="detailIndex"
        :table-data="tableData"
        @loaded="loaded"
      />
    </template>
    <template #headerRight>
      <div class="mr-[16px] flex items-center">
        <a-switch v-model:model-value="onlyMine" size="small" class="mr-[8px]" />
        {{ t('caseManagement.caseReview.onlyMine') }}
      </div>
      <MsButton type="button" status="default">
        <MsIcon type="icon-icon_link-record_outlined1" class="mr-[8px]" />
        {{ t('ms.case.associate.title') }}
      </MsButton>
      <MsButton type="button" status="default">
        <MsIcon type="icon-icon_edit_outlined" class="mr-[8px]" />
        {{ t('common.edit') }}
      </MsButton>
      <MsButton type="button" status="default">
        <MsIcon type="icon-icon_copy_outlined" class="mr-[8px]" />
        {{ t('common.copy') }}
      </MsButton>
      <MsButton type="button" status="default">
        <MsIcon type="icon-icon_collection_outlined" class="mr-[8px]" />
        {{ t('common.fork') }}
      </MsButton>
      <MsTableMoreAction :list="moreAction">
        <MsButton type="button" status="default">
          <MsIcon type="icon-icon_more_outlined" class="mr-[8px]" />
          {{ t('common.more') }}
        </MsButton>
      </MsTableMoreAction>
    </template>
    <template #subHeader>
      <div class="mt-[16px] w-[476px]">
        <div class="mb-[4px] flex items-center gap-[24px]">
          <div class="text-[var(--color-text-4)]">
            <span class="mr-[8px]">{{ t('caseManagement.caseReview.reviewedCase') }}</span>
            <span v-if="reviewDetail.status === 0" class="text-[var(--color-text-1)]">-</span>
            <span v-else>
              <span class="text-[var(--color-text-1)]">{{ reviewDetail.reviewCount }}/</span
              >{{ reviewDetail.caseCount }}
            </span>
          </div>
          <div class="text-[var(--color-text-4)]">
            <span class="mr-[8px]">{{ t('caseManagement.caseReview.passRate') }}</span>
            <span v-if="reviewDetail.status === 0" class="text-[var(--color-text-1)]">-</span>
            <span v-else>
              <span class="text-[var(--color-text-1)]">
                {{ ((reviewDetail.reviewCount / reviewDetail.caseCount) * 100).toFixed(2) }}%
              </span>
            </span>
          </div>
        </div>
        <passRateLine :review-detail="reviewDetail" height="8px" radius="var(--border-radius-mini)" />
      </div>
    </template>
    <div class="px-[24px]">
      <a-divider class="my-0" />
      <a-tabs v-model:active-key="showTab" class="no-content">
        <a-tab-pane v-for="item of tabList" :key="item.key" :title="item.title" />
      </a-tabs>
    </div>
  </MsCard>
  <MsCard class="mt-[16px]" :special-height="180" simple has-breadcrumb no-content-padding>
    <MsSplitBox>
      <template #left>
        <div class="p-[24px]">
          <CaseTree ref="folderTreeRef" @folder-node-select="handleFolderNodeSelect" />
        </div>
      </template>
      <template #right>
        <CaseTable :active-folder="activeFolderId"></CaseTable>
      </template>
    </MsSplitBox>
  </MsCard>
</template>

<script setup lang="ts">
  /**
   * @description 功能测试-用例评审-评审详情
   */
  import { useRoute, useRouter } from 'vue-router';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsCard from '@/components/pure/ms-card/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsSplitBox from '@/components/pure/ms-split-box/index.vue';
  import MsTableMoreAction from '@/components/pure/ms-table-more-action/index.vue';
  import { ActionsItem } from '@/components/pure/ms-table-more-action/types';
  import MsPrevNextButton from '@/components/business/ms-prev-next-button/index.vue';
  import CaseTable from './components/detail/caseTable.vue';
  import CaseTree from './components/detail/caseTree.vue';
  import passRateLine from './components/passRateLine.vue';
  import statusTag, { StatusMap } from './components/statusTag.vue';

  import { useI18n } from '@/hooks/useI18n';

  const router = useRouter();
  const route = useRoute();
  const { t } = useI18n();

  const loading = ref(false);
  const reviewDetail = ref({
    name: '具体的用例评审的名称，最大宽度260px,超过展示省略号啦',
    status: 2,
    caseCount: 100,
    passCount: 0,
    failCount: 10,
    reviewCount: 20,
    reviewingCount: 25,
  });

  const onlyMine = ref(false);
  const moreAction = ref<ActionsItem[]>([]);
  const fullActions = [
    {
      label: t('caseManagement.caseReview.archive'),
      eventTag: 'archive',
      icon: 'icon-icon-draft',
    },
    {
      label: t('common.export'),
      eventTag: 'export',
      icon: 'icon-icon_upload_outlined',
    },
    {
      label: t('caseManagement.caseReview.createTestPlan'),
      eventTag: 'createTestPlan',
      icon: 'icon-icon_add_outlined-1',
    },
    {
      isDivider: true,
    },
    {
      label: t('common.delete'),
      eventTag: 'delete',
      icon: 'icon-icon_delete-trash_outlined1',
      danger: true,
    },
  ];

  onBeforeMount(() => {
    if (reviewDetail.value.status === 2) {
      moreAction.value = [...fullActions];
    } else if (reviewDetail.value.status === 3) {
      moreAction.value = fullActions.filter((e) => e.eventTag === 'delete');
    } else {
      moreAction.value = fullActions.filter((e) => e.eventTag !== 'archive');
    }
  });

  const showTab = ref(0);
  const tabList = ref([
    {
      key: 0,
      title: t('menu.caseManagement.featureCase'),
    },
  ]);

  const pagination = ref({
    current: 1,
    pageSize: 10,
    total: 0,
  });
  const detailIndex = ref(0);
  const tableData = ref([]);

  async function getDetailFunc() {
    console.log('getDetailFunc');
  }

  async function pageChange() {
    console.log('page');
  }

  function loaded(e: any) {
    loading.value = false;
    reviewDetail.value = e;
  }

  const folderTreeRef = ref<InstanceType<typeof CaseTree>>();
  const activeFolderId = ref<string | number>('all');

  function handleFolderNodeSelect(ids: (string | number)[]) {
    [activeFolderId.value] = ids;
  }
</script>

<style lang="less" scoped>
  :deep(.arco-tabs-content) {
    @apply hidden;
  }
</style>
