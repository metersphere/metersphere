<template>
  <div class="p-[16px]">
    <a-input-search
      v-model:model-value="searchKeyword"
      :placeholder="t('workbench.homePage.searchCard')"
      allow-clear
      @clear="resetSearch"
    />
  </div>

  <div class="card-config-menu-wrapper">
    <a-menu v-if="filteredConfigList.length" class="w-full" :default-open-keys="defaultOpenKeys">
      <a-sub-menu v-for="item of filteredConfigList" :key="item.value">
        <template #title>
          <div class="font-medium text-[var(--color-text-1)]">
            {{ t(item.label) }}
          </div>
        </template>

        <a-menu-item v-for="ele of item.children" :key="ele.value" class="card-config-item" @click="addCard(ele)">
          <div class="card-config-menu-item">
            <div class="card-config-img">
              <svg-icon width="98px" height="69px" :name="getImgType(ele)" />
            </div>
            <div class="card-config-text">
              <div>{{ t(ele.label) }}</div>
              <div class="card-config-desc flex">
                <div>{{ t(ele.description || '') }}</div>
              </div>
            </div>
          </div>
        </a-menu-item>
      </a-sub-menu>
    </a-menu>
    <div v-else class="p-[16px]">
      <div
        class="rounded-[var(--border-radius-small)] bg-[var(--color-fill-1)] p-[8px] text-[12px] leading-[16px] text-[var(--color-text-4)]"
      >
        {{ t('common.noData') }}
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { ref } from 'vue';

  import { useI18n } from '@/hooks/useI18n';
  import { useAppStore } from '@/store';

  import { childrenWorkConfigItem, WorkConfigCard } from '@/models/workbench/homePage';
  import { WorkCardEnum } from '@/enums/workbenchEnum';

  const { t } = useI18n();
  const appStore = useAppStore();
  const defaultOpenKeys = ref(['overview', 'caseManagement', 'apiTest', 'testPlan', 'bugManagement']);

  const emit = defineEmits<{
    (e: 'add', item: childrenWorkConfigItem): void;
  }>();

  const innerSelectedIds = defineModel<WorkCardEnum[]>('selectedIds', {
    required: true,
  });

  const configList = ref<WorkConfigCard[]>([
    // 概览
    {
      label: 'workbench.homePage.overview',
      value: 'overview',
      description: '',
      img: '',
      children: [
        {
          label: 'workbench.homePage.projectOverview',
          value: WorkCardEnum.PROJECT_VIEW,
          description: 'workbench.homePage.projectOverviewDesc',
          brightImg: 'project-overview-img',
          darkImg: 'project-overview-dark-img',
        },
        {
          label: 'workbench.homePage.testPlanOverview',
          value: WorkCardEnum.PROJECT_PLAN_VIEW,
          description: 'workbench.homePage.testPlanOverviewDesc',
          brightImg: 'test-plan-overview',
          darkImg: 'test-plan-overview-dark',
        },
        {
          label: 'workbench.homePage.staffOverview',
          value: WorkCardEnum.PROJECT_MEMBER_VIEW,
          description: 'workbench.homePage.staffOverviewDesc',
          brightImg: 'staff-overview-img',
          darkImg: 'staff-overview-dark-img',
        },
        {
          label: 'workbench.homePage.createdByMe',
          value: WorkCardEnum.CREATE_BY_ME,
          description: 'workbench.homePage.createdByMeDesc',
          brightImg: 'my-created-project-img',
          darkImg: 'my-created-project-dark-img',
        },
      ],
    },
    // 测试用例
    {
      label: 'menu.caseManagement',
      value: 'caseManagement',
      description: '',
      img: '',
      children: [
        {
          label: 'workbench.homePage.useCasesNumber',
          value: WorkCardEnum.CASE_COUNT,
          description: 'workbench.homePage.useCasesNumberDesc',
          brightImg: 'link-case-img',
          darkImg: 'link-case-dark-img',
        },
        {
          label: 'workbench.homePage.useCasesCount',
          value: WorkCardEnum.ASSOCIATE_CASE_COUNT,
          description: 'workbench.homePage.useCasesCountDesc',
          brightImg: 'case-count-img',
          darkImg: 'case-count-dark-img',
        },
        {
          label: 'workbench.homePage.numberOfCaseReviews',
          value: WorkCardEnum.REVIEW_CASE_COUNT,
          description: 'workbench.homePage.numberOfCaseReviewsDesc',
          brightImg: 'case-review-img',
          darkImg: 'case-review-dark-img',
        },
        {
          label: 'workbench.homePage.waitForReview',
          value: WorkCardEnum.REVIEWING_BY_ME,
          description: 'workbench.homePage.waitForReviewDesc',
          brightImg: 'wait-review-img',
          darkImg: 'wait-review-dark-img',
        },
      ],
    },
    // 接口测试
    {
      label: 'menu.apiTest',
      value: 'apiTest',
      description: '',
      img: '',
      children: [
        {
          label: 'workbench.homePage.apiCount',
          value: WorkCardEnum.API_COUNT,
          description: 'workbench.homePage.apiCountDesc',
          brightImg: 'api-count-img',
          darkImg: 'api-count-dark-img',
        },
        {
          label: 'workbench.homePage.apiUseCasesNumber',
          value: WorkCardEnum.API_CASE_COUNT,
          description: 'workbench.homePage.apiUseCasesNumberDesc',
          brightImg: 'api-use-case-img',
          darkImg: 'api-use-case-dark-img',
        },
        {
          label: 'workbench.homePage.scenarioUseCasesNumber',
          value: WorkCardEnum.SCENARIO_COUNT,
          description: 'workbench.homePage.scenarioUseCasesNumberDesc',
          brightImg: 'scenario-case-img',
          darkImg: 'scenario-case-dark-img',
        },
        {
          label: 'workbench.homePage.interfaceChange',
          value: WorkCardEnum.API_CHANGE,
          description: 'workbench.homePage.interfaceChangeDesc',
          brightImg: 'api-change-img',
          darkImg: 'api-change-dark-img',
        },
      ],
    },
    // 测试计划
    {
      label: 'menu.testPlan',
      value: 'testPlan',
      description: '',
      img: '',
      children: [
        {
          label: 'workbench.homePage.numberOfTestPlan',
          value: WorkCardEnum.TEST_PLAN_COUNT,
          description: 'workbench.homePage.numberOfTestPlanDesc',
          brightImg: 'test-plan-img',
          darkImg: 'test-plan-dark-img',
        },
        {
          label: 'workbench.homePage.remainingBugOfPlan',
          value: WorkCardEnum.PLAN_LEGACY_BUG,
          description: 'workbench.homePage.remainingBugOfPlanDesc',
          brightImg: 'test-plan-bug-img',
          darkImg: 'test-plan-bug-dark-img',
        },
      ],
    },
    // 缺陷管理
    {
      label: 'menu.bugManagement',
      value: 'bugManagement',
      description: '',
      img: '',
      children: [
        {
          label: 'workbench.homePage.bugCount',
          value: WorkCardEnum.BUG_COUNT,
          description: 'workbench.homePage.bugCountDesc',
          brightImg: 'bug-count-img',
          darkImg: 'bug-count-dark-img',
        },
        {
          label: 'workbench.homePage.createdBugByMe',
          value: WorkCardEnum.CREATE_BUG_BY_ME,
          description: 'workbench.homePage.createdBugByMeDesc',
          brightImg: 'my-created-bug-img',
          darkImg: 'my-created-bug-dark-img',
        },
        {
          label: 'workbench.homePage.pendingDefect',
          value: WorkCardEnum.HANDLE_BUG_BY_ME,
          description: 'workbench.homePage.pendingDefectDesc',
          brightImg: 'wait-handle-bug-img',
          darkImg: 'wait-handle-bug-dark-img',
        },
        {
          label: 'workbench.homePage.defectProcessingNumber',
          value: WorkCardEnum.BUG_HANDLE_USER,
          description: 'workbench.homePage.defectProcessingNumberDesc',
          brightImg: 'bug-handler-img',
          darkImg: 'bug-handler-dark-img',
        },
      ],
    },
  ]);

  const searchKeyword = ref(''); // 存储搜索关键字

  const filteredConfigList = computed(() => {
    return configList.value.map((item) => ({
      ...item,
      children: item.children.filter(
        (child) =>
          t(child.label).toLowerCase().includes(searchKeyword.value.toLowerCase()) &&
          !innerSelectedIds.value.includes(child.value)
      ),
    }));
  });

  function getImgType(ele: childrenWorkConfigItem) {
    const key = appStore.isDarkTheme ? 'darkImg' : 'brightImg';
    return ele[key];
  }

  // 重置
  function resetSearch() {
    searchKeyword.value = '';
  }

  function addCard(item: childrenWorkConfigItem) {
    emit('add', item);
  }
</script>

<style scoped lang="less">
  .card-config-menu-wrapper {
    height: calc(100vh - 148px);
    @apply overflow-y-auto overflow-x-hidden;
    .ms-scroll-bar();
    :deep(.card-config-item) {
      padding: 0;
      background: var(--color-text-fff);
    }
    .card-config-menu-item {
      padding: 8px 16px 8px 10px;
      @apply flex gap-2;
      .card-config-img {
        width: 98px;
        height: 69px;
        border: 1px solid var(--color-text-n8);
        border-radius: 6px;
      }
      .card-config-text {
        line-height: 22px;
        @apply flex flex-col justify-start;
        .card-config-desc {
          /* stylelint-disable-next-line value-no-vendor-prefix */
          display: -webkit-box !important;
          overflow: hidden;
          font-size: 12px;
          text-overflow: ellipsis;
          word-wrap: break-word; /* 在单词边界换行 */
          white-space: normal; /* 允许正常换行 */
          color: var(--color-text-4);
          -webkit-line-clamp: 2 !important;
          -webkit-box-orient: vertical !important;
        }
      }
    }
  }
  :deep(.arco-menu-light) {
    background: none !important;
    .arco-menu-inner {
      padding: 0 !important;
      .arco-menu-inline-header {
        &:hover {
          background: var(--color-text-fff) !important;
        }
      }
      .arco-menu-selected {
        font-weight: normal !important;
        color: var(--color-text-1) !important;
        &:not(.arco-menu-inline-header) {
          background-color: var(--color-text-fff) !important;
        }
      }
    }
    .arco-menu-item {
      :hover {
        background: var(--color-text-n9) !important;
      }
    }
  }
  :deep(.arco-menu-item-inner) {
    padding: 0;
  }
  :deep(.arco-menu-indent-list) {
    display: none;
  }
  :deep(.arco-menu-inline) {
    .arco-menu-inline-header {
      padding: 0 0 0 8px;
    }
  }
</style>
