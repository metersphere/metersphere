<template>
  <div ref="mec" class="minder-container" :style="{ height: `${props.height}px` }">
    <a-button type="primary" :disabled="props.disabled" class="save-btn bottom-[30px] right-[30px]" @click="save">{{
      t('minder.main.main.save')
    }}</a-button>
    <navigator />
  </div>
</template>

<script lang="ts" name="minderContainer" setup>
  import { onMounted, ref } from 'vue';
  import type { Ref } from 'vue';
  import Navigator from './navigator.vue';
  import { markChangeNode, markDeleteNode } from '../script/tool/utils';
  import { useI18n } from '@/hooks/useI18n';
  import { editMenuProps, mainEditorProps, priorityProps, tagProps } from '../props';
  import Editor from '../script/editor';

  const { t } = useI18n();
  const props = defineProps({ ...editMenuProps, ...mainEditorProps, ...tagProps, ...priorityProps });

  const emit = defineEmits({
    afterMount: () => ({}),
    save: (json) => json,
  });

  const mec: Ref<HTMLDivElement | null> = ref(null);

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
</script>

<style lang="less">
  @import '../style/editor.less';
  .save-btn {
    @apply !absolute;
  }
  .minder-container {
    @apply relative;
  }
</style>
