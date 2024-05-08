<template>
  <a-collapse v-model:active-key="moreSettingActive" :bordered="false" :show-expand-icon="false">
    <a-collapse-item :key="1">
      <template #header>
        <MsButton
          type="text"
          @click="() => (moreSettingActive.length > 0 ? (moreSettingActive = []) : (moreSettingActive = [1]))"
        >
          {{ t('common.moreSetting') }}
          <icon-down v-if="moreSettingActive.length > 0" class="text-rgb(var(--primary-5))" />
          <icon-right v-else class="text-rgb(var(--primary-5))" />
        </MsButton>
      </template>
      <div class="mt-[24px]">
        <slot name="content"></slot>
      </div>
    </a-collapse-item>
  </a-collapse>
</template>

<script setup lang="ts">
  import { ref } from 'vue';

  import MsButton from '@/components/pure/ms-button/index.vue';

  import { useI18n } from '@/hooks/useI18n';

  const { t } = useI18n();

  const moreSettingActive = ref<number[]>([]);
  function clearMoreSettingActive() {
    moreSettingActive.value = [];
  }

  defineExpose({
    clearMoreSettingActive,
  });
</script>
