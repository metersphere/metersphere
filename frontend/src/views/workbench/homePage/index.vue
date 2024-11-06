<template>
  <div class="work-bench-content">
    <div class="header-setting">
      <div class="setting flex items-center justify-between">
        <div class="flex items-center gap-[8px]">
          <a-radio-group v-model:model-value="timeType" type="button" size="medium" @change="handleChangeTime">
            <a-radio value="3" class="show-type-icon p-[2px]">
              {{ t('workbench.homePage.nearlyThreeDays') }}
            </a-radio>
            <a-radio value="7" class="show-type-icon p-[2px]">
              {{ t('workbench.homePage.nearlySevenDays') }}
            </a-radio>
            <a-radio value="customize" class="show-type-icon p-[2px]">
              {{ t('workbench.homePage.customize') }}
            </a-radio>
          </a-radio-group>
          <a-range-picker
            v-if="timeType === 'customize'"
            show-time
            value-format="timestamp"
            :time-picker-props="{
              defaultValue: tempRange,
            }"
            class="w-[360px]"
            @select="onSelect"
          />
        </div>
        <a-button type="outline" class="arco-btn-outline--secondary !px-[8px]">
          <template #icon>
            <icon-settings class="setting-icon" @click="handleShowSetting" />
          </template>
          {{ t('workbench.homePage.cardSetting') }}
        </a-button>
      </div>
    </div>
    <!-- TODO 等待卡片设置列表排版出来调整 -->
    <div class="card-content">
      <div class="card-item">
        <Overview :title="t('workbench.homePage.overview')" />
      </div>
      <div class="card-item">
        <Overview :title="t('workbench.homePage.createdByMe')" />
      </div>
      <div class="card-item">
        <OverviewMember />
      </div>
      <div class="card-item">
        <CaseCount />
      </div>
      <div class="card-item">
        <RelatedCaseCount />
      </div>
      <div class="card-item">
        <CaseReviewedCount />
      </div>
      <div class="card-item">
        <WaitReviewList />
      </div>
      <div class="card-item">
        <ApiAndScenarioCase :type="WorkCardEnum.API_CASE_COUNT" />
      </div>
      <div class="card-item">
        <ApiAndScenarioCase :type="WorkCardEnum.SCENARIO_COUNT" />
      </div>
      <div class="card-item">
        <ApiChangeList />
      </div>
      <div class="card-item">
        <DefectMemberBar />
      </div>
      <div class="card-item">
        <DefectCount :type="WorkCardEnum.HANDLE_BUG_BY_ME" />
      </div>
      <div class="card-item">
        <DefectCount :type="WorkCardEnum.CREATE_BUG_BY_ME" />
      </div>
      <div class="card-item">
        <DefectCount :type="WorkCardEnum.BUG_COUNT" />
      </div>
      <div class="card-item">
        <DefectCount :type="WorkCardEnum.PLAN_LEGACY_BUG" />
      </div>
      <div class="card-item">
        <ApiCount />
      </div>
      <div class="card-item">
        <TestPlanCount />
      </div>
    </div>
  </div>
  <MsBackButton target=".page-content" />
</template>

<script setup lang="ts">
  import { ref } from 'vue';

  import MsBackButton from '@/components/pure/ms-back-button/index.vue';
  import ApiAndScenarioCase from './components/apiAndScenarioCase.vue';
  import ApiChangeList from './components/apiChangeList.vue';
  import ApiCount from './components/apiCount.vue';
  import CaseCount from './components/caseCount.vue';
  import CaseReviewedCount from './components/caseReviewedCount.vue';
  import DefectCount from './components/defectCount.vue';
  import Overview from './components/overview.vue';
  import RelatedCaseCount from './components/relatedCaseCount.vue';
  import TestPlanCount from './components/testPlanCount.vue';
  import WaitReviewList from './components/waitReviewList.vue';
  import DefectMemberBar from '@/views/workbench/homePage/components/defectMemberBar.vue';
  import OverviewMember from '@/views/workbench/homePage/components/overviewMember.vue';

  import { useI18n } from '@/hooks/useI18n';

  import { WorkCardEnum } from '@/enums/workbenchEnum';

  const { t } = useI18n();

  // 显示设置
  function handleShowSetting() {}

  const timeType = ref('3');
  // 改变时间
  function handleChangeTime() {}
  const tempRange = ref<(Date | string | number)[]>(['00:00:00', '00:00:00']);

  function onSelect(value: (Date | string | number | undefined)[]) {}
</script>

<style scoped lang="less">
  .setting {
    .setting-icon {
      color: var(--color-text-4);
      background-color: var(--color-text-10);
      cursor: pointer;
      &:hover {
        color: rgba(var(--primary-5));
      }
    }
  }
</style>

<style lang="less">
  .card-wrapper {
    margin: 16px 0;
    padding: 24px;
    box-shadow: 0 0 10px rgba(120 56 135/ 5%);
    @apply rounded-xl bg-white;
    .title {
      font-size: 16px;
      @apply font-medium;
    }
    .case-count-wrapper {
      @apply flex items-center gap-4;
      .case-count-item {
        @apply flex-1;
      }
    }
  }
</style>
