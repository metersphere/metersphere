<template>
  <MsTag theme="light" :type="getStatus"> {{ t(statusMap[props.status || 'PENDING'].label) }}</MsTag>
</template>

<script setup lang="ts">
  import { ref } from 'vue';

  import MsTag from '@/components/pure/ms-tag/ms-tag.vue';

  import { useI18n } from '@/hooks/useI18n';

  const { t } = useI18n();
  const props = defineProps<{
    status: string;
  }>();

  const statusMap = {
    PENDING: {
      label: 'report.detail.pendingCount',
      value: 'PENDING',
      type: 'default',
    },
    FAKE_ERROR: {
      label: 'report.detail.fakeErrorCount',
      value: 'FAKE_ERROR',
      type: 'warning',
    },
    ERROR: {
      label: 'report.failure',
      value: 'ERROR',
      type: 'danger',
    },
    SUCCESS: {
      label: 'report.detail.successCount',
      value: 'SUCCESS',
      type: 'success',
    },
  };

  const getStatus = computed(() => {
    if (props.status) {
      return statusMap[props.status].type;
    }
    return statusMap.PENDING.type;
  });
</script>

<style scoped></style>
