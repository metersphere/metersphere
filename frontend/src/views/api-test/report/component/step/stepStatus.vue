<template>
  <MsTag theme="light" :type="getStatus"> {{ t(statusMap[props.status || 'PENDING'].label) }}</MsTag>
</template>

<script setup lang="ts">
  import MsTag from '@/components/pure/ms-tag/ms-tag.vue';

  import { useI18n } from '@/hooks/useI18n';

  const { t } = useI18n();
  const props = defineProps<{
    status: string;
  }>();

  // TODO: Record<string,any>
  const statusMap: Record<string, any> = {
    PENDING: {
      label: 'common.unExecute',
      value: 'PENDING',
      type: 'default',
    },
    FAKE_ERROR: {
      label: 'common.fakeError',
      value: 'FAKE_ERROR',
      type: 'warning',
    },
    ERROR: {
      label: 'common.fail',
      value: 'ERROR',
      type: 'danger',
    },
    SUCCESS: {
      label: 'common.pass',
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
