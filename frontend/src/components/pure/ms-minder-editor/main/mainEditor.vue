<template>
  <div ref="mec" class="ms-minder-container">
    <minderHeader :icon-buttons="props.iconButtons" @save="save" />
    <Navigator />
    <div
      v-if="innerImportJson.treePath?.length > 1"
      class="absolute left-[50%] top-[24px] z-50 translate-x-[-50%] bg-white p-[8px]"
    >
      <a-breadcrumb>
        <a-breadcrumb-item v-for="crumb of innerImportJson.treePath" :key="crumb.name" @click="switchNode(crumb)">
          {{ crumb.text }}
        </a-breadcrumb-item>
      </a-breadcrumb>
    </div>
    <nodeFloatMenu v-bind="props">
      <template #extractMenu>
        <slot name="extractMenu"></slot>
      </template>
    </nodeFloatMenu>
  </div>
</template>

<script lang="ts" name="minderContainer" setup>
  import { onMounted, ref, watch } from 'vue';

  import nodeFloatMenu from '../menu/nodeFloatMenu.vue';
  import minderHeader from './header.vue';
  import Navigator from './navigator.vue';

  import useMinderStore from '@/store/modules/components/minder-editor';
  import { findNodePathByKey, replaceNodeInTree } from '@/utils';

  import { MinderEventName } from '@/enums/minderEnum';

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
  }>();

  const minderStore = useMinderStore();
  const mec: Ref<HTMLDivElement | null> = ref(null);
  const importJson = defineModel<MinderJson>('importJson', {
    required: true,
  });
  const innerImportJson = ref<MinderJson>({
    root: {},
    template: 'default',
    treePath: [],
  });
  const minderUnsaved = ref(false);

  function handlePriorityButton() {
    const { priorityPrefix } = props;
    const { priorityStartWithZero } = props;
    let start = priorityStartWithZero ? 0 : 1;
    let res = '';
    for (let i = 0; i < props.priorityCount; i++) {
      res += start++;
    }
    const priority = window.minder.hotbox.state('priority');
    res.replace(/./g, (p) => {
      priority.button({
        position: 'ring',
        label: priorityPrefix + p,
        key: p,
        action() {
          const pVal = parseInt(p, 10);
          window.minder.execCommand('Priority', priorityStartWithZero ? pVal + 1 : pVal);
        },
      });
      // 需要返回字符串
      return '';
    });
  }
  function handleTagButton() {
    const tag = window.minder.hotbox.state('tag');
    props.tags?.forEach((item) => {
      tag.button({
        position: 'ring',
        label: item,
        key: item,
        action() {
          window.minder.execCommand('resource', item);
        },
      });
    });
  }

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

    window.minder.on('preExecCommand', (env: any) => {
      const selectNodes = env.minder.getSelectedNodes();
      const notChangeCommands = new Set([
        'camera',
        'copy',
        'expand',
        'expandToLevel',
        'hand',
        'layout',
        'template',
        'theme',
        'zoom',
        'zoomIn',
        'zoomOut',
      ]);
      if (selectNodes && !notChangeCommands.has(env.commandName.toLocaleLowerCase())) {
        minderUnsaved.value = true;
        minderStore.dispatchEvent(MinderEventName.MINDER_CHANGED);
        selectNodes.forEach((node: MinderJsonNode) => {
          markChangeNode(node);
        });
      }
      if (env.commandName === 'movetoparent') {
        setTimeout(() => {
          const targetNode = window.minder.getSelectedNode();
          targetNode.parent.renderTree();
        }, 100);
      }
    });

    handlePriorityButton();
    handleTagButton();
    emit('afterMount');
  }

  onMounted(async () => {
    init();
  });

  const menuVisible = ref(false);
  const menuPopupOffset = ref([0, 0]);

  /**
   * 切换脑图展示的节点层级
   * @param node 切换的节点
   */
  function switchNode(node: MinderJsonNode | MinderJsonNodeData) {
    if (minderUnsaved.value) {
      // 切换前，如果脑图未保存，先把更改的节点信息同步一次
      replaceNodeInTree(
        [importJson.value.root],
        innerImportJson.value.root.data?.id || '',
        window.minder.exportJson()?.root,
        'data',
        'id'
      );
    }
    if (node.data) {
      innerImportJson.value = findNodePathByKey([importJson.value.root], node.data.id, 'data', 'id') as MinderJson;
    } else {
      innerImportJson.value = findNodePathByKey([importJson.value.root], node.id, 'data', 'id') as MinderJson;
    }
    window.minder.importJson(innerImportJson.value);
    setTimeout(() => {
      window.minder.select(window.minder.getRoot());
      window.minder.execCommand('camera', window.minder.getRoot());
    }, 100); // TODO:暂未知渲染时机，临时延迟解决
  }

  watch(
    () => minderStore.event.timestamp,
    () => {
      if (minderStore.event.name === MinderEventName.HOTBOX && minderStore.event.nodePosition) {
        const nodeDomWidth = minderStore.event.nodeDom?.getBoundingClientRect().width || 0;
        menuPopupOffset.value = [
          minderStore.event.nodePosition.x + nodeDomWidth / 2,
          minderStore.event.nodePosition.y - nodeDomWidth / 4,
        ];
        menuVisible.value = true;
      }
      if (minderStore.event.name === MinderEventName.ENTER_NODE && minderStore.event.nodes) {
        switchNode(minderStore.event.nodes[0]);
      }
    }
  );

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
      data = window.minder.exportJson();
    }
    emit('save', data, () => {
      minderUnsaved.value = false;
      menuVisible.value = false;
    });
  }
</script>

<style lang="less">
  @import '../style/editor.less';
  .save-btn {
    @apply !absolute;
  }
  .ms-minder-container {
    @apply relative overflow-hidden !bg-white;

    padding: 16px 0;
    height: calc(100% - 60px);
  }
  .ms-minder-dropdown {
    .arco-dropdown-list-wrapper {
      max-height: none;
    }
  }
</style>
