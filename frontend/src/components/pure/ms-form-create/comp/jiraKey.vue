<template>
  <a-input
    :value="props.modelValue"
    :placeholder="t('project.menu.pleaseInputJiraKey')"
    v-bind="attrs"
    :max-length="255"
    @change="(v: string) => emit('update:modelValue', v)"
    @blur="handleBlur"
  />
  <div class="flex flex-row items-center gap-[10px] text-[12px] leading-[16px]">
    <span class="text-[var(--color-text-4)]">{{ t('project.menu.howGetJiraKey') }}</span>
    <a-popover position="rt">
      <template #title>
        {{ null }}
      </template>
      <template #content>
        <img class="h-[247px] w-[398px]" :src="previewIcon" />
      </template>
      <span class="cursor-pointer text-[rgb(var(--primary-5))]">{{ t('project.menu.preview') }}</span>
    </a-popover>
  </div>
</template>

<script setup lang="ts">
  import { Message } from '@arco-design/web-vue';

  import { validateJIRAKey } from '@/api/modules/project-management/menuManagement';
  import { getLogo } from '@/api/modules/setting/serviceIntegration';
  import { useI18n } from '@/hooks/useI18n';

  const attrs = useAttrs();

  const props = defineProps<{
    modelValue: string;
    instructionsIcon: string;
  }>();
  const previewIcon = ref<string>('');

  const emit = defineEmits<{
    (event: 'update:modelValue', value: string): void;
  }>();

  const { t } = useI18n();
  onMounted(() => {
    const pluginId = sessionStorage.getItem('platformKey');
    if (!props.instructionsIcon || !pluginId) return;
    getLogo(pluginId, { imagePath: props.instructionsIcon }).then((res) => {
      const { data } = res;
      previewIcon.value = URL.createObjectURL(new Blob([data]));
    });
  });
  const handleBlur = async () => {
    const pluginId = sessionStorage.getItem('platformKey') || '';
    if (pluginId) {
      try {
        await validateJIRAKey({ name: '1231' }, pluginId);
        Message.success('common.validateSuccess');
      } catch {
        Message.error('common.validateFaild');
      }
    }
  };
</script>

<style scoped></style>
