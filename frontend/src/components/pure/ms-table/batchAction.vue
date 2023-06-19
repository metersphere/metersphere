<template>
  <div v-if="props.actionConfig" class="ms-table__patch-action">
    <span class="title">{{ t('msTable.batch.selected', { count: props.selectRowCount }) }}</span>
    <template v-for="element in props.actionConfig.baseAction" :key="element.label">
      <a-divider v-if="element.isDivider" class="mx-0 my-[6px]" />
      <a-button
        v-else
        :class="{
          'delete': element.danger,
          'ml-4': true,
        }"
        type="outline"
        @click="emit('batchAction', element)"
        >{{ t(element.label as string) }}</a-button
      >
    </template>
    <div v-if="props.actionConfig.moreAction" class="relative top-[2px] ml-3 inline-block">
      <a-dropdown position="tr">
        <a-button type="outline"><a-icon-more /></a-button>
        <template #content>
          <template v-for="element in props.actionConfig.moreAction" :key="element.label">
            <a-divider v-if="element.isDivider" margin="0" />
            <a-doption v-else :value="element" :class="{ delete: element.danger }">{{
              t(element.label as string)
            }}</a-doption>
          </template>
        </template>
      </a-dropdown>
    </div>
    <a-button class="ml-3" type="text" @click="emit('clear')">{{ t('msTable.batch.clear') }}</a-button>
  </div>
</template>

<script lang="ts" setup>
  import { useI18n } from '@/hooks/useI18n';
  import { BatchActionConfig, BatchActionParams } from './type';

  const { t } = useI18n();
  const props = defineProps<{
    selectRowCount?: number;
    actionConfig?: BatchActionConfig;
  }>();
  const emit = defineEmits<{
    (e: 'batchAction', value: BatchActionParams): void;
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
