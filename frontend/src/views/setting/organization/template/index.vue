<template>
  <MsCard simple>
    <div style="display: flex !important" class="flex h-[100%] flex-col overflow-hidden">
      <a-alert v-if="isShowTip" class="mb-4 py-3">
        <div class="flex items-start justify-between">
          <span class="w-[80%]">{{ t('system.orgTemplate.templateDescription') }}</span>
          <span class="cursor-pointer text-[var(--color-text-2)]" @click="noRemindHandler">{{
            t('system.orgTemplate.noReminders')
          }}</span>
        </div>
      </a-alert>
      <MsCardList
        mode="static"
        :card-min-width="360"
        class="flex-1"
        :shadow-limit="50"
        :list="cardList"
        :is-proportional="false"
        :gap="16"
        padding-bottom-space="16px"
      >
        <template #item="{ item, index }">
          <TemplateItem :card-item="item" :index="index" />
        </template>
      </MsCardList>
    </div>
  </MsCard>
</template>

<script setup lang="ts">
  /**
   * @description 系统设置--组织--模版
   */
  import { onMounted, ref } from 'vue';

  import MsCard from '@/components/pure/ms-card/index.vue';
  import MsCardList from '@/components/business/ms-card-list/index.vue';
  import TemplateItem from '@/components/business/ms-template-card/index.vue';

  import { useI18n } from '@/hooks/useI18n';
  import useVisit from '@/hooks/useVisit';

  const { t } = useI18n();

  const cardList = ref([
    {
      id: 1001,
      value: 'FUNCTIONAL',
      name: t('system.orgTemplate.caseTemplates'),
    },
    { id: 1002, value: 'API', name: t('system.orgTemplate.APITemplates') },
    { id: 1003, value: 'UI', name: t('system.orgTemplate.UITemplates') },
    { id: 1004, value: 'TEST_PLAN', name: t('system.orgTemplate.testPlanTemplates') },
    { id: 1005, value: 'BUG', name: t('system.orgTemplate.defectTemplates') },
  ]);

  const visitedKey = 'notRemind';
  const { addVisited } = useVisit(visitedKey);
  const { getIsVisited } = useVisit(visitedKey);

  const isShowTip = ref<boolean>(true);
  const noRemindHandler = () => {
    isShowTip.value = false;
    addVisited();
  };

  // 不再提醒
  const doCheckIsTip = () => {
    isShowTip.value = !getIsVisited();
  };

  onMounted(() => {
    doCheckIsTip();
  });
</script>

<style scoped lang="less"></style>
