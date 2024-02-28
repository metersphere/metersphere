<template>
  <a-input
    v-model:model-value="inputValue"
    :placeholder="t('project.menu.pleaseInputJiraKey')"
    v-bind="attrs"
    :max-length="255"
    @blur="handleBlur"
  />
  <div class="flex flex-row items-center gap-[10px] text-[12px] leading-[16px]">
    <span class="text-[var(--color-text-4)]">{{ attrs.subDesc }}</span>
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
  import { defineModel } from 'vue';
  import { Message } from '@arco-design/web-vue';

  import { validateJIRAKey } from '@/api/modules/project-management/menuManagement';
  import { getLogo } from '@/api/modules/setting/serviceIntegration';
  import { useI18n } from '@/hooks/useI18n';

  import { Rule } from '@form-create/arco-design';

  const attrs = useAttrs();
  const { formCreateInject } = attrs;

  const props = defineProps<{
    instructionsIcon: string;
    value?: string;
  }>();
  const previewIcon = ref<string>('');

  const inputValue = defineModel<string>();

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
        const { rule } = (attrs.formCreateInject as { [key: string]: any }).api;
        const extra = {};
        rule.forEach((item: Rule) => {
          extra[item.field as string] = item.value;
        });
        await validateJIRAKey(
          { [(formCreateInject as { [key: string]: string }).field]: inputValue.value, ...extra },
          pluginId
        );
        Message.success(t('common.validateSuccess'));
      } catch (e) {
        // eslint-disable-next-line no-console
        console.log(e);
      }
    }
  };
  watchEffect(() => {
    if (props.value) {
      inputValue.value = props.value;
    }
  });
</script>

<style scoped></style>
