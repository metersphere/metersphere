<template>
  <a-collapse v-model:active-key="moreSettingActive" :bordered="false" :show-expand-icon="false">
    <a-collapse-item :key="1">
      <template #header>
        <slot name="header" :collapse="moreSettingActive.length > 0">
          <MsButton
            type="text"
            @click="() => (moreSettingActive.length > 0 ? (moreSettingActive = []) : (moreSettingActive = [1]))"
          >
            {{ t('common.moreSetting') }}
            <icon-down v-if="moreSettingActive.length > 0" class="text-rgb(var(--primary-5))" />
            <icon-right v-else class="text-rgb(var(--primary-5))" />
          </MsButton>
        </slot>
      </template>
      <div :class="`mt-[24px] ${props.contentClass}`">
        <slot name="content" :collapse="moreSettingActive.length > 0"></slot>
      </div>
    </a-collapse-item>
  </a-collapse>
</template>

<script setup lang="ts">
  import { ref } from 'vue';

  import MsButton from '@/components/pure/ms-button/index.vue';

  import { useI18n } from '@/hooks/useI18n';

  const { t } = useI18n();

  const props = defineProps<{
    contentClass?: string;
    defaultExpand?: boolean;
  }>();

  const moreSettingActive = ref<number[]>([]);
  function clearMoreSettingActive() {
    moreSettingActive.value = [];
  }

  watch(
    () => props.defaultExpand,
    (val) => {
      if (val) {
        moreSettingActive.value = [1];
      } else {
        moreSettingActive.value = [];
      }
    },
    {
      immediate: true,
    }
  );

  defineExpose({
    clearMoreSettingActive,
  });
</script>
