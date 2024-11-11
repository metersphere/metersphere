<template>
  <div class="work-bench-content">
    <div class="header-setting pb-[4px]">
      <div class="setting flex items-center justify-between">
        <div class="flex items-center gap-[8px]">
          <a-radio-group
            v-model:model-value="timeForm.dayNumber"
            type="button"
            size="medium"
            @change="handleChangeTime"
          >
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
            v-if="timeForm.dayNumber === 'customize'"
            v-model:model-value="rangeTime"
            show-time
            value-format="timestamp"
            :time-picker-props="{
              defaultValue: tempRange,
            }"
            class="w-[360px]"
            @select="handleTimeSelect"
          />
        </div>
        <a-button type="outline" class="arco-btn-outline--secondary !px-[8px]" @click="cardSetting">
          <template #icon>
            <icon-settings class="setting-icon" @click="handleShowSetting" />
          </template>
          {{ t('workbench.homePage.cardSetting') }}
        </a-button>
      </div>
    </div>
    <!-- TODO 等待卡片设置列表排版出来调整 -->
    <div v-if="defaultWorkList.length" class="card-content grid grid-cols-2 gap-4">
      <div v-for="item of defaultWorkList" :key="item.id" :class="`card-item ${item.fullScreen ? 'col-span-2' : ''}`">
        <Overview
          v-if="[WorkCardEnum.CREATE_BY_ME, WorkCardEnum.PROJECT_VIEW].includes(item.key)"
          :title="item.label"
        />
        <OverviewMember v-else-if="item.key === WorkCardEnum.PROJECT_MEMBER_VIEW" />
        <CaseCount v-else-if="item.key === WorkCardEnum.CASE_COUNT" />
        <RelatedCaseCount v-else-if="item.key === WorkCardEnum.ASSOCIATE_CASE_COUNT" />
        <CaseReviewedCount v-else-if="item.key === WorkCardEnum.REVIEW_CASE_COUNT" />
        <WaitReviewList v-else-if="item.key === WorkCardEnum.REVIEWING_BY_ME" />
        <ApiAndScenarioCase
          v-else-if="[WorkCardEnum.API_CASE_COUNT, WorkCardEnum.SCENARIO_COUNT].includes(item.key)"
          :type="item.key"
        />
        <ApiChangeList v-else-if="item.key === WorkCardEnum.API_CHANGE" />
        <DefectMemberBar v-else-if="item.key === WorkCardEnum.BUG_HANDLE_USER" />
        <DefectCount v-else-if="countOfBug.includes(item.key)" :type="item.key" />
        <ApiCount v-else-if="item.key === WorkCardEnum.API_COUNT" />
        <TestPlanCount v-else-if="item.key === WorkCardEnum.TEST_PLAN_COUNT" />
      </div>
    </div>
    <NoData v-else all-screen />
  </div>
  <CardSettingDrawer v-model:visible="showSettingDrawer" />
  <MsBackButton target=".page-content" />
</template>

<script setup lang="ts">
  import { ref } from 'vue';

  import MsBackButton from '@/components/pure/ms-back-button/index.vue';
  import NoData from '../components/notData.vue';
  import ApiAndScenarioCase from './components/apiAndScenarioCase.vue';
  import ApiChangeList from './components/apiChangeList.vue';
  import ApiCount from './components/apiCount.vue';
  import CardSettingDrawer from './components/cardSettingDrawer.vue';
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
  import { useUserStore } from '@/store';
  import { getGenerateId } from '@/utils';
  import { getLocalStorage, setLocalStorage } from '@/utils/local-storage';

  import { SelectedCardItem } from '@/models/workbench/homePage';
  import { WorkCardEnum } from '@/enums/workbenchEnum';

  const userStore = useUserStore();

  const { t } = useI18n();

  // 显示设置
  function handleShowSetting() {}

  const rangeTime = ref<number[]>([]);
  const tempRange = ref<(Date | string | number)[]>(['00:00:00', '00:00:00']);
  const initTime = {
    dayNumber: '3',
    startTime: 0,
    endTime: 0,
  };
  const timeForm = ref({ ...initTime });

  function resetTime() {
    timeForm.value = { ...initTime };
    rangeTime.value = [];
  }

  // 改变时间类型
  function handleChangeTime(value: string | number | boolean, ev: Event) {
    resetTime();
    timeForm.value.dayNumber = value as string;
    setLocalStorage(`WORK_TIME_${userStore.id}`, JSON.stringify(timeForm.value));
  }
  // 改变时间
  function handleTimeSelect(value: (Date | string | number | undefined)[]) {
    if (value) {
      timeForm.value.startTime = 0;
      timeForm.value.endTime = 0;
      const start = (value as number[])[0];
      const end = (value as number[])[1];
      tempRange.value = [end, start];
      timeForm.value.startTime = start;
      timeForm.value.endTime = end;
      setLocalStorage(`WORK_TIME_${userStore.id}`, JSON.stringify(timeForm.value));
    }
  }
  const showSettingDrawer = ref(false);

  // 卡片设置
  function cardSetting() {
    showSettingDrawer.value = true;
  }

  const countOfBug = [
    WorkCardEnum.HANDLE_BUG_BY_ME,
    WorkCardEnum.CREATE_BUG_BY_ME,
    WorkCardEnum.BUG_COUNT,
    WorkCardEnum.PLAN_LEGACY_BUG,
  ];

  const defaultWorkList = ref<SelectedCardItem[]>([]);

  function initDefaultList() {
    if (userStore.isAdmin) {
      defaultWorkList.value = [
        {
          id: getGenerateId(),
          label: t('workbench.homePage.projectOverview'),
          key: WorkCardEnum.PROJECT_VIEW,
          fullScreen: true,
          isDisabledHalfScreen: true,
          projectIds: [],
          handleUsers: [],
        },
        {
          id: getGenerateId(),
          label: t('workbench.homePage.staffOverview'),
          key: WorkCardEnum.PROJECT_MEMBER_VIEW,
          fullScreen: true,
          isDisabledHalfScreen: true,
          projectIds: [],
          handleUsers: [],
        },
      ];
    }
  }

  onMounted(() => {
    initDefaultList();
    const defaultTime = getLocalStorage(`WORK_TIME_${userStore.id}`);
    if (!defaultTime) {
      setLocalStorage(`WORK_TIME_${userStore.id}`, JSON.stringify(timeForm.value));
    } else {
      timeForm.value = JSON.parse(defaultTime);
      const { startTime, endTime } = timeForm.value;
      rangeTime.value = [startTime, endTime];
    }
  });
</script>

<style scoped lang="less">
  .work-bench-content {
    .header-setting {
      position: sticky;
      top: 0;
      z-index: 999;
      background: var(--color-text-n9);
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
