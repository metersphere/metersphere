<template>
  <div class="mt-6 flex flex-col">
    <div
      v-for="(item, index) in sceneList"
      :key="index"
      class="ms-ls-row my-2"
      :class="{ 'ms-ls-row--active': item.isSelected }"
      @click="selectHandler(item)"
    >
      <div class="ms-icon-list ml-4 mr-5">
        <svg-icon :width="'64px'" :height="'46px'" :name="item.svg" />
      </div>
      <div class="flex flex-col justify-center">
        <div class="mb-1 font-medium">{{ t(item.name) }}</div>
        <div class="text-sm">{{ t(item.description) }}</div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import type { SceneItem, SceneList } from '@/models/setting/plugin';
  import { useI18n } from '@/hooks/useI18n';

  const { t } = useI18n();
  const props = defineProps<{
    setCurrent: (step: number) => void;
  }>();
  const sceneList = ref<SceneList>([
    {
      name: 'system.plugin.interfaceTest',
      description: 'system.plugin.interfaceTestDescribe',
      isSelected: true,
      svg: 'apitest',
    },
    {
      name: 'system.plugin.projectManger',
      description: 'system.plugin.projectMangerDescribe',
      isSelected: false,
      svg: 'promanger',
    },
  ]);

  const selectHandler = (currentItem: SceneItem) => {
    sceneList.value.forEach((item: SceneItem) => {
      item.isSelected = false;
    });
    currentItem.isSelected = true;
    props.setCurrent(2);
  };
</script>

<style scoped lang="less">
  .ms-ls-row {
    border-width: 1px;
    @apply flex flex-row items-center rounded border-solid py-2;
    &--active {
      /* stylelint-disable-next-line color-function-notation */
      background-color: rgb(var(--primary-1));
    }
    &:hover {
      background-color: rgb(var(--primary-1));
    }
  }
</style>
@/models/setting/plugin
