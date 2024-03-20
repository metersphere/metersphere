<template>
  <div class="h-full w-full overflow-hidden">
    <div class="px-[24px] pt-[16px]">
      <MsDetailCard :title="`【${previewDetail.num}】${previewDetail.name}`" :description="description">
        <template #titleAppend>
          <apiStatus :status="previewDetail.status" size="small" />
        </template>
        <template #titleRight>
          <a-button
            type="outline"
            :loading="followLoading"
            size="mini"
            class="arco-btn-outline--secondary mr-[4px] !bg-transparent"
            @click="toggleFollowReview"
          >
            <div class="flex items-center gap-[4px]">
              <MsIcon
                :type="previewDetail.follow ? 'icon-icon_collect_filled' : 'icon-icon_collection_outlined'"
                :class="`${previewDetail.follow ? 'text-[rgb(var(--warning-6))]' : 'text-[var(--color-text-4)]'}`"
                :size="14"
              />
              {{ t(previewDetail.follow ? 'common.forked' : 'common.fork') }}
            </div>
          </a-button>
          <a-button type="outline" size="mini" class="arco-btn-outline--secondary !bg-transparent" @click="share">
            <div class="flex items-center gap-[4px]">
              <MsIcon type="icon-icon_share1" class="text-[var(--color-text-4)]" :size="14" />
              {{ t('common.share') }}
            </div>
          </a-button>
        </template>
        <template #type="{ value }">
          <apiMethodName :method="value as RequestMethods" tag-size="small" is-tag />
        </template>
      </MsDetailCard>
    </div>
    <div class="h-[calc(100%-124px)]">
      <a-tabs v-model:active-key="activeKey" class="h-full" animation lazy-load>
        <a-tab-pane
          :key="ScenarioDetailComposition.BASE_INFO"
          :title="t('apiScenario.baseInfo')"
          class="px-[24px] py-[16px]"
        >
          BASE_INFO
        </a-tab-pane>
        <a-tab-pane :key="ScenarioCreateComposition.STEP" :title="t('apiScenario.step')" class="px-[24px] py-[16px]">
          <step v-if="activeKey === ScenarioCreateComposition.STEP" :step="previewDetail.step" />
        </a-tab-pane>
        <a-tab-pane
          :key="ScenarioCreateComposition.PARAMS"
          :title="t('apiScenario.params')"
          class="px-[24px] py-[16px]"
        >
          <params v-if="activeKey === ScenarioCreateComposition.PARAMS" v-model:params="allParams" />
        </a-tab-pane>
        <a-tab-pane
          :key="ScenarioCreateComposition.PRE_POST"
          :title="t('apiScenario.prePost')"
          class="px-[24px] py-[16px]"
        >
          <prePost v-if="activeKey === ScenarioCreateComposition.PRE_POST" />
        </a-tab-pane>
        <a-tab-pane
          :key="ScenarioCreateComposition.ASSERTION"
          :title="t('apiScenario.assertion')"
          class="px-[24px] py-[16px]"
        >
          <assertion v-if="activeKey === ScenarioCreateComposition.ASSERTION" />
        </a-tab-pane>
        <a-tab-pane
          :key="ScenarioDetailComposition.EXECUTE_HISTORY"
          :title="t('apiScenario.executeHistory')"
          class="px-[24px] py-[16px]"
        >
          <executeHistory
            v-if="activeKey === ScenarioDetailComposition.EXECUTE_HISTORY"
            :scenario-id="previewDetail.id"
          />
        </a-tab-pane>
        <a-tab-pane
          :key="ScenarioDetailComposition.CHANGE_HISTORY"
          :title="t('apiScenario.changeHistory')"
          class="px-[24px] py-[16px]"
        >
          <changeHistory v-if="activeKey === ScenarioDetailComposition.CHANGE_HISTORY" :source-id="previewDetail.id" />
        </a-tab-pane>
        <a-tab-pane
          :key="ScenarioDetailComposition.DEPENDENCY"
          :title="t('apiScenario.dependency')"
          class="px-[24px] py-[16px]"
        >
          <dependency v-if="activeKey === ScenarioDetailComposition.DEPENDENCY" />
        </a-tab-pane>
        <a-tab-pane :key="ScenarioDetailComposition.QUOTE" :title="t('apiScenario.quote')" class="px-[24px] py-[16px]">
          <quote v-if="activeKey === ScenarioDetailComposition.QUOTE" />
        </a-tab-pane>
        <a-tab-pane :key="ScenarioCreateComposition.SETTING" :title="t('common.setting')" class="px-[24px] py-[16px]">
          <setting v-if="activeKey === ScenarioCreateComposition.SETTING" />
        </a-tab-pane>
      </a-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { useI18n } from 'vue-i18n';
  import { useClipboard } from '@vueuse/core';
  import { Message } from '@arco-design/web-vue';
  import { cloneDeep } from 'lodash-es';

  import MsDetailCard from '@/components/pure/ms-detail-card/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import apiMethodName from '@/views/api-test/components/apiMethodName.vue';
  import apiStatus from '@/views/api-test/components/apiStatus.vue';

  import { RequestMethods, ScenarioCreateComposition, ScenarioDetailComposition } from '@/enums/apiEnum';

  // 组成部分异步导入
  const step = defineAsyncComponent(() => import('../components/step/index.vue'));
  const params = defineAsyncComponent(() => import('../components/params.vue'));
  const prePost = defineAsyncComponent(() => import('../components/prePost.vue'));
  const assertion = defineAsyncComponent(() => import('../components/assertion.vue'));
  const executeHistory = defineAsyncComponent(() => import('../components/executeHistory.vue'));
  const changeHistory = defineAsyncComponent(() => import('../components/changeHistory.vue'));
  const dependency = defineAsyncComponent(() => import('../components/dependency.vue'));
  const quote = defineAsyncComponent(() => import('../components/quote.vue'));
  const setting = defineAsyncComponent(() => import('../components/setting.vue'));

  const allParams = ref<any[]>([]);
  const props = defineProps<{
    detail: Record<string, any>;
  }>();
  const emit = defineEmits(['updateFollow']);

  const { copy, isSupported } = useClipboard();
  const { t } = useI18n();

  const previewDetail = ref<Record<string, any>>(cloneDeep(props.detail));

  const description = computed(() => [
    {
      key: 'type',
      locale: 'something.type',
      value: 'type',
    },
    {
      key: 'path',
      locale: 'something.path',
      value: 'path',
    },
  ]);

  const followLoading = ref(false);
  async function toggleFollowReview() {
    try {
      followLoading.value = true;
      Message.success(previewDetail.value.follow ? t('common.unFollowSuccess') : t('common.followSuccess'));
      emit('updateFollow');
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      followLoading.value = false;
    }
  }

  function share() {
    if (isSupported) {
      copy(`${window.location.href}&dId=${previewDetail.value.id}`);
      Message.success(t('common.copySuccess'));
    } else {
      Message.error(t('common.copyNotSupport'));
    }
  }

  const activeKey = ref<ScenarioCreateComposition | ScenarioDetailComposition>(ScenarioDetailComposition.BASE_INFO);
</script>

<style lang="less" scoped>
  :deep(.arco-tabs-content) {
    @apply pt-0;
  }
</style>
