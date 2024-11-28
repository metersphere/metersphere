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
        <a-radio v-for="tag of tags.filter((e) => e !== currentNodeTags[0])" :key="tag" :value="tag">{{ tag }}</a-radio>
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
            <MsIcon type="icon-icon_title-top-align_outlined" class="text-[var(--color-text-4)]" />
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
            <MsIcon type="icon-icon_title-left-align_outlined" class="text-[var(--color-text-4)]" />
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
          <a-doption v-if="props.canShowEnterNode" value="enterNode">
            <div class="flex items-center">
              <div>{{ t('minder.hotboxMenu.enterNode') }}</div>
              <div class="ml-[4px] text-[var(--color-text-4)]">(<MsCtrlOrCommand /> + Enter)</div>
            </div>
          </a-doption>
          <template v-if="props.canShowMoreMenuNodeOperation">
            <a-doption value="copy">
              <div class="flex items-center">
                <div>{{ t('minder.hotboxMenu.copy') }}</div>
                <div class="ml-[4px] text-[var(--color-text-4)]">(<MsCtrlOrCommand /> + C)</div>
              </div>
            </a-doption>
            <a-doption value="cut">
              <div class="flex items-center">
                <div>{{ t('minder.hotboxMenu.cut') }}</div>
                <div class="ml-[4px] text-[var(--color-text-4)]">(<MsCtrlOrCommand /> + X)</div>
              </div>
            </a-doption>
            <a-doption v-if="props.canShowPasteMenu && minderStore.clipboard.length > 0" value="paste">
              <div class="flex items-center">
                <div>{{ t('minder.hotboxMenu.paste') }}</div>
                <div class="ml-[4px] text-[var(--color-text-4)]">(<MsCtrlOrCommand /> + V)</div>
              </div>
            </a-doption>
            <a-doption value="delete">
              <div class="flex items-center">
                <div>{{ t('minder.hotboxMenu.delete') }}</div>
                <div class="ml-[4px] text-[var(--color-text-4)]">(Backspace)</div>
              </div>
            </a-doption>
          </template>
          <a-doption
            v-for="item in props.moreMenuOtherOperationList"
            :key="item.value"
            v-permission="item.permission || []"
            :value="item.value"
            @click="item.onClick()"
          >
            <div>{{ item.label }}</div>
          </a-doption>
        </template>
      </a-dropdown>
      <a-tooltip v-else-if="props.canShowDeleteMenu" :content="t('common.delete')">
        <MsButton type="icon" class="ms-minder-node-float-menu-icon-button" @click="handleMinderMenuSelect('delete')">
          <MsIcon type="icon-icon_delete-trash_outlined1" class="text-[var(--color-text-4)]" />
        </MsButton>
      </a-tooltip>
    </template>
  </a-trigger>
</template>

<script setup lang="ts">
  import { TriggerPopupTranslate } from '@arco-design/web-vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsCtrlOrCommand from '@/components/pure/ms-ctrl-or-command';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';

  import { useI18n } from '@/hooks/useI18n';
  import useMinderStore from '@/store/modules/components/minder-editor/index';

  import { MinderEventName } from '@/enums/minderEnum';

  import useMinderOperation from '../hooks/useMinderOperation';
  import usePriority from '../hooks/useMinderPriority';
  import useMinderTrigger from '../hooks/useMinderTrigger';
  import { floatMenuProps, mainEditorProps, MinderJsonNode, priorityColorMap, priorityProps, tagProps } from '../props';

  const props = defineProps({
    ...mainEditorProps,
    ...floatMenuProps,
    ...tagProps,
    ...priorityProps,
  });
  const emit = defineEmits<{
    (e: 'close'): void;
  }>();

  const { t } = useI18n();
  const minderStore = useMinderStore();
  const { setPriority } = usePriority(props);

  const currentNodeTags = ref<string[]>([]);
  const tags = ref<string[]>([]);

  const menuVisible = defineModel<boolean>('visible', {
    default: false,
  });
  const menuPopupOffset = ref<TriggerPopupTranslate>([0, 0]);

  const { triggerOffset, triggerVisible } = useMinderTrigger((event, selectedNodes) => {
    currentNodeTags.value = event.nodes?.[0].data?.resource || [];
    if (props.replaceableTags && !props.disabled) {
      tags.value = props.replaceableTags(selectedNodes);
    } else {
      tags.value = [];
    }
  });

  watch(
    () => triggerOffset.value,
    (val) => {
      menuPopupOffset.value = [val[0], val[1]];
    },
    {
      immediate: true,
    }
  );

  watch(
    () => triggerVisible.value,
    (val) => {
      menuVisible.value = val;
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

  const { minderCopy, minderCut, minderPaste, appendChildNode, appendSiblingNode, minderDelete } =
    useMinderOperation(props);

  /**
   * 处理快捷菜单选择
   * @param type 选择的菜单项
   */
  function handleMinderMenuSelect(type: string | number | Record<string, any> | undefined, value?: string) {
    const selectedNodes: MinderJsonNode[] = window.minder.getSelectedNodes();
    if (selectedNodes.length > 0) {
      switch (type) {
        case 'AppendChildNode':
          appendChildNode(selectedNodes, value);
          break;
        case 'AppendSiblingNode':
          appendSiblingNode(selectedNodes, value);
          break;
        case 'copy':
          minderCopy();
          break;
        case 'cut':
          minderCut();
          break;
        case 'paste':
          minderPaste();
          break;
        case 'delete':
          minderDelete(selectedNodes);
          break;
        case 'enterNode':
          minderStore.dispatchEvent(MinderEventName.ENTER_NODE, undefined, undefined, undefined, [selectedNodes[0]]);
          break;
        case 'priority':
          setPriority(value);
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
</script>

<style lang="less">
  .ms-minder-node-float-menu {
    .arco-trigger-content {
      @apply flex w-auto flex-1 items-center;

      padding: 4px 8px;
      border-radius: var(--border-radius-small);
      background-color: var(--color-text-fff);
      box-shadow: 0 4px 10px -1px rgb(100 100 102 / 15%);
      gap: 8px;
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
