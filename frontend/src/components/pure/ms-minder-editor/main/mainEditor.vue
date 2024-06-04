<template>
  <div ref="mec" class="minder-container">
    <a-button type="primary" :disabled="props.disabled" class="save-btn bottom-[30px] right-[30px]" @click="save">
      {{ t('minder.main.main.save') }}
    </a-button>
    <Navigator />
    <a-dropdown
      v-model:popup-visible="menuVisible"
      class="minder-dropdown"
      position="bl"
      :popup-translate="menuPopupOffset"
      @select="handleMinderMenuSelect"
    >
      <span></span>
      <template #content>
        <a-doption value="expand">
          <div class="flex items-center">
            <div>{{ t('minder.hotboxMenu.expand') }}</div>
            <div class="ml-[4px] text-[var(--color-text-4)]">( / )</div>
          </div>
        </a-doption>
        <a-doption value="insetSon">
          <div class="flex items-center">
            <div>{{ t('minder.hotboxMenu.insetSon') }}</div>
            <div class="ml-[4px] text-[var(--color-text-4)]">(Tab)</div>
          </div>
        </a-doption>
        <a-doption value="insetBrother">
          <div class="flex items-center">
            <div>{{ t('minder.hotboxMenu.insetBrother') }}</div>
            <div class="ml-[4px] text-[var(--color-text-4)]">(Enter)</div>
          </div>
        </a-doption>
        <a-doption value="copy">
          <div class="flex items-center">
            <div>{{ t('minder.hotboxMenu.copy') }}</div>
            <div class="ml-[4px] text-[var(--color-text-4)]">(Ctrl + C)</div>
          </div>
        </a-doption>
        <a-doption value="cut">
          <div class="flex items-center">
            <div>{{ t('minder.hotboxMenu.cut') }}</div>
            <div class="ml-[4px] text-[var(--color-text-4)]">(Ctrl + X)</div>
          </div>
        </a-doption>
        <a-doption value="paste">
          <div class="flex items-center">
            <div>{{ t('minder.hotboxMenu.paste') }}</div>
            <div class="ml-[4px] text-[var(--color-text-4)]">(Ctrl + V)</div>
          </div>
        </a-doption>
        <a-doption value="delete">
          <div class="flex items-center">
            <div>{{ t('minder.hotboxMenu.delete') }}</div>
            <div class="ml-[4px] text-[var(--color-text-4)]">(Backspace)</div>
          </div>
        </a-doption>
        <a-doption value="enterNode">
          <div class="flex items-center">
            <div>{{ t('minder.hotboxMenu.enterNode') }}</div>
            <div class="ml-[4px] text-[var(--color-text-4)]">(Ctrl+ Enter)</div>
          </div>
        </a-doption>
      </template>
    </a-dropdown>
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
  </div>
</template>

<script lang="ts" name="minderContainer" setup>
  import { onMounted, ref, watch } from 'vue';
  import { cloneDeep } from 'lodash-es';

  import Navigator from './navigator.vue';

  import { useI18n } from '@/hooks/useI18n';
  import useMinderStore from '@/store/modules/components/minder-editor';
  import { findNodePathByKey, getGenerateId } from '@/utils';

  import { MinderEventName } from '@/enums/minderEnum';

  import {
    editMenuProps,
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

  const { t } = useI18n();
  const props = defineProps({ ...editMenuProps, ...insertProps, ...mainEditorProps, ...tagProps, ...priorityProps });

  const emit = defineEmits<{
    (e: 'afterMount'): void;
    (e: 'save', json: MinderJson): void;
  }>();

  const minderStore = useMinderStore();
  const mec: Ref<HTMLDivElement | null> = ref(null);
  const innerImportJson = ref<any>({});

  function save() {
    emit('save', window.minder.exportJson());
  }
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
    if (Object.keys(props.importJson || {}).length > 0) {
      editor.minder.importJson(props.importJson);
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
        'append',
        'appendchildnode',
        'appendsiblingnode',
      ]);
      if (selectNodes && !notChangeCommands.has(env.commandName.toLocaleLowerCase())) {
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
    if (node.data) {
      innerImportJson.value = cloneDeep(findNodePathByKey([props.importJson.root], node.data.id, 'data', 'id'));
    } else {
      innerImportJson.value = cloneDeep(findNodePathByKey([props.importJson.root], node.id, 'data', 'id'));
    }
    innerImportJson.value.data.expandState = 'expand';
    window.minder.importJson(innerImportJson.value);
    setTimeout(() => {
      window.minder.execCommand('camera', window.minder.getRoot(), 600);
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

  /**
   * 执行插入
   * @param command 插入命令
   */
  function execInsertCommand(command: string) {
    const node: MinderJsonNode = window.minder.getSelectedNode();
    if (props.insertNode) {
      props.insertNode(node, command);
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
   * 处理快捷菜单选择
   * @param val 选择的菜单项
   */
  function handleMinderMenuSelect(val: string | number | Record<string, any> | undefined) {
    const selectedNodes: MinderJsonNode[] = window.minder.getSelectedNodes();
    if (selectedNodes.length > 0) {
      switch (val) {
        case 'expand':
          if (selectedNodes.some((node) => node.data?.expandState === 'collapse')) {
            window.minder.execCommand('Expand');
          } else {
            window.minder.execCommand('Collapse');
          }
          minderStore.dispatchEvent(MinderEventName.EXPAND, undefined, undefined, selectedNodes);
          break;
        case 'insetParent':
          execInsertCommand('AppendParentNode');
          minderStore.dispatchEvent(MinderEventName.INSERT_PARENT, undefined, undefined, selectedNodes);
          break;
        case 'insetSon':
          execInsertCommand('AppendChildNode');
          minderStore.dispatchEvent(MinderEventName.INSERT_CHILD, undefined, undefined, selectedNodes);
          break;
        case 'insetBrother':
          execInsertCommand('AppendSiblingNode');
          minderStore.dispatchEvent(MinderEventName.INSERT_SIBLING, undefined, undefined, selectedNodes);
          break;
        case 'copy':
          window.minder.execCommand('Copy');
          minderStore.dispatchEvent(MinderEventName.COPY_NODE, undefined, undefined, selectedNodes);
          break;
        case 'cut':
          window.minder.execCommand('Cut');
          minderStore.dispatchEvent(MinderEventName.CUT_NODE, undefined, undefined, selectedNodes);
          break;
        case 'paste':
          window.minder.execCommand('Paste');
          minderStore.dispatchEvent(MinderEventName.PASTE_NODE, undefined, undefined, selectedNodes);
          break;
        case 'delete':
          window.minder.execCommand('RemoveNode');
          minderStore.dispatchEvent(MinderEventName.DELETE_NODE, undefined, undefined, selectedNodes);
          break;
        case 'enterNode':
          switchNode(selectedNodes[0]);
          minderStore.dispatchEvent(MinderEventName.ENTER_NODE, undefined, undefined, [selectedNodes[0]]);
          break;
        default:
          break;
      }
    }
  }
</script>

<style lang="less">
  @import '../style/editor.less';
  .save-btn {
    @apply !absolute;
  }
  .minder-container {
    @apply relative !bg-white;

    height: calc(100% - 60px);
  }
  .minder-dropdown {
    .arco-dropdown-list-wrapper {
      max-height: none;
    }
  }
</style>
