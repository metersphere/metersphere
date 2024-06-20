<template>
  <a-trigger
    v-model:popup-visible="menuVisible"
    class="ms-minder-node-float-menu"
    position="bl"
    :popup-translate="menuPopupOffset"
    trigger="click"
    :click-outside-to-close="false"
    popup-container=".ms-minder-container"
  >
    <span></span>
    <template #content>
      <a-radio-group
        v-if="tags.length > 0"
        v-model:model-value="currentNodeTags[0]"
        type="button"
        size="mini"
        @change="(val) => handleTagChange(val as string)"
      >
        <a-radio v-for="tag of currentNodeTags" :key="tag" :value="tag">{{ tag }}</a-radio>
        <a-radio v-for="tag of tags" :key="tag" :value="tag">{{ tag }}</a-radio>
      </a-radio-group>
      <a-dropdown
        v-if="props.insertSiblingMenus.length > 0"
        v-model:popup-visible="insertSiblingMenuVisible"
        class="ms-minder-dropdown"
        :popup-translate="[0, 4]"
        position="bl"
        trigger="click"
        @select="(val) => handleMinderMenuSelect('AppendSiblingNode',val as string)"
      >
        <a-tooltip :content="t('minder.hotboxMenu.insetBrother')">
          <MsButton
            type="icon"
            class="ms-minder-node-float-menu-icon-button"
            :class="[insertSiblingMenuVisible ? 'ms-minder-node-float-menu-icon-button--focus' : '']"
          >
            <MsIcon type="icon-icon_title-top-align_outlined1" class="text-[var(--color-text-4)]" />
          </MsButton>
        </a-tooltip>
        <template #content>
          <div class="mx-[6px] px-[8px] py-[3px] text-[var(--color-text-4)]">
            {{ t('minder.hotboxMenu.insetBrother') }}
          </div>
          <a-doption v-for="menu of props.insertSiblingMenus" :key="menu.value" :value="menu.value">
            {{ t(menu.label) }}
          </a-doption>
        </template>
      </a-dropdown>
      <a-dropdown
        v-if="props.insertSonMenus.length > 0"
        v-model:popup-visible="insertSonMenuVisible"
        class="ms-minder-dropdown"
        :popup-translate="[0, 4]"
        position="bl"
        trigger="click"
        @select="(val) => handleMinderMenuSelect('AppendChildNode',val as string)"
      >
        <a-tooltip :content="t('minder.hotboxMenu.insetSon')">
          <MsButton
            type="icon"
            class="ms-minder-node-float-menu-icon-button"
            :class="[insertSonMenuVisible ? 'ms-minder-node-float-menu-icon-button--focus' : '']"
          >
            <MsIcon type="icon-icon_title-left-align_outlined1" class="text-[var(--color-text-4)]" />
          </MsButton>
        </a-tooltip>
        <template #content>
          <div class="mx-[6px] px-[8px] py-[3px] text-[var(--color-text-4)]">
            {{ t('minder.hotboxMenu.insetSon') }}
          </div>
          <a-doption v-for="menu of props.insertSonMenus" :key="menu.value" :value="menu.value">
            {{ t(menu.label) }}
          </a-doption>
        </template>
      </a-dropdown>
      <a-dropdown
        v-if="props.priorityCount && props.canShowPriorityMenu"
        v-model:popup-visible="priorityMenuVisible"
        class="ms-minder-dropdown"
        :popup-translate="[0, 4]"
        position="bl"
        trigger="click"
        @select="(val) => handleMinderMenuSelect('priority',val as string)"
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
      <slot name="extractMenu"></slot>
      <a-dropdown
        v-if="props.canShowMoreMenu"
        v-model:popup-visible="moreMenuVisible"
        class="ms-minder-dropdown"
        :popup-translate="[0, 4]"
        position="bl"
        trigger="click"
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
          <a-doption v-if="props.canShowEnterNode" value="enterNode">
            <div class="flex items-center">
              <div>{{ t('minder.hotboxMenu.enterNode') }}</div>
              <!-- <div class="ml-[4px] text-[var(--color-text-4)]">(Ctrl+ Enter)</div> -->
            </div>
          </a-doption>
          <a-doption value="copy">
            <div class="flex items-center">
              <div>{{ t('minder.hotboxMenu.copy') }}</div>
              <!-- <div class="ml-[4px] text-[var(--color-text-4)]">(Ctrl + C)</div> -->
            </div>
          </a-doption>
          <a-doption value="cut">
            <div class="flex items-center">
              <div>{{ t('minder.hotboxMenu.cut') }}</div>
              <!-- <div class="ml-[4px] text-[var(--color-text-4)]">(Ctrl + X)</div> -->
            </div>
          </a-doption>
          <a-doption v-if="props.canShowPasteMenu && minderStore.clipboard.length > 0" value="paste">
            <div class="flex items-center">
              <div>{{ t('minder.hotboxMenu.paste') }}</div>
              <!-- <div class="ml-[4px] text-[var(--color-text-4)]">(Ctrl + V)</div> -->
            </div>
          </a-doption>
          <a-doption value="delete">
            <div class="flex items-center">
              <div>{{ t('minder.hotboxMenu.delete') }}</div>
              <!-- <div class="ml-[4px] text-[var(--color-text-4)]">(Backspace)</div> -->
            </div>
          </a-doption>
        </template>
      </a-dropdown>
      <a-tooltip v-else-if="props.canShowDeleteMenu" :content="t('common.delete')">
        <MsButton type="icon" class="ms-minder-node-float-menu-icon-button" @click="handleMinderMenuSelect('delete')">
          <MsIcon type="icon-icon_delete-trash_outlined" class="text-[var(--color-text-4)]" />
        </MsButton>
      </a-tooltip>
    </template>
  </a-trigger>
</template>

<script setup lang="ts">
  import { TriggerPopupTranslate } from '@arco-design/web-vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';

  import { useI18n } from '@/hooks/useI18n';
  import useMinderStore from '@/store/modules/components/minder-editor/index';
  import { MinderNodePosition } from '@/store/modules/components/minder-editor/types';
  import { getGenerateId, sleep } from '@/utils';

  import { MinderEventName } from '@/enums/minderEnum';

  import {
    floatMenuProps,
    insertProps,
    mainEditorProps,
    MinderJsonNode,
    MinderJsonNodeData,
    priorityProps,
    tagProps,
  } from '../props';
  import { isDisableNode, isNodeInMinderView, setPriorityView } from '../script/tool/utils';

  const props = defineProps({
    ...mainEditorProps,
    ...floatMenuProps,
    ...insertProps,
    ...tagProps,
    ...priorityProps,
  });
  const emit = defineEmits<{
    (e: 'close'): void;
  }>();

  const { t } = useI18n();
  const minderStore = useMinderStore();

  const currentNodeTags = ref<string[]>([]);
  const tags = ref<string[]>([]);

  const menuVisible = defineModel<boolean>('visible', {
    default: false,
  });
  const menuPopupOffset = ref<TriggerPopupTranslate>([0, 0]);

  watch(
    () => minderStore.event.eventId,
    async () => {
      if (window.minder) {
        let nodePosition: MinderNodePosition | undefined;
        const selectedNodes: MinderJsonNode[] = window.minder.getSelectedNodes();
        if (minderStore.event.name === MinderEventName.NODE_SELECT) {
          nodePosition = minderStore.event.nodePosition;
          currentNodeTags.value = minderStore.event.nodes?.[0].data?.resource || [];
          if (props.replaceableTags && !props.disabled) {
            tags.value = props.replaceableTags(selectedNodes);
          } else {
            tags.value = [];
          }
        }
        if (selectedNodes.length > 1) {
          // 多选时隐藏悬浮菜单 TODO:支持批量操作
          menuVisible.value = false;
          return;
        }
        if ([MinderEventName.VIEW_CHANGE, MinderEventName.DRAG_FINISH].includes(minderStore.event.name)) {
          // 脑图画布移动时，重新计算节点位置
          await sleep(300); // 拖拽完毕后会有 300ms 的动画，等待动画结束后再计算
          nodePosition = window.minder.getSelectedNode()?.getRenderBox();
        }
        if (nodePosition && isNodeInMinderView(undefined, nodePosition, nodePosition.width / 2)) {
          // 判断节点在脑图可视区域内且遮挡的节点不超过节点宽度的一半，则显示菜单
          const nodeDomHeight = nodePosition.height || 0;
          menuPopupOffset.value = [nodePosition.x, nodePosition.y + nodeDomHeight + 4]; // 菜单显示在节点下方4px处
          menuVisible.value = true;
        } else {
          menuVisible.value = false;
        }
      }
    },
    {
      immediate: true,
    }
  );

  const insertSiblingMenuVisible = ref(false);
  const insertSonMenuVisible = ref(false);
  const priorityMenuVisible = ref(false);
  const moreMenuVisible = ref(false);

  /**
   * 执行插入
   * @param command 插入命令
   */
  function execInsertCommand(command: string, value?: string) {
    const node: MinderJsonNode = window.minder.getSelectedNode();
    if (props.insertNode) {
      props.insertNode(node, command, value);
      return;
    }
    if (window.minder.queryCommandState(command) !== -1) {
      window.minder.execCommand(command);
      nextTick(() => {
        const newNode: MinderJsonNode = window.minder.getSelectedNode();
        if (!newNode.data) {
          newNode.data = {
            id: getGenerateId(),
            text: '',
          };
        }
        newNode.data.isNew = true; // 新建的节点标记为新建
      });
    }
  }

  /**
   * 切换标签
   * @param value 切换后的标签
   */
  function handleTagChange(value: string) {
    const selectedNodes: MinderJsonNode[] = window.minder.getSelectedNodes();
    if (selectedNodes.length > 0) {
      const origin = window.minder.queryCommandValue('resource');
      if (props.singleTag) {
        origin.splice(0, origin.length, value);
      } else {
        const index = origin.indexOf(value);
        // 先删除排他的标签
        if (props.distinctTags.indexOf(value) > -1) {
          for (let i = 0; i < origin.length; i++) {
            if (props.distinctTags.indexOf(origin[i]) > -1) {
              origin.splice(i, 1);
              i--;
            }
          }
        }
        if (index !== -1) {
          origin.splice(index, 1);
        } else {
          origin.push(value);
        }
      }
      window.minder.execCommand('resource', origin);
      minderStore.dispatchEvent(MinderEventName.SET_TAG, undefined, undefined, undefined, selectedNodes);
      if (props.afterTagEdit) {
        props.afterTagEdit(selectedNodes, value);
      }
    }
  }

  const priorityColorMap: Record<number, string> = {
    1: 'rgb(var(--danger-6))',
    2: 'rgb(var(--link-6))',
    3: 'rgb(var(--success-6))',
    4: 'rgb(var(--warning-6))',
  };
  const priorityDisabled = ref(true);
  function isDisable(): boolean {
    if (Object.keys(window.minder).length === 0) return true;
    nextTick(() => {
      setPriorityView(props.priorityStartWithZero, props.priorityPrefix);
    });
    const node = window.minder.getSelectedNode();
    if (isDisableNode(window.minder) || !node || node.parent === null) {
      return true;
    }
    if (props.priorityDisableCheck) {
      return props.priorityDisableCheck(node);
    }
    return !!window.minder.queryCommandState && window.minder.queryCommandState('priority') === -1;
  }

  /**
   * 处理快捷菜单选择
   * @param type 选择的菜单项
   */
  function handleMinderMenuSelect(type: string | number | Record<string, any> | undefined, value?: string) {
    const selectedNodes: MinderJsonNode[] = window.minder.getSelectedNodes();
    if (selectedNodes.length > 0) {
      switch (type) {
        case 'AppendChildNode':
          execInsertCommand('AppendChildNode', value);
          minderStore.dispatchEvent(MinderEventName.INSERT_CHILD, value, undefined, undefined, selectedNodes);
          break;
        case 'AppendSiblingNode':
          execInsertCommand('AppendSiblingNode', value);
          minderStore.dispatchEvent(MinderEventName.INSERT_SIBLING, value, undefined, undefined, selectedNodes);
          break;
        case 'copy':
          minderStore.dispatchEvent(MinderEventName.COPY_NODE, undefined, undefined, undefined, selectedNodes);
          window.minder.execCommand('Copy');
          break;
        case 'cut':
          minderStore.dispatchEvent(MinderEventName.CUT_NODE, undefined, undefined, undefined, selectedNodes);
          window.minder.execCommand('Cut');
          break;
        case 'paste':
          minderStore.dispatchEvent(MinderEventName.PASTE_NODE, undefined, undefined, undefined, selectedNodes);
          window.minder.execCommand('Paste');
          let pastedNodes: MinderJsonNode[] = window.minder.getSelectedNodes();
          if (pastedNodes.length > 0) {
            pastedNodes = pastedNodes.map((e) => {
              e.data = {
                ...(e.data as MinderJsonNodeData),
                isNew: true,
                id: getGenerateId(),
              };
              return e;
            });
          }
          break;
        case 'delete':
          minderStore.dispatchEvent(MinderEventName.DELETE_NODE, undefined, undefined, undefined, selectedNodes);
          window.minder.execCommand('RemoveNode');
          break;
        case 'enterNode':
          minderStore.dispatchEvent(MinderEventName.ENTER_NODE, undefined, undefined, undefined, [selectedNodes[0]]);
          break;
        case 'priority':
          if (value && !priorityDisabled.value) {
            window.minder.execCommand('priority', value);
            setPriorityView(props.priorityStartWithZero, props.priorityPrefix);
          } else if (window.minder.execCommand && !priorityDisabled.value) {
            window.minder.execCommand('priority');
          }
          break;
        default:
          break;
      }
    }
    // 保持菜单常显
    nextTick(() => {
      menuVisible.value = true;
    });
  }

  watch(
    () => menuVisible.value,
    (val) => {
      if (!val) {
        emit('close');
      }
    }
  );

  onMounted(() => {
    nextTick(() => {
      const freshFuc = setPriorityView;
      if (window.minder && !props.customPriority) {
        window.minder.on('contentchange', () => {
          // 异步执行，否则执行完，还会被重置
          setTimeout(() => {
            freshFuc(props.priorityStartWithZero, props.priorityPrefix);
          }, 0);
        });
        window.minder.on('selectionchange', () => {
          priorityDisabled.value = isDisable();
        });
      }
    });
  });
</script>

<style lang="less">
  .ms-minder-node-float-menu {
    .arco-trigger-content {
      @apply flex w-auto flex-1 items-center bg-white;

      padding: 4px 8px;
      gap: 8px;
      border-radius: var(--border-radius-small);
      box-shadow: 0 4px 10px -1px rgb(100 100 102 / 15%);
    }
    .ms-minder-node-float-menu-icon-button {
      @apply !mr-0;
      &:hover {
        background-color: rgb(var(--primary-1)) !important;
        .arco-icon {
          color: rgb(var(--primary-4)) !important;
        }
      }
    }
    .ms-minder-node-float-menu-icon-button--focus {
      background-color: rgb(var(--primary-1)) !important;
      .arco-icon {
        color: rgb(var(--primary-5)) !important;
      }
    }
  }
</style>
