<template>
  <div class="arrange-group">
    <div class="arrange menu-btn" :disabled="disabled" @click="resetlayout">
      <span class="tab-icons" />
      <span class="label">
        {{ t('minder.menu.arrange.arrange_layout') }}
      </span>
    </div>
  </div>
</template>

<script lang="ts" name="Arrange" setup>
  import { computed } from 'vue';
  import { useI18n } from '@/hooks/useI18n';

  const { t } = useI18n();

  const disabled = computed(() => {
    try {
      if (!window.minder) return false;
    } catch (e) {
      // 如果window的还没挂载minder，先捕捉undefined异常
      return false;
    }
    return window.minder.queryCommandState && window.minder.queryCommandState('resetlayout') === -1;
  });

  function resetlayout() {
    if (window.minder.queryCommandState('resetlayout') !== -1) {
      window.minder.execCommand('resetlayout');
    }
  }
</script>
