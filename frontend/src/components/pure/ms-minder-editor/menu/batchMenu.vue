<template>
  <a-affix
    v-if="batchMenuVisible"
    :offset-bottom="32"
    target-container=".page-content"
    class="absolute bottom-0 left-[50%]"
  >
    <div class="ms-minder-batch-menu">
      <a-dropdown
        v-if="props.priorityCount && props.canShowPriorityMenu"
        v-model:popup-visible="priorityMenuVisible"
        class="ms-minder-dropdown"
        :popup-translate="[0, 4]"
        position="bl"
        trigger="click"
        @select="(val) => handleMinderMenuSelect('priority', val as string)"
      >
        <a-tooltip :content="props.priorityTooltip" :disabled="!props.priorityTooltip">
          <MsButton
            type="icon"
            class="ms-minder-node-float-menu-icon-button"
            :class="[priorityMenuVisible ? 'ms-minder-node-float-menu-icon-button--focus' : '']"
          >
            <div
              class="h-[16px] w-[16px] rounded-full bg-[rgb(var(--primary-5))] text-center text-[12px] font-medium text-white"
            >
              P
            </div>
          </MsButton>
        </a-tooltip>
        <template #content>
          <div v-if="props.priorityTooltip" class="mx-[6px] px-[8px] py-[3px] text-[var(--color-text-4)]">
            {{ props.priorityTooltip }}
          </div>
          <template v-for="(item, pIndex) in priorityCount + 1" :key="item">
            <a-doption v-if="pIndex != 0" :value="pIndex">
              <div
                class="flex h-[20px] w-[20px] items-center justify-center rounded-full text-[12px] font-medium text-white"
                :style="{
                  backgroundColor: priorityColorMap[pIndex],
                }"
              >
                {{ priorityPrefix }}{{ priorityStartWithZero ? pIndex - 1 : pIndex }}
              </div>
            </a-doption>
          </template>
        </template>
      </a-dropdown>
      <slot name="batchMenu"></slot>
      <a-tooltip v-if="onlyDelete" :content="t('common.delete')">
        <MsButton type="icon" class="ms-minder-node-float-menu-icon-button" @click="handleMinderMenuSelect('delete')">
          <MsIcon type="icon-icon_delete-trash_outlined1" class="text-[var(--color-text-4)]" />
        </MsButton>
      </a-tooltip>
      <a-dropdown
        v-else-if="props.canShowMoreBatchMenu"
        v-model:popup-visible="moreMenuVisible"
        class="ms-minder-dropdown"
        :popup-translate="[0, -4]"
        position="tl"
        trigger="hover"
        @select="(val) => handleMinderMenuSelect(val)"
      >
        <a-tooltip :content="t('common.more')">
          <MsButton
            type="icon"
            class="ms-minder-node-float-menu-icon-button"
            :class="[moreMenuVisible ? 'ms-minder-node-float-menu-icon-button--focus' : '']"
          >
            <MsIcon type="icon-icon_more_outlined" class="text-[var(--color-text-4)]" />
          </MsButton>
        </a-tooltip>
        <template #content>
          <a-doption v-if="props.canShowBatchCopy" value="copy">
            <div class="flex items-center">
              <div>{{ t('minder.hotboxMenu.copy') }}</div>
              <div class="ml-[4px] text-[var(--color-text-4)]">(<MsCtrlOrCommand /> + C)</div>
            </div>
          </a-doption>
          <a-doption v-if="props.canShowBatchCut" value="cut">
            <div class="flex items-center">
              <div>{{ t('minder.hotboxMenu.cut') }}</div>
              <div class="ml-[4px] text-[var(--color-text-4)]">(<MsCtrlOrCommand /> + X)</div>
            </div>
          </a-doption>
          <a-doption v-if="props.canShowBatchDelete" value="delete">
            <div class="flex items-center">
              <div>{{ t('minder.hotboxMenu.delete') }}</div>
              <div class="ml-[4px] text-[var(--color-text-4)]">(Backspace)</div>
            </div>
          </a-doption>
          <a-doption v-if="props.canShowBatchExpand" value="expand">
            <div class="flex items-center">
              <div>{{ t('minder.hotboxMenu.expand') }}</div>
              <div class="ml-[4px] text-[var(--color-text-4)]">(/)</div>
            </div>
          </a-doption>
        </template>
      </a-dropdown>
    </div>
  </a-affix>
</template>

<script setup lang="ts">
  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsCtrlOrCommand from '@/components/pure/ms-ctrl-or-command';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';

  import { useI18n } from '@/hooks/useI18n';
  import { useMinderStore } from '@/store';

  import { MinderEventName } from '@/enums/minderEnum';

  import useMinderOperation from '../hooks/useMinderOperation';
  import usePriority from '../hooks/useMinderPriority';
  import {
    batchMenuProps,
    floatMenuProps,
    mainEditorProps,
    MinderJsonNode,
    priorityColorMap,
    priorityProps,
  } from '../props';

  const props = defineProps({
    ...mainEditorProps,
    ...floatMenuProps,
    ...priorityProps,
    ...batchMenuProps,
  });

  const { t } = useI18n();
  const minderStore = useMinderStore();
  const { setPriority } = usePriority(props);

  const batchMenuVisible = ref(false);
  const priorityMenuVisible = ref(false);
  const moreMenuVisible = ref(false);

  const { minderCopy, minderCut, minderDelete, minderExpand } = useMinderOperation(props);

  watch(
    () => minderStore.event.eventId,
    async () => {
      if (window.minder) {
        const selectedNodes: MinderJsonNode[] = window.minder.getSelectedNodes();
        if (
          minderStore.event.name === MinderEventName.DRAG_FINISH ||
          minderStore.event.name === MinderEventName.NODE_UNSELECT ||
          minderStore.event.name === MinderEventName.NODE_SELECT
        ) {
          batchMenuVisible.value = selectedNodes.length > 1;
        }
      }
    },
    {
      immediate: true,
    }
  );

  const onlyDelete = computed(() => {
    return props.canShowBatchDelete && !props.canShowBatchCopy && !props.canShowBatchCut && !props.canShowBatchExpand;
  });

  /**
   * 处理快捷菜单选择
   * @param type 选择的菜单项
   */
  function handleMinderMenuSelect(type: string | number | Record<string, any> | undefined, value?: string) {
    const selectedNodes: MinderJsonNode[] = window.minder.getSelectedNodes();
    if (selectedNodes.length > 0) {
      switch (type) {
        case 'copy':
          minderCopy();
          break;
        case 'cut':
          minderCut();
          break;
        case 'expand':
          minderExpand(selectedNodes);
          break;
        case 'delete':
          minderDelete(selectedNodes);
          break;
        case 'priority':
          setPriority(value);
          break;
        default:
          break;
      }
    }
  }
</script>

<style lang="less" scoped>
  .ms-minder-batch-menu {
    @apply absolute flex w-auto flex-1 items-center;

    bottom: 6px;
    left: 50%;
    padding: 4px 8px;
    border-radius: var(--border-radius-small);
    background-color: var(--color-text-fff);
    box-shadow: 0 4px 10px -1px rgb(100 100 102 / 15%);
    gap: 8px;
  }
</style>
