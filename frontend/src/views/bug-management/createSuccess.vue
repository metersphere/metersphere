<template>
  <MsCard simple>
    <div class="h-full">
      <div class="mt-8 text-center">
        <div class="flex justify-center"><svg-icon :width="'60px'" :height="'60px'" :name="'success'" /></div>
        <div class="mb-2 mt-6 text-[20px] font-medium"> {{ t('common.addSuccess') }} </div>
        <div
          ><span class="mr-1 text-[rgb(var(--primary-5))]">{{ timer }}</span
          ><span class="text-[var(--color-text-4)]">{{ t('bugManagement.success.countDownTip') }}</span></div
        >
        <div class="my-6">
          <a-button type="primary" @click="goDetail"> {{ t('bugManagement.success.bugDetail') }} </a-button>
          <a-button class="mx-3" type="outline" @click="continueCreate">
            {{ t('bugManagement.success.addContinueCreate') }}
          </a-button>
          <a-button type="secondary" @click="backCaseList">
            {{ t('bugManagement.success.backBugList') }}
          </a-button>
        </div>
        <a-checkbox v-model="isNextTip" class="mb-6">{{ t('bugManagement.success.notNextTip') }}</a-checkbox>
      </div>
      <div>
        <div class="mb-4 font-medium">{{ t('bugManagement.success.mightWantTo') }}</div>
        <MsCardList
          mode="static"
          :card-min-width="569"
          class="flex-1"
          :shadow-limit="50"
          :list="cardList"
          :is-proportional="false"
          :gap="16"
          padding-bottom-space="16px"
        >
          <template #item="{ item }">
            <div class="outerWrapper p-[3px]">
              <div class="innerWrapper flex items-center justify-between">
                <div class="flex items-center">
                  <div class="logo-img flex h-[48px] w-[48px] items-center justify-center">
                    <svg-icon width="36px" height="36px" :name="item.key"></svg-icon>
                  </div>
                  <div class="ml-2"> {{ item.name }} </div>
                </div>

                <a-button type="outline" @click="handleCaseRelated">
                  {{ t('bugManagement.success.caseRelated') }}
                </a-button>
              </div>
            </div>
          </template>
        </MsCardList>
      </div>
    </div>
  </MsCard>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import { useIntervalFn } from '@vueuse/core';

  import MsCard from '@/components/pure/ms-card/index.vue';
  import MsCardList from '@/components/business/ms-card-list/index.vue';

  import { useI18n } from '@/hooks/useI18n';
  import useVisit from '@/hooks/useVisit';

  import { BugManagementRouteEnum, RouteEnum } from '@/enums/routeEnum';

  const { t } = useI18n();

  const visitedKey = 'doNotNextTipCreateBug';
  const { addVisited } = useVisit(visitedKey);

  const router = useRouter();
  const route = useRoute();
  const cardList = ref([
    {
      key: 'caseReview',
      name: t('bugManagement.success.caseRelated'),
    },
  ]);

  const isNextTip = ref<boolean>(false);
  const timer = ref(15); // 存储倒计时
  const { pause, resume } = useIntervalFn(() => {
    timer.value--;
  }, 1000);
  // 初始化组件时关闭
  pause();
  // timer 为0时关闭
  watch(timer, () => {
    if (timer.value === 0) {
      pause();
      router.push({
        name: BugManagementRouteEnum.BUG_MANAGEMENT_INDEX,
      });
    }
  });

  function isDoNotShowAgainChecked() {
    if (isNextTip.value) {
      addVisited();
    }
  }

  // 返回用例列表
  function backCaseList() {
    router.push({
      name: BugManagementRouteEnum.BUG_MANAGEMENT_INDEX,
    });
  }

  // 继续创建
  function continueCreate() {
    router.push({
      name: BugManagementRouteEnum.BUG_MANAGEMENT_DETAIL,
    });
  }

  function goDetail() {
    router.push({
      name: BugManagementRouteEnum.BUG_MANAGEMENT_INDEX,
      query: route.query,
    });
  }

  // 关联用例
  function handleCaseRelated() {
    router.push({
      name: RouteEnum.CASE_MANAGEMENT_CASE,
      query: route.query,
    });
  }

  watch(
    () => isNextTip.value,
    () => {
      isDoNotShowAgainChecked();
    }
  );

  onMounted(() => {
    resume();
  });
</script>

<style scoped lang="less">
  .outerWrapper {
    background-color: var(--color-text-fff);
    box-shadow: 0 6px 15px rgba(120 56 135/ 5%);
    @apply rounded;
    .innerWrapper {
      background: var(--color-bg-3);
      @apply rounded p-6;
      .logo-img {
        @apply mr-3 flex items-center;

        background-color: var(--color-text-fff);
      }
    }
  }
</style>
