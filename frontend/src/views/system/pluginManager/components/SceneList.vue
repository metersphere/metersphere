<template>
  <div class="mt-6 flex flex-col">
    <div
      v-for="(item, index) in sceneList"
      :key="index"
      class="ms-ls-row my-2"
      :class="{ 'ms-ls-row--active': item.isSelected }"
      @click="selectHandler(item)"
    >
      <div class="ms-icon-list ml-4 mr-5" :class="`ms-icon-list--${item.svg}`"></div>
      <div class="flex flex-col justify-center">
        <div class="mb-1 font-medium">{{ t(item.name) }}</div>
        <div class="text-sm">{{ t(item.description) }}</div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import type { SceneItem, SceneList } from '@/models/system/plugin';
  import { useI18n } from '@/hooks/useI18n';

  const { t } = useI18n();
  const sceneList = ref<SceneList>([
    {
      name: 'system.plugin.interfaceTest',
      description: 'system.plugin.interfaceTestDescribe',
      isSelected: true,
      svg: 'api',
    },
    {
      name: 'system.plugin.projectManger',
      description: 'system.plugin.projectMangerDescribe',
      isSelected: false,
      svg: 'project',
    },
  ]);

  const selectHandler = (currentItem: SceneItem) => {
    sceneList.value.forEach((item: SceneItem) => {
      item.isSelected = false;
    });
    currentItem.isSelected = true;
  };
</script>

<style scoped lang="less">
  .ms-icon-list {
    width: 48px;
    height: 48px;
    &--api {
      background-image: url('@/assets/svg/icons/apitest.svg');
    }
    &--project {
      background-image: url('@/assets/svg/icons/promanger.svg');
    }
  }
  .ms-ls-row {
    border-width: 1px;
    @apply flex flex-row items-center rounded border-solid py-2;
    &--active {
      /* stylelint-disable-next-line color-function-notation */
      background-color: rgba(var(--primary-5), 0.1);
    }
  }
</style>
