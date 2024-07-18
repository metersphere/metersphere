<template>
  <a-input
    v-model:model-value="inputValue"
    :placeholder="t('project.menu.pleaseInputJiraKey')"
    v-bind="attrs"
    :max-length="255"
    @blur="handleBlur"
  />
  <div class="flex flex-row items-center gap-[10px] text-[12px] leading-[16px]">
    <span class="mt-2 text-[var(--color-text-4)]">{{ attrs.subDesc }}</span>
    <a class="show-sub" @click="showPreviewImg">{{ t('project.menu.show') }}</a>
    <a-image-preview v-model:visible="imgVisible" :default-scale="0.75" :src="previewIcon" />
  </div>
</template>

<script setup lang="ts">
  import { Message } from '@arco-design/web-vue';

  import { validateJIRAKey } from '@/api/modules/project-management/menuManagement';
  import { getLogo } from '@/api/modules/setting/serviceIntegration';
  import { useI18n } from '@/hooks/useI18n';

  import { Rule } from '@form-create/arco-design';

  const attrs = useAttrs();
  const { formCreateInject } = attrs;
  const imgVisible = ref(false);

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
        const extra: Record<string, any> = {};
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
  const showPreviewImg = () => {
    imgVisible.value = true;
  };
  watchEffect(() => {
    if (props.value) {
      inputValue.value = props.value;
    }
  });
</script>

<style scoped>
  .show-sub {
    font-size: 12px;
    padding-top: 0.6rem;
    color: rgb(var(--primary-5)) !important;
    cursor: pointer;
  }
</style>
