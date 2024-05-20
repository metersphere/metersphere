<template>
  <a-spin :loading="loading" class="w-full">
    <div class="execute-history-list">
      <div v-for="item of executeHistoryList" :key="item.status" class="execute-history-list-item">
        <div class="flex items-center">
          <MsAvatar :avatar="item.userLogo" />
          <div class="ml-[8px] flex items-center">
            <a-tooltip :content="item.userName" :mouse-enter-delay="300">
              <div class="one-line-text max-w-[300px] font-medium text-[var(--color-text-1)]">{{ item.userName }}</div>
            </a-tooltip>
            <a-divider direction="vertical" margin="8px"></a-divider>
            <div v-if="item.status === 'SUCCESS'" class="flex items-center">
              <MsIcon type="icon-icon_succeed_filled" class="mr-[4px] text-[rgb(var(--success-6))]" />
              {{ t('common.success') }}
            </div>
            <div v-if="item.status === 'BLOCKED'" class="flex items-center">
              <MsIcon type="icon-icon_block_filled" class="mr-[4px] text-[rgb(var(--warning-6))]" />
              {{ t('common.block') }}
            </div>
            <div v-if="item.status === 'ERROR'" class="flex items-center">
              <MsIcon type="icon-icon_close_filled" class="mr-[4px] text-[rgb(var(--danger-6))]" />
              {{ t('common.fail') }}
            </div>
          </div>
        </div>
        <div class="markdown-body" style="margin-left: 48px" v-html="item.contentText"></div>
        <div class="ml-[48px] mt-[8px] flex text-[var(--color-text-4)]">
          {{ dayjs(item.createTime).format('YYYY-MM-DD HH:mm:ss') }}
          <div>
            <a-tooltip :content="item.userName" :mouse-enter-delay="300" :disabled="!item.userName">
              <span v-if="item.deleted" class="one-line-text ml-[16px] max-w-[300px] break-words break-all">
                {{ characterLimit(item.userName) }}
              </span>
            </a-tooltip>
          </div>
        </div>
      </div>
      <MsEmpty v-if="executeHistoryList.length === 0" />
    </div>
  </a-spin>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { useRoute } from 'vue-router';
  import dayjs from 'dayjs';

  import MsAvatar from '@/components/pure/ms-avatar/index.vue';
  import MsEmpty from '@/components/pure/ms-empty/index.vue';

  import { executeHistory } from '@/api/modules/test-plan/testPlan';
  import { useI18n } from '@/hooks/useI18n';
  import { characterLimit } from '@/utils';

  import type { ExecuteHistoryItem } from '@/models/testPlan/testPlan';

  const { t } = useI18n();
  const props = defineProps<{
    caseId: string;
    testPlanCaseId: string;
  }>();

  const executeHistoryList = ref<ExecuteHistoryItem[]>([]);

  const route = useRoute();
  const loading = ref<boolean>(false);

  async function initList() {
    loading.value = true;
    try {
      executeHistoryList.value = await executeHistory({
        caseId: props.caseId,
        id: props.testPlanCaseId,
        testPlanId: route.query.id as string,
      });
    } catch (error) {
      console.log(error);
    } finally {
      loading.value = false;
    }
  }

  onBeforeMount(() => {
    initList();
  });

  watch(
    () => props.caseId,
    (val) => {
      if (val) {
        initList();
      }
    }
  );
</script>

<style scoped lang="less">
  .execute-history-list {
    height: calc(100vh - 240px);
    @apply overflow-auto;
    .ms-scroll-bar();
    .execute-history-list-item {
      &:not(:last-child) {
        margin-bottom: 16px;
      }
    }
  }
</style>
