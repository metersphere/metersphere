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
            <a-radio :value="3" class="show-type-icon p-[2px]">
              {{ t('workbench.homePage.nearlyThreeDays') }}
            </a-radio>
            <a-radio :value="7" class="show-type-icon p-[2px]">
              {{ t('workbench.homePage.nearlySevenDays') }}
            </a-radio>
            <a-radio value="" class="show-type-icon p-[2px]">
              {{ t('workbench.homePage.customize') }}
            </a-radio>
          </a-radio-group>
          <a-range-picker
            v-if="!timeForm.dayNumber"
            v-model:model-value="rangeTime"
            show-time
            value-format="timestamp"
            :time-picker-props="{
              defaultValue: tempRange,
            }"
            class="w-[360px]"
            @ok="handleTimeSelect"
          />
        </div>
        <div class="flex items-center gap-[8px]">
          <MsTag
            no-margin
            :tooltip-disabled="true"
            class="h-[30px] cursor-pointer"
            theme="outline"
            @click="handleRefresh"
          >
            <MsIcon class="text-[16px] text-[var(color-text-4)]" :size="32" type="icon-icon_reset_outlined" />
          </MsTag>
          <a-button type="outline" class="arco-btn-outline--secondary !px-[8px]" @click="cardSetting">
            <template #icon>
              <icon-settings class="setting-icon" />
            </template>
            {{ t('workbench.homePage.cardSetting') }}
          </a-button>
        </div>
      </div>
    </div>
    <div v-if="defaultWorkList.length" class="card-content mt-[12px] grid grid-cols-2 gap-4">
      <div v-for="item of defaultWorkList" :key="item.id" :class="`card-item ${item.fullScreen ? 'col-span-2' : ''}`">
        <Overview
          v-if="[WorkCardEnum.CREATE_BY_ME, WorkCardEnum.PROJECT_VIEW].includes(item.key)"
          v-model:projectIds="item.projectIds"
          :item="item"
          @change="changeHandler"
        />
        <OverviewMember
          v-else-if="item.key === WorkCardEnum.PROJECT_MEMBER_VIEW"
          v-model:projectIds="item.projectIds"
          v-model:handleUsers="item.handleUsers"
          :item="item"
          @change="changeHandler"
        />
        <CaseCount
          v-else-if="item.key === WorkCardEnum.CASE_COUNT"
          v-model:projectIds="item.projectIds"
          :item="item"
          @change="changeHandler"
        />
        <RelatedCaseCount
          v-else-if="item.key === WorkCardEnum.ASSOCIATE_CASE_COUNT"
          v-model:projectIds="item.projectIds"
          :item="item"
          @change="changeHandler"
        />
        <CaseReviewedCount
          v-else-if="item.key === WorkCardEnum.REVIEW_CASE_COUNT"
          v-model:projectIds="item.projectIds"
          :item="item"
          @change="changeHandler"
        />
        <WaitReviewList
          v-else-if="item.key === WorkCardEnum.REVIEWING_BY_ME"
          v-model:projectIds="item.projectIds"
          :item="item"
          @change="changeHandler"
        />
        <ApiAndScenarioCase
          v-else-if="[WorkCardEnum.API_CASE_COUNT, WorkCardEnum.SCENARIO_COUNT].includes(item.key)"
          v-model:projectIds="item.projectIds"
          :type="item.key"
          :item="item"
          @change="changeHandler"
        />
        <ApiChangeList
          v-else-if="item.key === WorkCardEnum.API_CHANGE"
          v-model:projectIds="item.projectIds"
          :item="item"
          @change="changeHandler"
        />
        <DefectMemberBar
          v-else-if="item.key === WorkCardEnum.BUG_HANDLE_USER"
          v-model:projectIds="item.projectIds"
          v-model:handleUsers="item.handleUsers"
          :item="item"
          @change="changeHandler"
        />
        <DefectCount
          v-else-if="countOfBug.includes(item.key)"
          v-model:projectIds="item.projectIds"
          :item="item"
          :type="item.key"
          @change="changeHandler"
        />
        <ApiCount
          v-else-if="item.key === WorkCardEnum.API_COUNT"
          v-model:projectIds="item.projectIds"
          :item="item"
          @change="changeHandler"
        />
        <TestPlanCount
          v-else-if="item.key === WorkCardEnum.TEST_PLAN_COUNT"
          v-model:projectIds="item.projectIds"
          :item="item"
          @change="changeHandler"
        />
      </div>
    </div>
    <NoData
      v-if="showNoData || !appStore.projectList.length"
      :no-res-permission="!appStore.projectList.length"
      :all-screen="!!appStore.projectList.length"
      height="h-[calc(100vh-110px)]"
      @config="cardSetting"
    />
  </div>
  <CardSettingDrawer v-model:visible="showSettingDrawer" :list="defaultWorkList" @success="initDefaultList" />
  <MsBackButton target=".page-content" />
</template>

<script setup lang="ts">
  import { ref } from 'vue';

  import MsBackButton from '@/components/pure/ms-back-button/index.vue';
  import MsTag from '@/components/pure/ms-tag/ms-tag.vue';
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

  import { editDashboardLayout, getDashboardLayout } from '@/api/modules/workbench';
  import { useI18n } from '@/hooks/useI18n';
  import { useUserStore } from '@/store';
  import useAppStore from '@/store/modules/app';
  import { getLocalStorage, setLocalStorage } from '@/utils/local-storage';

  import { SelectedCardItem, TimeFormParams } from '@/models/workbench/homePage';
  import { WorkCardEnum } from '@/enums/workbenchEnum';

  const userStore = useUserStore();

  const appStore = useAppStore();

  const { t } = useI18n();

  const rangeTime = ref<number[]>([]);
  const tempRange = ref<(Date | string | number)[]>(['00:00:00', '00:00:00']);
  const initTime: TimeFormParams = {
    dayNumber: 3,
    startTime: 0,
    endTime: 0,
  };
  const timeForm = ref<TimeFormParams>({ ...initTime });

  function resetTime() {
    timeForm.value = { ...initTime };
    rangeTime.value = [];
  }

  // 改变时间类型
  function handleChangeTime(value: string | number | boolean, ev: Event) {
    if (value) {
      resetTime();
      timeForm.value.dayNumber = value as number;
      setLocalStorage(`WORK_TIME_${userStore.id}`, JSON.stringify(timeForm.value));
    }
  }
  // 改变时间
  function handleTimeSelect(value: (Date | string | number | undefined)[]) {
    if (value) {
      timeForm.value.dayNumber = '';
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

  const showNoData = ref(false);
  async function initDefaultList() {
    try {
      appStore.showLoading();
      const result = await getDashboardLayout(appStore.currentOrgId);
      defaultWorkList.value = result;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      if (!defaultWorkList.value.length) {
        showNoData.value = true;
      }
      appStore.hideLoading();
    }
  }

  // 刷新
  function handleRefresh() {
    initDefaultList();
  }

  async function changeHandler() {
    try {
      await editDashboardLayout(defaultWorkList.value, appStore.currentOrgId);
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
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
      if (startTime && endTime) {
        rangeTime.value = [startTime, endTime];
      }
    }
  });

  const time = ref({ ...timeForm.value });

  watch(
    () => timeForm.value,
    (val) => {
      if (val.dayNumber || (val.endTime && val.startTime)) {
        time.value = { ...val };
      }
    },
    { deep: true }
  );

  provide('timeForm', time);
</script>

<style scoped lang="less">
  .work-bench-content {
    .header-setting {
      position: sticky;
      top: 0;
      z-index: 9;
      background: var(--color-text-n9);
      .setting {
        .setting-icon {
          color: var(--color-text-1);
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
    padding: 24px;
    box-shadow: 0 0 10px rgba(120 56 135/ 5%);
    @apply rounded-xl bg-white;
    &.card-min-height {
      min-height: 356px;
    }
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
