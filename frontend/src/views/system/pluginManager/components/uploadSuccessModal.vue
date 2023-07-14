<template>
  <a-modal
    v-model:visible="dialogVisible"
    class="ms-modal-form ms-modal-small"
    title-align="start"
    :footer="false"
    @open="BeforeOpen"
  >
    <template #title> {{ t(title as string) }} </template>
    <div class="flex w-full flex-col items-center justify-center">
      <div class="mb-5"><svg-icon :width="'60px'" :height="'60px'" :name="'success'" /></div>
      <div class="font-semibold">{{ t('system.plugin.uploadSuccess') }}</div>
      <div class="my-1 text-sm"
        ><a class="mx-1" href="javascript:;">{{ countDown }}</a>
        <span class="text-slate-400">{{ t('system.plugin.afterSecond') }}</span></div
      >
      <div class="mb-6 text-sm">
        {{ t('system.plugin.uploadSuccessAfter') }}
        <a href="javascript:;">{{ t('system.plugin.ServiceIntegration') }}</a>
        {{ t('system.plugin.platformAuthentication') }}
      </div>
      <div>
        <a-space>
          <a-button type="primary">{{ t('system.plugin.continueUpload') }}</a-button>
          <a-button type="outline">{{ t('system.plugin.ServiceIntegration') }}</a-button>
          <a-button type="secondary">{{ t('system.plugin.backPluginList') }}</a-button>
        </a-space>
      </div>
      <div class="mt-4">
        <a-checkbox v-model="isTip" class="text-sm">{{ t('system.plugin.nextNoTips') }}</a-checkbox>
      </div>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { useDialog } from '@/hooks/useDialog';
  import { useI18n } from '@/hooks/useI18n';

  const { t } = useI18n();
  const props = defineProps<{
    visible: boolean;
    title?: string;
  }>();
  const emits = defineEmits<{
    (event: 'update:visible', visible: boolean): void;
    (event: 'close'): void;
  }>();
  const { dialogVisible } = useDialog(props, emits);
  const isTip = ref(false);
  const countDown = ref<number>(5);
  const timer = ref<any>(null);
  const BeforeOpen = () => {
    timer.value = setInterval(() => {
      if (countDown.value > 1) {
        --countDown.value;
      } else {
        clearInterval(timer.value);
        emits('close');
        countDown.value = 5;
      }
    }, 1000);
  };
</script>

<style scoped>
  a {
    color: rgb(var(--primary-5));
  }
</style>
