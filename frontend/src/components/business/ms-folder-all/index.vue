<template>
  <div class="folder">
    <div :class="getFolderClass()" @click="emit('setActiveFolder', 'all')">
      <MsIcon type="icon-icon_folder_filled1" class="folder-icon" />
      <div class="folder-name">{{ props.folderName }}</div>
      <div class="folder-count">({{ addCommasToNumber(props.allCount) }})</div>
    </div>
    <div class="ml-auto flex items-center">
      <slot name="expandLeft"></slot>
      <a-tooltip
        v-if="typeof isExpandAll === 'boolean'"
        :content="isExpandAll ? t('common.collapseAll') : t('common.expandAll')"
      >
        <MsButton
          v-if="typeof isExpandAll === 'boolean'"
          type="icon"
          status="secondary"
          class="!mr-0 p-[4px]"
          @click="changeExpand"
        >
          <MsIcon :type="isExpandAll ? 'icon-icon_folder_collapse1' : 'icon-icon_folder_expansion1'" />
        </MsButton>
      </a-tooltip>
      <slot name="expandRight"></slot>
    </div>
  </div>
</template>

<script setup lang="ts">
  import MsButton from '@/components/pure/ms-button/index.vue';

  import { useI18n } from '@/hooks/useI18n';
  import { addCommasToNumber } from '@/utils';

  const props = defineProps<{
    activeFolder?: string; // 选中的节点
    folderName: string; // 名称
    allCount: number; // 总数
  }>();

  const isExpandAll = defineModel<boolean>('isExpandAll', {
    required: false,
    default: undefined,
  });

  const emit = defineEmits<{
    (e: 'setActiveFolder', val: string): void;
  }>();

  const { t } = useI18n();

  function getFolderClass() {
    return props.activeFolder === 'all' ? 'folder-text folder-text--active' : 'folder-text';
  }

  function changeExpand() {
    isExpandAll.value = !isExpandAll.value;
  }
</script>

<style lang="less" scoped>
  .folder {
    @apply flex cursor-pointer items-center justify-between;

    padding: 4px;
    border-radius: var(--border-radius-small);
    &:hover {
      background-color: rgb(var(--primary-1));
    }
    .folder-text {
      @apply flex cursor-pointer items-center;

      height: 26px;
      .folder-icon {
        margin-right: 4px;
        color: var(--color-text-4);
      }
      .folder-name {
        color: var(--color-text-1);
      }
      .folder-count {
        margin-left: 4px;
        color: var(--color-text-4);
      }
    }
    .folder-text--active {
      .folder-icon,
      .folder-name,
      .folder-count {
        color: rgb(var(--primary-5));
      }
    }
  }
</style>
