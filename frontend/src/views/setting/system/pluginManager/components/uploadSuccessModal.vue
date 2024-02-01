<template>
  <a-modal
    v-model:visible="dialogVisible"
    class="ms-modal-form ms-modal-small"
    title-align="start"
    :footer="false"
    @open="BeforeOpen"
  >
    <template #title> {{ t('system.plugin.uploadPlugin') }} </template>
    <div class="flex w-full flex-col items-center justify-center">
      <div class="mb-5"><svg-icon :width="'60px'" :height="'60px'" :name="'success'" /></div>
      <div class="font-semibold">{{ t('system.plugin.uploadSuccess') }}</div>
      <div class="my-1 text-sm"
        ><a class="mx-1" href="javascript:;">{{ countDown }}</a>
        <span class="text-slate-400">{{ t('system.plugin.afterSecond') }}</span></div
      >
      <div class="mb-6 text-sm">
        {{ t('system.plugin.uploadSuccessAfter') }}
        <a @click="router.push({ name: RouteEnum.SETTING_ORGANIZATION_SERVICE })">{{
          t('system.plugin.ServiceIntegration')
        }}</a>
        {{ t('system.plugin.platformAuthentication') }}
      </div>
      <div>
        <a-space>
          <a-button type="primary" @click="continueAdd">{{ t('system.plugin.continueUpload') }}</a-button>
          <a-button type="outline" @click="router.push({ name: RouteEnum.SETTING_ORGANIZATION_SERVICE })">{{
            t('system.plugin.ServiceIntegration')
          }}</a-button>
          <a-button type="secondary" @click="emits('close')">{{ t('system.plugin.backPluginList') }}</a-button>
        </a-space>
      </div>
      <div class="mt-4">
        <a-checkbox v-model="isTip" class="text-sm">{{ t('system.plugin.nextNoTips') }}</a-checkbox>
      </div>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
  import { ref, watch, watchEffect } from 'vue';
  import { useRouter } from 'vue-router';

  import { useI18n } from '@/hooks/useI18n';
  import useVisit from '@/hooks/useVisit';

  import { RouteEnum } from '@/enums/routeEnum';

  const router = useRouter();
  const visitedKey = 'doNotShowAgain';
  const { t } = useI18n();
  const { addVisited } = useVisit(visitedKey);

  const props = defineProps<{
    visible: boolean;
  }>();
  const emits = defineEmits<{
    (event: 'update:visible', visible: boolean): void;
    (event: 'close'): void;
    (event: 'openUploadModal'): void;
  }>();

  const dialogVisible = ref<boolean>(false);

  watchEffect(() => {
    dialogVisible.value = props.visible;
  });

  watch(
    () => dialogVisible.value,
    (val) => {
      emits('update:visible', val);
    }
  );
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
  const isDoNotShowAgainChecked = () => {
    if (isTip.value) {
      addVisited();
    }
  };
  watch(isTip, () => {
    isDoNotShowAgainChecked();
  });

  const continueAdd = () => {
    emits('close');
    emits('openUploadModal');
  };
</script>

<style scoped>
  a {
    color: rgb(var(--primary-5));
  }
</style>
