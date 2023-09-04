<template>
  <div class="flex items-center gap-[4px]">
    <popConfirm mode="add" :all-names="[]" @close="emit('close')">
      <MsButton type="text" size="mini" class="action-btn" @click="emit('add', props.item)">
        <MsIcon type="icon-icon_add_outlined" size="14" class="text-[var(--color-text-4)]" />
      </MsButton>
    </popConfirm>
    <MsTableMoreAction
      :list="folderMoreActions"
      trigger="click"
      @select="emit('select', $event, props.item)"
      @close="emit('actionsClose')"
    >
      <MsButton type="text" size="mini" class="action-btn" @click="emit('clickMore', props.item)">
        <MsIcon type="icon-icon_more_outlined" size="14" class="text-[var(--color-text-4)]" />
      </MsButton>
    </MsTableMoreAction>
    <popConfirm mode="rename" :title="item.name" :all-names="[]" @close="emit('close')">
      <span ref="renameSpanRef" class="relative"></span>
    </popConfirm>
  </div>
</template>

<script setup lang="ts">
  import popConfirm from './popConfirm.vue';
  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsTableMoreAction from '@/components/pure/ms-table-more-action/index.vue';

  import type { ActionsItem } from '@/components/pure/ms-table-more-action/types';

  const props = defineProps<{
    item: Record<string, any>;
  }>();

  const emit = defineEmits(['add', 'close', 'select', 'actionsClose', 'clickMore']);

  const folderMoreActions: ActionsItem[] = [
    {
      label: 'project.fileManagement.rename',
      eventTag: 'rename',
    },
    {
      label: 'project.fileManagement.delete',
      eventTag: 'delete',
      danger: true,
    },
  ];
</script>

<style lang="less" scoped>
  .action-btn {
    @apply !mr-0;

    padding: 4px;
    border-radius: var(--border-radius-mini);
    &:hover {
      background-color: rgb(var(--primary-9));
      .arco-icon {
        color: rgb(var(--primary-5));
      }
    }
  }
</style>
