<template>
  <a-modal v-model:visible="dialogVisible" class="ms-modal-form ms-modal-medium" title-align="start">
    <template #title> {{ title }} </template>
    <a-alert type="warning" :closable="true">
      <div>{{ t('system.plugin.changeSceneTips') }}</div>
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
              >{{ t('system.plugin.currentScene') }}</a-tag
            >
          </div>
          <div class="text-sm">{{ t(item.description) }}</div>
        </div>
      </div>
    </div>
    <template #footer>
      <a-button type="secondary" @click="emits('close')">{{ t('system.plugin.pluginCancel') }}</a-button>
      <a-button type="primary" @click="handelOk">
        {{ t('system.plugin.pluginConfirm') }}
      </a-button>
    </template>
  </a-modal>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { useDialog } from '@/hooks/useDialog';
  import type { SceneItem, SceneList } from '@/models/setting/plugin';

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
  const handelOk = () => {
    emits('close');
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
@/models/setting/plugin
