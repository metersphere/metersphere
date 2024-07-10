<template>
  <div class="text-[var(--color-text-2)]">
    {{ t('ms.case.associate.allSelected') }}
    <span class="mx-[4px] text-[rgb(var(--primary-5))]">{{ props.totalCount }}</span>
    {{ t('ms.case.associate.allSelectedItem') }}
  </div>
</template>

<script setup lang="ts">
  import { ref } from 'vue';

  import { useI18n } from '@/hooks/useI18n';

  const { t } = useI18n();

  const props = defineProps<{
    totalCount: number;
  }>();
</script>

<style scoped></style>
