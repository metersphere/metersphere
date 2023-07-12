<template>
  <a-modal v-model:visible="dialogVisible" class="ms-modal-form ms-modal-small" title-align="start">
    <template #title> {{ title }} </template>
    <a-alert type="warning" :closable="true">
      <div> 插件内容与应用场景不一致时插件功能将无法启用，请谨慎操作！ </div>
    </a-alert>
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
          <div class="mb-1 font-medium">
            <span>{{ t(item.name) }}</span>
            <a-tag
              v-show="item.isSelected"
              size="small"
              class="ml-[4px] border-[rgb(var(--primary-4))] bg-transparent px-1 text-xs !text-[rgb(var(--primary-4))]"
              >当前场景</a-tag
            >
          </div>
          <div class="text-sm">{{ t(item.description) }}</div>
        </div>
      </div>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { useDialog } from '@/hooks/useDialog';
  import type { SceneItem, SceneList } from '@/models/system/plugin';

  import { useI18n } from '@/hooks/useI18n';

  const { t } = useI18n();
  const props = defineProps<{
    visible: boolean;
    title?: string;
  }>();
  const emits = defineEmits<{
    (event: 'update:visible', visible: boolean): void;
    (event: 'close'): void;
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
  const { dialogVisible } = useDialog(props, emits);
  const selectHandler = (currentItem: SceneItem) => {
    sceneList.value.forEach((item: SceneItem) => {
      item.isSelected = false;
    });
    currentItem.isSelected = true;
  };
</script>

<style scoped lang="less">
  .ms-ls-row {
    border-width: 1px;
    @apply flex flex-row items-center rounded border-solid py-2;
    &--active {
      background-color: rgb(var(--primary-1));
    }
    &:hover {
      background-color: rgb(var(--primary-1));
    }
  }
</style>
