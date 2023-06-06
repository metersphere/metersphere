<template>
  <div class="expand-group">
    <a-button class="tab-icons expand" type="text" @click="expandAll" />
    <a-dropdown :popup-max-height="false" @select="handleCommand">
      <span class="dropdown-link">
        {{ t('minder.menu.expand.expand') }}
        <icon-caret-down />
      </span>
      <template #content>
        <a-doption value="1">{{ t('minder.menu.expand.expand_one') }}</a-doption>
        <a-doption value="2">{{ t('minder.menu.expand.expand_tow') }}</a-doption>
        <a-doption value="3">{{ t('minder.menu.expand.expand_three') }}</a-doption>
        <a-doption value="4">{{ t('minder.menu.expand.expand_four') }}</a-doption>
        <a-doption value="5">{{ t('minder.menu.expand.expand_five') }}</a-doption>
        <a-doption value="6">{{ t('minder.menu.expand.expand_six') }}</a-doption>
      </template>
    </a-dropdown>
  </div>
</template>

<script lang="ts" name="expand" setup>
  import { useI18n } from '@/hooks/useI18n';

  const { t } = useI18n();

  function handleCommand(command: string | number | Record<string, any> | undefined) {
    window.minder?.execCommand('ExpandToLevel', command);
  }

  function expandAll() {
    window.minder?.execCommand('ExpandToLevel', 9999);
  }
</script>

<style lang="less" scoped>
  .dropdown-link {
    @apply cursor-pointer;
  }
</style>
