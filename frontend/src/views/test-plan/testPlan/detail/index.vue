<template>
  <MsCard
    :loading="loading"
    :header-min-width="1100"
    :min-width="150"
    auto-height
    hide-footer
    no-content-padding
    hide-divider
  >
    <template #headerLeft>
      <MsStatusTag :status="detail.status" />
      <a-tooltip :content="`[${detail.num}]${detail.name}`">
        <div class="one-line-text ml-[4px] max-w-[360px] gap-[4px] font-medium text-[var(--color-text-1)]">
          <span>[{{ detail.num }}]</span>
          {{ detail.name }}
        </div>
      </a-tooltip>
    </template>
    <template #headerRight>
      <MsButton v-permission="['PROJECT_TEST_PLAN:READ+ASSOCIATION']" type="button" status="default">
        <MsIcon type="icon-icon_link-record_outlined1" class="mr-[8px]" />
        {{ t('ms.case.associate.title') }}
      </MsButton>
      <MsButton v-permission="['PROJECT_TEST_PLAN:READ+UPDATE']" type="button" status="default">
        <MsIcon type="icon-icon_edit_outlined" class="mr-[8px]" />
        {{ t('common.edit') }}
      </MsButton>
      <MsButton v-permission="['PROJECT_TEST_PLAN:READ+ADD']" type="button" status="default">
        <MsIcon type="icon-icon_copy_outlined" class="mr-[8px]" />
        {{ t('common.copy') }}
      </MsButton>
      <MsButton v-permission="['PROJECT_TEST_PLAN:READ+UPDATE']" type="button" status="default">
        <MsIcon
          :type="detail.followFlag ? 'icon-icon_collect_filled' : 'icon-icon_collection_outlined'"
          :class="`mr-[8px] ${detail.followFlag ? 'text-[rgb(var(--warning-6))]' : ''}`"
        />
        {{ t(detail.followFlag ? 'common.forked' : 'common.fork') }}
      </MsButton>
      <MsTableMoreAction :list="moreAction" @select="handleMoreSelect">
        <MsButton v-permission="['PROJECT_TEST_PLAN:READ+DELETE']" type="button" status="default">
          <MsIcon type="icon-icon_more_outlined" class="mr-[8px]" />
          {{ t('common.more') }}
        </MsButton>
      </MsTableMoreAction>
    </template>
    <template #subHeader>
      <div class="mt-[16px] w-[476px]">
        <div class="mb-[8px] flex items-center gap-[24px] text-[12px]">
          <div class="text-[var(--color-text-4)]">
            <span class="mr-[8px]">{{ t('testPlan.testPlanDetail.executed') }}</span>
            <span v-if="detail.status === 'PREPARED'" class="text-[var(--color-text-1)]">-</span>
            <span v-else>
              <span class="font-medium text-[var(--color-text-1)]"> {{ detail.executedCount }} </span>/{{
                detail.caseCount
              }}
            </span>
          </div>
          <div class="text-[var(--color-text-4)]">
            <span class="mr-[8px]">{{ t('caseManagement.caseReview.passRate') }}</span>
            <span v-if="detail.status === 'PREPARED'" class="text-[var(--color-text-1)]">-</span>
            <span v-else>
              <span class="font-medium text-[var(--color-text-1)]"> {{ detail.passRate }}% </span>
            </span>
          </div>
        </div>
        <passRateLine :review-detail="detail" height="8px" radius="var(--border-radius-mini)" />
      </div>
    </template>
    <a-tabs v-model:active-key="activeTab" class="no-content">
      <a-tab-pane v-for="item of tabList" :key="item.key" :title="item.title" />
    </a-tabs>
  </MsCard>
  <!-- special-height的174: 上面卡片高度158 + mt的16 -->
  <MsCard class="mt-[16px]" :special-height="174" simple has-breadcrumb no-content-padding>
    <BugManagement v-if="activeTab === 'defectList'" :plan-id="detail.id" />
  </MsCard>
</template>

<script setup lang="ts">
  import { computed, onMounted, ref } from 'vue';
  import { useRoute } from 'vue-router';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsCard from '@/components/pure/ms-card/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsTableMoreAction from '@/components/pure/ms-table-more-action/index.vue';
  import { ActionsItem } from '@/components/pure/ms-table-more-action/types';
  import MsStatusTag from '@/components/business/ms-status-tag/index.vue';
  import BugManagement from './bugManagement/index.vue';
  import passRateLine from '@/views/case-management/caseReview/components/passRateLine.vue';

  import { getTestPlanDetail } from '@/api/modules/test-plan/testPlan';
  import { testPlanDefaultDetail } from '@/config/testPlan';
  import { useI18n } from '@/hooks/useI18n';

  import type { TestPlanDetail } from '@/models/testPlan/testPlan';

  const { t } = useI18n();
  const route = useRoute();

  const loading = ref(false);
  const planId = ref(route.query.id as string);
  const detail = ref<TestPlanDetail>({
    ...testPlanDefaultDetail,
  });
  async function initDetail() {
    try {
      loading.value = true;
      detail.value = await getTestPlanDetail(planId.value);
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      loading.value = false;
    }
  }
  onMounted(() => {
    initDetail();
  });

  const fullActions = [
    {
      label: t('common.archive'),
      eventTag: 'archive',
      permission: ['PROJECT_TEST_PLAN:READ+UPDATE'],
    },
    {
      isDivider: true,
    },
    {
      label: t('common.delete'),
      eventTag: 'delete',
      danger: true,
      permission: ['PROJECT_TEST_PLAN:READ+DELETE'],
    },
  ];
  const moreAction = computed(() => {
    if (detail.value.status === 'COMPLETED') {
      return [...fullActions];
    }
    return fullActions.filter((e) => e.eventTag !== 'archive');
  });
  function handleMoreSelect(item: ActionsItem) {
    switch (item.eventTag) {
      case 'archive':
        break;
      case 'delete':
        break;
      default:
        break;
    }
  }

  const activeTab = ref('featureCase');
  const tabList = ref([
    {
      key: 'featureCase',
      title: t('menu.caseManagement.featureCase'),
    },
    {
      key: 'defectList',
      title: t('caseManagement.featureCase.defectList'),
    },
  ]);
</script>

<style lang="less" scoped>
  :deep(.arco-tabs-content) {
    @apply hidden;
  }
</style>
