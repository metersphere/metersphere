<template>
  <MsCard simple>
    <div class="h-full">
      <div class="mt-8 text-center">
        <div class="flex justify-center"><svg-icon :width="'60px'" :height="'60px'" :name="'success'" /></div>
        <div class="mb-2 mt-6 text-[20px] font-medium"> {{ t('caseManagement.featureCase.addSuccess') }} </div>
        <div
          ><span class="mr-1 text-[rgb(var(--primary-5))]">{{ countDown }}</span
          ><span class="text-[var(--color-text-4)]">{{ t('caseManagement.featureCase.countDownTip') }}</span></div
        >
        <div class="my-6">
          <a-button type="primary" @click="goDetail"> {{ t('caseManagement.featureCase.caseDetail') }} </a-button>
          <a-button class="mx-3" type="outline" @click="continueCreate">
            {{ t('caseManagement.featureCase.addContinueCreate') }}
          </a-button>
          <a-button type="secondary" @click="backCaseList">
            {{ t('caseManagement.featureCase.backCaseList') }}
          </a-button>
        </div>
        <a-checkbox v-model="isNextTip" class="mb-6">{{ t('caseManagement.featureCase.notNextTip') }}</a-checkbox>
      </div>
      <div>
        <div class="mb-4 font-medium">{{ t('caseManagement.featureCase.mightWantTo') }}</div>
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

                <a-button type="outline" @click="goNavigation(item.route)">
                  {{ t('caseManagement.featureCase.addContinueCreate') }}
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

  import MsCard from '@/components/pure/ms-card/index.vue';
  import MsCardList from '@/components/business/ms-card-list/index.vue';

  import { useI18n } from '@/hooks/useI18n';
  import useVisit from '@/hooks/useVisit';

  import { RouteEnum } from '@/enums/routeEnum';

  const { t } = useI18n();

  const visitedKey = 'doNotNextTipCreateCase';
  const { addVisited } = useVisit(visitedKey);

  const router = useRouter();
  const route = useRoute();
  const cardList = ref([
    // {
    //   key: 'testPlanTemplate',
    //   name: t('caseManagement.featureCase.createTestPlan'),
    // },
    {
      key: 'caseReview',
      name: t('caseManagement.featureCase.createCaseReview'),
      route: RouteEnum.CASE_MANAGEMENT_REVIEW_CREATE,
    },
  ]);

  const isNextTip = ref<boolean>(false);
  const countDown = ref<number>(5);
  const timer = ref<any>(null);
  function setCountdown() {
    timer.value = setInterval(() => {
      if (countDown.value > 1) {
        --countDown.value;
      } else {
        clearInterval(timer.value);
        router.push({
          name: RouteEnum.CASE_MANAGEMENT_CASE,
        });
      }
    }, 1000);
  }

  function isDoNotShowAgainChecked() {
    if (isNextTip.value) {
      addVisited();
    }
  }

  // 返回用例列表
  function backCaseList() {
    clearInterval(timer.value);
    timer.value = null;
    router.push({
      name: RouteEnum.CASE_MANAGEMENT_CASE,
    });
  }

  // 继续创建
  function continueCreate() {
    clearInterval(timer.value);
    timer.value = null;
    router.push({
      name: RouteEnum.CASE_MANAGEMENT_CASE_DETAIL,
    });
  }

  function goDetail() {
    clearInterval(timer.value);
    router.push({
      name: RouteEnum.CASE_MANAGEMENT_CASE,
      query: route.query,
    });
  }

  function goNavigation(name: string) {
    router.push({
      name,
    });
  }

  watch(
    () => isNextTip.value,
    () => {
      isDoNotShowAgainChecked();
    }
  );

  onMounted(() => {
    setCountdown();
  });

  onBeforeUnmount(() => {
    clearInterval(timer.value);
    timer.value = null;
    countDown.value = 0;
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
