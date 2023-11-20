<template>
  <MsTag
    :type="props.visible ? 'primary' : 'default'"
    :theme="props.visible ? 'lightOutLine' : 'outline'"
    size="large"
    class="mt-[3px] min-w-[64px] cursor-pointer"
  >
    <span :class="!props.visible ? 'text-[var(--color-text-4)]' : ''" @click="handleOpenFilter">
      <icon-filter class="text-[16px]" />
      <span class="ml-[4px]">
        <span v-if="props.count">{{ props.count }}</span>
        {{ t('common.filter') }}
      </span>
    </span>
  </MsTag>
</template>

<script setup lang="ts">
  import MsTag from '../ms-tag/ms-tag.vue';

  import { useI18n } from '@/hooks/useI18n';

  const { t } = useI18n();
  const props = defineProps<{
    visible: boolean;
    count?: number;
  }>();
  const emit = defineEmits<{
    (e: 'update:visible', value: boolean): void;
    (e: 'update:count', value: number): void;
  }>();

  const handleOpenFilter = () => {
    emit('update:visible', !props.visible);
  };
</script>
