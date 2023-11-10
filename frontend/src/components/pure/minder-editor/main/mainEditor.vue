<template>
  <div ref="mec" class="minder-container" :style="{ height: `${props.height}px` }">
    <a-button type="primary" :disabled="props.disabled" class="save-btn bottom-[30px] right-[30px]" @click="save">
      {{ t('minder.main.main.save') }}
    </a-button>
    <navigator />
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
        <a-doption value="insetParent">
          <div class="flex items-center">
            <div>{{ t('minder.hotboxMenu.insetParent') }}</div>
            <div class="ml-[4px] text-[var(--color-text-4)]">(Shift + Tab)</div>
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
  import { findNodePathByKey } from '@/utils';

  import { editMenuProps, mainEditorProps, priorityProps, tagProps } from '../props';
  import Editor from '../script/editor';
  import { markChangeNode, markDeleteNode } from '../script/tool/utils';
  import type { Ref } from 'vue';

  const { t } = useI18n();
  const props = defineProps({ ...editMenuProps, ...mainEditorProps, ...tagProps, ...priorityProps });

  const emit = defineEmits({
    afterMount: () => ({}),
    save: (json) => json,
    enterNode: (data) => data,
  });

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
        selectNodes.forEach((node: any) => {
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

  watch(
    () => props.importJson,
    (val) => {
      innerImportJson.value = val;
      window.minder.importJson(val);
    }
  );

  const menuVisible = ref(false);
  const menuPopupOffset = ref([0, 0]);

  function switchNode(node: any) {
    innerImportJson.value = cloneDeep(findNodePathByKey([props.importJson.root], node.id, 'data', 'id'));
    innerImportJson.value.data.expandState = 'expand';
    window.minder.importJson(innerImportJson.value);
    window.minder.execCommand('template', Object.keys(window.kityminder.Minder.getTemplateList())[minderStore.mold]);
  }

  watch(
    () => minderStore.event.timestamp,
    () => {
      if (minderStore.event.name === 'hotbox') {
        const nodeDomWidth = minderStore.event.nodeDom?.getBoundingClientRect().width || 0;
        menuPopupOffset.value = [
          minderStore.event.nodePosition.x + nodeDomWidth / 2,
          minderStore.event.nodePosition.y - nodeDomWidth / 4,
        ];
        menuVisible.value = true;
      }
      if (minderStore.event.name === 'enterNode') {
        switchNode(minderStore.event.nodeData);
      }
    }
  );

  function handleMinderMenuSelect(val: string | number | Record<string, any> | undefined) {
    const selectedNode = window.minder.getSelectedNode();
    switch (val) {
      case 'expand':
        if (selectedNode.data.expandState === 'collapse') {
          window.minder.execCommand('Expand');
        } else {
          window.minder.execCommand('Collapse');
        }
        break;
      case 'insetParent':
        window.minder.execCommand('AppendParentNode');
        break;
      case 'insetSon':
        window.minder.execCommand('AppendChildNode');
        break;
      case 'insetBrother':
        window.minder.execCommand('AppendSiblingNode');
        break;
      case 'copy':
        window.minder.execCommand('Copy');
        break;
      case 'cut':
        window.minder.execCommand('Cut');
        break;
      case 'paste':
        window.minder.execCommand('Paste');
        break;
      case 'delete':
        window.minder.execCommand('RemoveNode');
        break;
      case 'enterNode':
        switchNode(selectedNode.data);
        break;
      default:
        break;
    }
  }
</script>

<style lang="less">
  @import '../style/editor.less';
  .save-btn {
    @apply !absolute;
  }
  .minder-container {
    @apply relative;
  }
  .minder-dropdown {
    .arco-dropdown-list-wrapper {
      max-height: none;
    }
  }
</style>
