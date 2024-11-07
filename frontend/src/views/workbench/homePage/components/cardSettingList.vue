<template>
  <div class="p-[16px]">
    <a-input-search
      v-model:model-value="searchKeyword"
      :placeholder="t('workbench.homePage.searchCard')"
      allow-clear
      @search="searchList"
      @press-enter="searchList"
      @clear="resetSearch"
    />
  </div>

  <div class="card-config-menu-wrapper">
    <a-menu class="w-full" :default-open-keys="defaultOpenKeys">
      <a-sub-menu v-for="item of filteredConfigList" :key="item.value">
        <template #title>
          <div class="font-medium text-[var(--color-text-1)]">
            {{ item.label }}
          </div>
        </template>

        <a-menu-item v-for="ele of item.children" :key="ele.value" class="card-config-item" @click="addCard(ele)">
          <div class="card-config-menu-item">
            <div class="card-config-img">
              <svg-icon width="98px" height="69px" :name="ele.img" />
            </div>
            <div class="card-config-text">
              <div>{{ ele.label }}</div>
              <div class="card-config-desc flex">
                <div>{{ ele.description }}</div>
              </div>
            </div>
          </div>
        </a-menu-item>
      </a-sub-menu>
    </a-menu>
  </div>
</template>

<script setup lang="ts">
  import { ref } from 'vue';

  import { useI18n } from '@/hooks/useI18n';

  import { childrenWorkConfigItem, WorkConfigCard } from '@/models/workbench/homePage';
  import { WorkCardEnum } from '@/enums/workbenchEnum';

  const { t } = useI18n();
  const defaultOpenKeys = ref(['overview', 'caseManagement', 'apiTest', 'testPlan', 'bugManagement']);

  const emit = defineEmits<{
    (e: 'add', item: childrenWorkConfigItem): void;
  }>();

  const configList = ref<WorkConfigCard[]>([
    {
      label: t('workbench.homePage.overview'),
      value: 'overview',
      description: '',
      img: '',
      children: [
        {
          label: t('workbench.homePage.projectOverview'),
          value: WorkCardEnum.PROJECT_VIEW,
          description: t('workbench.homePage.projectOverviewDesc'),
          img: 'project-overview-img',
        },
        {
          label: t('workbench.homePage.staffOverview'),
          value: WorkCardEnum.PROJECT_MEMBER_VIEW,
          description: t('workbench.homePage.staffOverviewDesc'),
          img: 'staff-overview-img',
        },
        {
          label: t('workbench.homePage.createdByMe'),
          value: WorkCardEnum.CREATE_BY_ME,
          description: t('workbench.homePage.createdByMeDesc'),
          img: 'my-created-project-img',
        },
      ],
    },
    {
      label: t('menu.caseManagement'),
      value: 'caseManagement',
      description: '',
      img: '',
      children: [
        {
          label: t('workbench.homePage.useCasesNumber'),
          value: WorkCardEnum.CASE_COUNT,
          description: t('workbench.homePage.useCasesNumberDesc'),
          img: 'link-case-img',
        },
        {
          label: t('workbench.homePage.useCasesCount'),
          value: WorkCardEnum.ASSOCIATE_CASE_COUNT,
          description: t('workbench.homePage.useCasesCountDesc'),
          img: 'case-count-img',
        },
        {
          label: t('workbench.homePage.numberOfCaseReviews'),
          value: WorkCardEnum.REVIEW_CASE_COUNT,
          description: t('workbench.homePage.numberOfCaseReviewsDesc'),
          img: 'case-review-img',
        },
        {
          label: t('workbench.homePage.waitForReview'),
          value: WorkCardEnum.REVIEWING_BY_ME,
          description: t('workbench.homePage.waitForReviewDesc'),
          img: 'wait-review-img',
        },
      ],
    },
    {
      label: t('menu.apiTest'),
      value: 'apiTest',
      description: '',
      img: '',
      children: [
        {
          label: t('workbench.homePage.apiCount'),
          value: WorkCardEnum.API_COUNT,
          description: t('workbench.homePage.apiCountDesc'),
          img: 'api-count-img',
        },
        {
          label: t('workbench.homePage.apiUseCasesNumber'),
          value: WorkCardEnum.API_CASE_COUNT,
          description: t('workbench.homePage.apiUseCasesNumberDesc'),
          img: 'api-use-case-img',
        },
        {
          label: t('workbench.homePage.scenarioUseCasesNumber'),
          value: WorkCardEnum.SCENARIO_COUNT,
          description: t('workbench.homePage.scenarioUseCasesNumberDesc'),
          img: 'scenario-case-img',
        },
        {
          label: t('workbench.homePage.interfaceChange'),
          value: WorkCardEnum.API_CHANGE,
          description: t('workbench.homePage.interfaceChangeDesc'),
          img: 'api-change-img',
        },
      ],
    },
    {
      label: t('menu.testPlan'),
      value: 'testPlan',
      description: '',
      img: '',
      children: [
        {
          label: t('workbench.homePage.numberOfTestPlan'),
          value: WorkCardEnum.TEST_PLAN_COUNT,
          description: t('workbench.homePage.numberOfTestPlanDesc'),
          img: 'test-plan-img',
        },
        {
          label: t('workbench.homePage.remainingBugOfPlan'),
          value: WorkCardEnum.PLAN_LEGACY_BUG,
          description: t('workbench.homePage.remainingBugOfPlanDesc'),
          img: 'test-plan-bug-img',
        },
      ],
    },
    {
      label: t('menu.bugManagement'),
      value: 'bugManagement',
      description: '',
      img: '',
      children: [
        {
          label: t('workbench.homePage.bugCount'),
          value: WorkCardEnum.BUG_COUNT,
          description: t('workbench.homePage.bugCountDesc'),
          img: 'bug-count-img',
        },
        {
          label: t('workbench.homePage.createdBugByMe'),
          value: WorkCardEnum.CREATE_BUG_BY_ME,
          description: t('workbench.homePage.createdBugByMeDesc'),
          img: 'my-created-bug-img',
        },
        {
          label: t('workbench.homePage.pendingDefect'),
          value: WorkCardEnum.HANDLE_BUG_BY_ME,
          description: t('workbench.homePage.pendingDefectDesc'),
          img: 'wait-handle-bug-img',
        },
        {
          label: t('workbench.homePage.defectProcessingNumber'),
          value: WorkCardEnum.BUG_HANDLE_USER,
          description: t('workbench.homePage.defectProcessingNumberDesc'),
          img: 'bug-handler-img',
        },
      ],
    },
  ]);

  const searchKeyword = ref(''); // 存储搜索关键字

  const filteredConfigList = computed(() => {
    if (!searchKeyword.value) {
      return configList.value;
    }
    return configList.value
      .map((item) => ({
        ...item,
        children: item.children.filter((child) =>
          child.label.toLowerCase().includes(searchKeyword.value.toLowerCase())
        ),
      }))
      .filter((item) => item.children.length > 0);
  });

  // 搜索
  function searchList(keyword: string) {
    searchKeyword.value = keyword;
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
      background: white;
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
          background: white !important;
        }
      }
      .arco-menu-selected {
        font-weight: normal !important;
        color: var(--color-text-1) !important;
        &:not(.arco-menu-inline-header) {
          background-color: white !important;
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
