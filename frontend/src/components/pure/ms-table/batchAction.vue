<template>
  <div class="ms-table__patch-action">
    <span>{{ t('msTable.batch.selected', { count: props.selectRowCount }) }}</span>
    <a-button @click="emit('batchExport')">{{ t('msTable.batch.export') }}</a-button>
    <a-button @click="emit('batchEdit')">{{ t('msTable.batch.edit') }}</a-button>
    <a-button @click="emit('batchExport')">{{ t('msTable.batch.moveTo') }}</a-button>
    <a-button @click="emit('batchExport')">{{ t('msTable.batch.copyTo') }}</a-button>
    <a-button @click="emit('batchExport')">{{ t('msTable.batch.related') }}</a-button>
    <a-button @click="emit('batchExport')">{{ t('msTable.batch.delete') }}</a-button>
    <a-button @click="emit('clear')">{{ t('msTable.batch.clear') }}</a-button>
  </div>
</template>

<script lang="ts" setup>
  import { useI18n } from '@/hooks/useI18n';

  const { t } = useI18n();
  const props = defineProps({
    selectRowCount: {
      type: Number,
    },
  });
  const emit = defineEmits<{
    (e: 'batchExport'): void;
    (e: 'batchEdit'): void;
    (e: 'batchMove'): void;
    (e: 'batchRelated'): void;
    (e: 'batchDelete'): void;
    (e: 'clear'): void;
  }>();
</script>
