<template>
  <a-modal
    v-model:visible="pluginVisible"
    title-align="start"
    width="500px"
    :footer="false"
    @ok="handleOk"
    @cancel="handleCancel"
  >
    <template #title> {{ t('system.plugin.uploadPlugin') }} </template>
    <div>
      <StepProgress :step-list="stepList" :current="currentStep" small :set-current="setCurrent" changeable />
      <SceneList />
    </div>
  </a-modal>
</template>

<script setup lang="ts">
  import { ref, watchEffect } from 'vue';
  import { useI18n } from '@/hooks/useI18n';
  import type { StepList } from '@/models/system/plugin';
  import StepProgress from './stepProgress.vue';
  import SceneList from './SceneList.vue';

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
  const emit = defineEmits<{
    (e: 'cancel'): void;
  }>();
  const props = defineProps<{
    visible: boolean;
  }>();
  watchEffect(() => {
    pluginVisible.value = props.visible;
  });
  const handleCancel = () => {
    emit('cancel');
  };

  const handleOk = () => {
    handleCancel();
  };
  const setCurrent = (step: number) => {
    currentStep.value = step;
  };
</script>

<style scoped></style>
