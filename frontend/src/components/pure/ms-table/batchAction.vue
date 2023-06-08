<template>
  <div class="ms-table__patch-action">
    <span class="title">{{ t('msTable.batch.selected', { count: props.selectRowCount }) }}</span>
    <a-button class="ml-4" type="outline" @click="emit('batchExport')">{{ t('msTable.batch.export') }}</a-button>
    <a-button class="ml-3" type="outline" @click="emit('batchEdit')">{{ t('msTable.batch.edit') }}</a-button>
    <a-button class="ml-3" type="outline" @click="emit('batchMoveTo')">{{ t('msTable.batch.moveTo') }}</a-button>
    <a-button class="ml-3" type="outline" @click="emit('batchCopyTo')">{{ t('msTable.batch.copyTo') }}</a-button>
    <div class="relative top-[2px] ml-3 inline-block">
      <a-dropdown position="tr">
        <a-button type="outline"><a-icon-more /></a-button>
        <template #content>
          <a-doption @click="emit('batchRelated')">{{ t('msTable.batch.related') }}</a-doption>
          <a-doption @click="emit('batchGenerate')">{{ t('msTable.batch.generateDep') }}</a-doption>
          <a-doption @click="emit('batchAddTo')">{{ t('msTable.batch.addPublic') }}</a-doption>
          <a-divider margin="0" />
          <a-doption class="delete" @click="emit('batchExport')">{{ t('msTable.batch.delete') }}</a-doption>
        </template>
      </a-dropdown>
    </div>
    <a-button class="ml-3" type="text" @click="emit('clear')">{{ t('msTable.batch.clear') }}</a-button>
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
    (e: 'batchMoveTo'): void;
    (e: 'batchCopyTo'): void;
    (e: 'batchRelated'): void;
    (e: 'batchGenerate'): void;
    (e: 'batchAddTo'): void;
    (e: 'batchDelete'): void;
    (e: 'clear'): void;
  }>();
</script>

<style lang="less" scoped>
  .ms-table__patch-action {
    .title {
      color: var(--color-text-2);
    }
  }
  .delete {
    color: rgb(var(--danger-6));
  }
</style>
