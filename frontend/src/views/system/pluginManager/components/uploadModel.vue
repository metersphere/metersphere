<template>
  <a-modal
    v-model:visible="pluginVisible"
    class="ms-modal-form ms-modal-small"
    title-align="start"
    width="500px"
    @ok="handleOk"
    @cancel="handleCancel"
  >
    <template #title> {{ t('system.plugin.uploadPlugin') }} </template>
    <div>
      <StepProgress :step-list="stepList" :current="currentStep" small :set-current="setCurrent" changeable />
      <SceneList v-show="currentStep === 1" :set-current="setCurrent" />
      <uploadPlugin v-show="currentStep === 2" />
    </div>
    <template #footer>
      <div v-show="currentStep === 2" class="float-right">
        <a-space>
          <a-button type="secondary" @click="handleCancel">{{ t('system.plugin.pluginCancel') }}</a-button>
          <a-button type="secondary" @click="preStep">{{ t('system.plugin.pluginPreStep') }}</a-button>
          <a-button type="secondary" @click="saveAndAddPlugin">{{ t('system.plugin.saveAndAdd') }}</a-button>
          <a-button type="primary" @click="saveConfirm('confirm')">{{ t('system.plugin.pluginConfirm') }}</a-button>
        </a-space>
      </div>
    </template>
  </a-modal>
</template>

<script setup lang="ts">
  import { ref, watchEffect } from 'vue';
  import { useI18n } from '@/hooks/useI18n';
  import type { StepList } from '@/models/system/plugin';
  import StepProgress from './stepProgress.vue';
  import SceneList from './SceneList.vue';
  import uploadPlugin from './uploadPlugin.vue';

  const { t } = useI18n();
  const currentStep = ref<number>(1);

  const stepList = ref<StepList>([
    {
      name: '选择应用场景',
      title: 'system.plugin.SelectApplicationScene',
      status: true,
    },
    {
      name: '上传插件',
      title: 'system.plugin.uploadPlugin',
      status: true,
    },
  ]);
  const pluginVisible = ref(false);
  const emits = defineEmits<{
    (e: 'cancel'): void;
    (e: 'upload'): void;
    (e: 'success'): void;
  }>();
  const props = defineProps<{
    visible: boolean;
  }>();
  watchEffect(() => {
    pluginVisible.value = props.visible;
  });
  const handleCancel = () => {
    emits('cancel');
  };

  const handleOk = () => {
    handleCancel();
  };
  const setCurrent = (step: number) => {
    currentStep.value = step;
  };
  const preStep = () => {
    currentStep.value = currentStep.value === 2 ? 1 : 2;
  };
  const saveConfirm = (flag: string) => {
    if (flag === 'confirm') {
      handleCancel();
      emits('success');
    }
  };
  const saveAndAddPlugin = () => {
    saveConfirm('saveAndAdd');
    preStep();
  };
</script>

<style scoped lang="less"></style>
