<template>
  <a-input
    :value="props.modelValue"
    :placeholder="t('project.menu.pleaseInputJiraKey')"
    v-bind="attrs"
    @change="(v: string) => emit('update:modelValue', v)"
  />
  <div class="flex flex-row items-center gap-[10px] text-[12px] leading-[20px]">
    <span class="text-[var(--color-text-4)]">{{ t('project.menu.howGetJiraKey') }}</span>
    <a-popover position="rt">
      <template #title>
        {{ null }}
      </template>
      <template #content>
        <img class="h-[247px] w-[398px]" :src="props.instructionsIcon" />
      </template>
      <span class="cursor-pointer text-[rgb(var(--primary-5))]">{{ t('project.menu.preview') }}</span>
    </a-popover>
  </div>
</template>

<script setup lang="ts">
  import { useI18n } from '@/hooks/useI18n';

  const attrs = useAttrs();

  const props = defineProps<{
    modelValue: string;
    instructionsIcon: string;
  }>();
  const emit = defineEmits<{
    (event: 'update:modelValue', value: string): void;
  }>();

  const { t } = useI18n();
</script>

<style scoped></style>
