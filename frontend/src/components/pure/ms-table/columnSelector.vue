<template>
  <icon-settings @click="handleClick" />
  <a-drawer :width="480" :visible="visible" unmount-on-close :footer="false" @cancel="handleCancel">
    <template #title> {{ t('mmsTable.columnSetting.display') }} </template>
    <div class="ms-table-column-seletor">
      <div>
        <span>{{ t('msTable.columnSetting.mode') }}</span>
        <icon-question-circle class="ml-1" />
      </div>
      <a-radio-group class="ml-[14px]" type="button">
        <a-radio value="drawer">{{ t('system.userGroup.auth') }}</a-radio>
        <a-radio value="newWindow">{{ t('system.userGroup.user') }}</a-radio>
      </a-radio-group>
    </div>
  </a-drawer>
</template>

<script lang="ts" setup>
  import { useI18n } from '@/hooks/useI18n';
  import { watchEffect, ref } from 'vue';
  // import { useAppStore } from '@/store';

  // const appStore = useAppStore();
  const props = defineProps<{
    visible: boolean;
  }>();

  // const currentMode = computed(() => {});

  const { t } = useI18n();

  const emit = defineEmits<{
    (e: 'cancel'): void;
    (e: 'openSetting'): void;
  }>();

  const visible = ref(props.visible);

  const handleCancel = () => {
    emit('cancel');
  };

  const handleClick = () => {
    emit('openSetting');
  };

  watchEffect(() => {
    visible.value = props.visible;
  });
</script>

<style lang="less"></style>
