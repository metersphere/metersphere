<template>
  <div ref="mec" class="ms-minder-container">
    <minderHeader
      :minder-key="props.minderKey"
      :icon-buttons="props.iconButtons"
      :disabled="props.disabled"
      @save="save"
    />
    <Navigator :shortcut-list="props.shortcutList">
      <template #shortCutList>
        <slot name="shortCutList"></slot>
      </template>
    </Navigator>
    <div
      v-if="currentTreePath?.length > 0"
      class="absolute left-[50%] top-[16px] z-[9] w-[60%] translate-x-[-50%] overflow-hidden bg-[var(--color-text-fff)] p-[8px]"
    >
      <a-menu v-model:selected-keys="selectedBreadcrumbKeys" mode="horizontal" class="ms-minder-breadcrumb">
        <a-menu-item v-for="(crumb, i) of currentTreePath" :key="crumb.id" @click="switchNode(crumb, i)">
          <a-tooltip :content="crumb.text" :mouse-enter-delay="300">
            <div class="one-line-text">{{ crumb.text }}</div>
          </a-tooltip>
        </a-menu-item>
      </a-menu>
    </div>
    <nodeFloatMenu
      v-if="props.canShowFloatMenu"
      v-bind="props"
      v-model:visible="floatMenuVisible"
      @close="emit('floatMenuClose')"
    >
      <template #extractMenu>
        <slot name="extractMenu"></slot>
      </template>
    </nodeFloatMenu>
    <nodeDropdown v-if="props.canShowDropdown" :dropdown-list="props.dropdownList" :checked-val="props.checkedVal" />
    <batchMenu v-bind="props">
      <template #batchMenu>
        <slot name="batchMenu"></slot>
      </template>
    </batchMenu>
  </div>
</template>

<script lang="ts" name="minderContainer" setup>
  import { cloneDeep } from 'lodash-es';

  import batchMenu from '../menu/batchMenu.vue';
  import nodeDropdown from '../menu/nodeDropdown.vue';
  import nodeFloatMenu from '../menu/nodeFloatMenu.vue';
  import minderHeader from './header.vue';
  import Navigator from './navigator.vue';

  import useLeaveUnSaveTip from '@/hooks/useLeaveUnSaveTip';
  import useAppStore from '@/store/modules/app';
  import useMinderStore from '@/store/modules/components/minder-editor';
  import { findNodePathByKey, mapTree, replaceNodeInTree } from '@/utils';

  import { MinderEventName } from '@/enums/minderEnum';

  import useEventListener from '../hooks/useMinderEventListener';
  import {
    batchMenuProps,
    dropdownMenuProps,
    editMenuProps,
    floatMenuProps,
    headerProps,
    mainEditorProps,
    MinderJson,
    MinderJsonNode,
    MinderJsonNodeData,
    navigatorProps,
    priorityProps,
    tagProps,
  } from '../props';
  import Editor from '../script/editor';
  import { markChangeNode, markDeleteNode } from '../script/tool/utils';
  import type { Ref } from 'vue';

  const props = defineProps({
    ...headerProps,
    ...floatMenuProps,
    ...editMenuProps,
    ...mainEditorProps,
    ...tagProps,
    ...priorityProps,
    ...batchMenuProps,
    ...dropdownMenuProps,
    ...navigatorProps,
  });
  const emit = defineEmits<{
    (e: 'save', data: MinderJson, callback: () => void): void;
    (e: 'afterMount'): void;
    (e: 'floatMenuClose'): void;
  }>();

  const appStore = useAppStore();
  const minderStore = useMinderStore();
  const { setIsSave } = useLeaveUnSaveTip({
    leaveContent: 'ms.minders.leaveUnsavedTip',
    leaveTitle: 'common.unSaveLeaveTitle',
    tipType: 'warning',
  });
  const mec: Ref<HTMLDivElement | null> = ref(null);
  const importJson = defineModel<MinderJson>('importJson', {
    required: true,
  });
  const innerImportJson = ref<MinderJson>({
    root: {},
    treePath: [],
  });
  const currentTreePath = ref<MinderJsonNodeData[]>([]);

  const floatMenuVisible = ref(false);

  async function init() {
    window.editor = new Editor(mec.value, {
      sequenceEnable: props.sequenceEnable,
      tagEnable: props.tagEnable,
      progressEnable: props.progressEnable,
      moveEnable: props.moveEnable,
    });
    const { editor } = window;
    if (Object.keys(importJson.value || {}).length > 0) {
      editor.minder.importJson(importJson.value);
    }
    window.km = editor.minder;
    window.minder = window.km;
    window.minderEditor = editor;
    window.minder.moveEnable = props.moveEnable;
    if (appStore.isDarkTheme) {
      window.minder.setDefaultOptions({
        defaultTheme: 'wire',
      });
    } else {
      window.minder.setDefaultOptions({
        defaultTheme: 'fresh-purple',
      });
    }
    window.minder.refresh();
    window.minder.forceRemoveNode = () => {
      markDeleteNode(window.minder);
      window.minder.execCommand('RemoveNode');
    };
    emit('afterMount');
  }

  onMounted(() => {
    init();
    useEventListener({
      handleBeforeExecCommand(event) {
        const selectNodes: MinderJsonNode[] = event.minder.getSelectedNodes();
        const notChangeCommands = new Set([
          'camera',
          'copy',
          'expand',
          'expandtolevel',
          'hand',
          'layout',
          'template',
          'theme',
          'zoom',
          'zoomin',
          'zoomout',
        ]);
        if (props.disabled) {
          ['movetoparent', 'arrange'].forEach((item) => notChangeCommands.add(item));
        }
        if (
          !props.disabled &&
          selectNodes.length > 0 &&
          !notChangeCommands.has(event.commandName.toLocaleLowerCase())
        ) {
          minderStore.setMinderUnsaved(true);
          selectNodes.forEach((node: MinderJsonNode) => {
            markChangeNode(node);
          });
        }
        if (event.commandName === 'movetoparent') {
          setTimeout(() => {
            const targetNode = window.minder.getSelectedNode();
            targetNode.parent.renderTree();
          }, 100);
        }
      },
      handleDblclick() {
        const state = window.editor.fsm.state();
        if (state === 'input') {
          minderStore.setMinderUnsaved(true);
          floatMenuVisible.value = false;
        }
      },
    });
  });

  function getCurrentTreePath() {
    if (innerImportJson.value.root.id === 'NONE' || innerImportJson.value.treePath?.length <= 1) {
      return [];
    }
    const index = innerImportJson.value.treePath?.findIndex((e) => e.id === innerImportJson.value.root.data?.id);
    return innerImportJson.value.treePath?.filter((e, i) => i <= index) || [];
  }

  const selectedBreadcrumbKeys = computed({
    get: () => [currentTreePath.value[currentTreePath.value.length - 1]?.id],
    set: (val) => {
      return val;
    },
  });

  /**
   * 切换脑图展示的节点层级
   * @param node 切换的节点
   */
  async function switchNode(node?: MinderJsonNode | MinderJsonNodeData, index?: number) {
    if (!props.minderKey || index === currentTreePath.value.length - 1) return;
    const currentSelectedNodes: MinderJsonNode[] = window.minder.getSelectedNodes();
    if (currentSelectedNodes && currentSelectedNodes.length > 0) {
      // 切换前，如果有选中节点，先取消选中
      window.minder.toggleSelect(currentSelectedNodes);
    }
    if (minderStore.minderUnsaved) {
      // 切换前，如果脑图未保存，先把更改的节点信息同步一次
      replaceNodeInTree(
        [importJson.value.root],
        innerImportJson.value.root.data?.id || '',
        window.minder.exportJson()?.root,
        'data',
        'id'
      );
    }
    if (node?.id === 'NONE') {
      innerImportJson.value = importJson.value;
    } else if (node?.data) {
      innerImportJson.value = findNodePathByKey([importJson.value.root], node.data.id, 'data', 'id') as MinderJson;
    } else {
      innerImportJson.value = findNodePathByKey([importJson.value.root], node?.id, 'data', 'id') as MinderJson;
    }
    const template = await minderStore.getMode(props.minderKey);
    importJson.value.template = template;
    window.minder.importJson(innerImportJson.value);
    await minderStore.setMode(props.minderKey, importJson.value.template);
    const root: MinderJsonNode = window.minder.getRoot();
    window.minder.toggleSelect(root); // 先取消选中
    window.minder.select(root); // 再选中，才能触发选中变化事件
    window.minder.execCommand('ExpandToLevel', 1);
    currentTreePath.value = getCurrentTreePath();
    setTimeout(() => {
      window.minder.execCommand('camera', root);
    }, 300); // TODO:暂未知渲染时机，临时延迟解决
  }

  function save() {
    let data = importJson.value;
    if (innerImportJson.value.treePath?.length > 1) {
      replaceNodeInTree(
        [importJson.value.root],
        innerImportJson.value.root.data?.id || '',
        window.minder.exportJson()?.root,
        'data',
        'id'
      );
    } else {
      const fullJson = window.minder.exportJson();
      data = cloneDeep(fullJson);
      importJson.value = fullJson;
    }
    emit('save', data, (refresh = false) => {
      importJson.value.root.children = mapTree<MinderJsonNode>(
        importJson.value.root.children || [],
        (node, path, level) => ({
          ...node,
          data: {
            ...node.data,
            level,
            isNew: false,
            changed: false,
          },
        })
      );
      if (refresh) {
        if (innerImportJson.value.treePath?.length > 1) {
          switchNode(innerImportJson.value.root.data);
        } else {
          innerImportJson.value = importJson.value;
          window.minder.importJson(importJson.value);
        }
      }
      minderStore.setMinderUnsaved(false);
      floatMenuVisible.value = false;
    });
  }

  watch(
    () => minderStore.event.eventId,
    () => {
      if (minderStore.event.name === MinderEventName.ENTER_NODE && minderStore.event.nodes) {
        switchNode(minderStore.event.nodes[0]);
      }
      if (minderStore.event.name === MinderEventName.SAVE_MINDER) {
        save();
      }
    }
  );

  watch(
    () => minderStore.getMinderUnsaved,
    (val) => {
      setIsSave(!val);
    },
    {
      immediate: true,
    }
  );

  watch(
    () => importJson.value.treePath,
    (arr) => {
      currentTreePath.value = arr || [];
    }
  );

  watch(
    () => appStore.isDarkTheme,
    (val) => {
      if (val) {
        window.minder.useTheme('wire');
      } else {
        window.minder.useTheme('fresh-purple');
      }
    }
  );

  onBeforeUnmount(() => {
    minderStore.setMinderUnsaved(false);
  });
</script>

<style lang="less">
  @import '@/assets/icon-font/iconfont.css';
  @import '../style/editor.less';
  .save-btn {
    @apply !absolute;
  }
  .ms-minder-container {
    @apply relative h-full overflow-hidden;

    background-color: var(--color-text-fff) !important;
  }
  .ms-minder-dropdown {
    .arco-dropdown-list-wrapper {
      max-height: none;
    }
  }
  .ms-minder-breadcrumb {
    @apply p-0;

    background-color: var(--color-text-fff);
    .arco-menu-inner {
      @apply p-0;

      background-color: var(--color-text-fff);
      .arco-menu-item {
        @apply relative p-0;

        padding-right: 24px;
        max-width: 200px;
        &:hover {
          color: rgb(var(--primary-4));
        }
        &:not(:last-child)::after {
          @apply absolute;

          top: 50%;
          right: 7px;
          font-size: 12px;
          font-family: iconfont;
          content: '\e6fe';
          color: var(--color-text-brand);
          line-height: 16px;
          transform: translateY(-50%);
        }
      }
      .arco-menu-item,
      .arco-menu-overflow-sub-menu {
        @apply ml-0;

        color: var(--color-text-4);
        background-color: var(--color-text-fff);
      }
      .arco-menu-selected {
        color: rgb(var(--primary-4));
        &:hover {
          background-color: var(--color-text-fff) !important;
        }
      }
      .arco-menu-pop::after,
      .arco-menu-selected-label {
        @apply hidden;
      }
    }
  }
</style>
