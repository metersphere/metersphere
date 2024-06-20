<template>
  <div ref="mec" class="ms-minder-container">
    <minderHeader :icon-buttons="props.iconButtons" :disabled="props.disabled" @save="save" />
    <Navigator />
    <div
      v-if="currentTreePath?.length > 0"
      class="absolute left-[50%] top-[24px] z-50 translate-x-[-50%] bg-white p-[8px]"
    >
      <a-breadcrumb>
        <a-breadcrumb-item v-for="crumb of currentTreePath" :key="crumb.name" @click="switchNode(crumb)">
          {{ crumb.text }}
        </a-breadcrumb-item>
      </a-breadcrumb>
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
  </div>
</template>

<script lang="ts" name="minderContainer" setup>
  import { cloneDeep } from 'lodash-es';

  import nodeFloatMenu from '../menu/nodeFloatMenu.vue';
  import minderHeader from './header.vue';
  import Navigator from './navigator.vue';

  import useLeaveUnSaveTip from '@/hooks/useLeaveUnSaveTip';
  import useMinderStore from '@/store/modules/components/minder-editor';
  import { findNodePathByKey, mapTree, replaceNodeInTree } from '@/utils';

  import { MinderEventName } from '@/enums/minderEnum';

  import useEventListener from '../hooks/useMinderEventListener';
  import {
    editMenuProps,
    floatMenuProps,
    headerProps,
    insertProps,
    mainEditorProps,
    MinderJson,
    MinderJsonNode,
    MinderJsonNodeData,
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
    ...insertProps,
    ...mainEditorProps,
    ...tagProps,
    ...priorityProps,
  });
  const emit = defineEmits<{
    (e: 'save', data: MinderJson, callback: () => void): void;
    (e: 'afterMount'): void;
    (e: 'floatMenuClose'): void;
  }>();

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
    template: 'default',
    treePath: [],
  });
  const currentTreePath = ref<MinderJsonNodeData[]>([]);

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
          'hand',
          'layout',
          'template',
          'theme',
          'zoom',
          'zoomin',
          'zoomout',
          'appendchildnode',
          'appendsiblingnode',
        ]);
        if (selectNodes.length > 0 && !notChangeCommands.has(event.commandName.toLocaleLowerCase())) {
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
    });
  });

  function getCurrentTreePath() {
    if (innerImportJson.value.root.id === 'NONE' || innerImportJson.value.treePath?.length <= 1) {
      return [];
    }
    const index = innerImportJson.value.treePath?.findIndex((e) => e.id === innerImportJson.value.root.data?.id);
    return innerImportJson.value.treePath?.filter((e, i) => i <= index) || [];
  }

  /**
   * 切换脑图展示的节点层级
   * @param node 切换的节点
   */
  function switchNode(node?: MinderJsonNode | MinderJsonNodeData) {
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
    window.minder.importJson(innerImportJson.value);
    const root: MinderJsonNode = window.minder.getRoot();
    window.minder.toggleSelect(root); // 先取消选中
    window.minder.select(root); // 再选中，才能触发选中变化事件
    window.minder.execCommand('ExpandToLevel', 1);
    currentTreePath.value = getCurrentTreePath();
    setTimeout(() => {
      window.minder.execCommand('camera', root);
    }, 100); // TODO:暂未知渲染时机，临时延迟解决
  }

  const floatMenuVisible = ref(false);

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
    emit('save', data, () => {
      importJson.value.root.children = mapTree<MinderJsonNode>(importJson.value.root.children || [], (node) => ({
        ...node,
        data: {
          ...node.data,
          isNew: false,
          changed: false,
        },
      }));
      if (innerImportJson.value.treePath?.length > 1) {
        switchNode(innerImportJson.value.root.data);
      } else {
        innerImportJson.value = importJson.value;
        window.minder.importJson(importJson.value);
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
      currentTreePath.value = arr;
    }
  );

  onBeforeUnmount(() => {
    minderStore.setMinderUnsaved(false);
  });
</script>

<style lang="less">
  @import '../style/editor.less';
  .save-btn {
    @apply !absolute;
  }
  .ms-minder-container {
    @apply relative h-full overflow-hidden !bg-white;
  }
  .ms-minder-dropdown {
    .arco-dropdown-list-wrapper {
      max-height: none;
    }
  }
</style>
