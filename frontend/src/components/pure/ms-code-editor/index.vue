<template>
  <div ref="fullRef" class="rounded-[4px] bg-[var(--color-fill-1)] p-[12px]">
    <div class="mb-[12px] flex justify-between pr-[12px]">
      <slot name="title">
        <span class="font-medium">{{ title }}</span>
      </slot>
      <div v-if="showThemeChange">
        <a-select
          v-model:model-value="currentTheme"
          :options="themeOptions"
          class="w-[100px]"
          size="small"
          @change="(val) => handleThemeChange(val as Theme)"
        ></a-select>
      </div>
      <div v-if="showFullScreen" class="w-[96px] cursor-pointer text-right !text-[var(--color-text-4)]" @click="toggle">
        <MsIcon v-if="isFullscreen" type="icon-icon_minify_outlined" />
        <MsIcon v-else type="icon-icon_magnify_outlined" />
        {{ t('msCodeEditor.fullScreen') }}
      </div>
    </div>
    <div class="flex w-full flex-row">
      <div ref="codeEditBox" :class="['ms-code-editor', isFullscreen ? 'ms-code-editor-full-screen' : '']"></div>
      <slot name="rightBox"> </slot>
    </div>
  </div>
</template>

<script lang="ts">
  import { defineComponent, onBeforeUnmount, onMounted, ref, watch } from 'vue';
  import { useFullscreen } from '@vueuse/core';

  import { useI18n } from '@/hooks/useI18n';

  import './userWorker';
  import MsCodeEditorTheme from './themes';
  import { CustomTheme, editorProps, Theme } from './types';
  import * as monaco from 'monaco-editor/esm/vs/editor/editor.api';
  import prettier from 'prettier';
  import parserBabel from 'prettier/parser-babel';

  export default defineComponent({
    name: 'MonacoEditor',
    props: editorProps,
    emits: ['update:modelValue', 'change', 'editorMounted'],
    setup(props, { emit }) {
      const { t } = useI18n();
      let editor: monaco.editor.IStandaloneCodeEditor;

      const codeEditBox = ref();
      const fullRef = ref<HTMLElement | null>();
      const currentTheme = ref<Theme>(props.theme);
      const themeOptions = [
        { label: 'vs', value: 'vs' },
        { label: 'vs-dark', value: 'vs-dark' },
        { label: 'hc-black', value: 'hc-black' },
      ].concat(
        Object.keys(MsCodeEditorTheme).map((item) => ({
          label: item,
          value: item,
        }))
      );

      watch(
        () => props.theme,
        (val) => {
          currentTheme.value = val;
        }
      );

      function handleThemeChange(val: Theme) {
        monaco.editor.setTheme(val);
      }

      const init = () => {
        // 注册自定义主题
        Object.keys(MsCodeEditorTheme).forEach((e) => {
          monaco.editor.defineTheme(e, MsCodeEditorTheme[e as CustomTheme]);
        });
        editor = monaco.editor.create(codeEditBox.value, {
          value: props.modelValue,
          automaticLayout: true,
          ...props,
        });

        // 监听值的变化
        editor.onDidBlurEditorText(() => {
          const value = editor.getValue(); // 给父组件实时返回最新文本
          emit('update:modelValue', value);
          emit('change', value);
        });
      };

      const setEditBoxBg = () => {
        const codeBgEl = document.querySelector('.monaco-editor-background');
        if (codeBgEl) {
          // 获取计算后的样式对象
          const computedStyle = window.getComputedStyle(codeBgEl);

          // 获取背景颜色
          const { backgroundColor } = computedStyle;
          codeEditBox.value.style.backgroundColor = backgroundColor;
        }
      };

      const { isFullscreen, toggle } = useFullscreen(fullRef);

      // 插入内容
      const insertContent = (text: string) => {
        if (editor) {
          const position = editor.getPosition();
          if (position) {
            editor.executeEdits('', [
              {
                range: new monaco.Range(position?.lineNumber, position?.column, position?.lineNumber, position?.column),
                text,
              },
            ]);
            editor.setPosition({
              lineNumber: position?.lineNumber,
              column: position.column + text.length,
            });
          }
          editor.focus();
        }
      };

      watch(
        () => props.modelValue,
        (newValue) => {
          if (editor) {
            const value = editor.getValue();
            if (newValue !== value) {
              editor.setValue(newValue);
            }
          }
        }
      );

      watch(
        () => props.options,
        (newValue) => {
          editor.updateOptions(newValue);
        },
        { deep: true }
      );

      watch(
        () => props.language,
        (newValue) => {
          monaco.editor.setModelLanguage(editor.getModel()!, newValue);
        }
      );

      onBeforeUnmount(() => {
        editor.dispose();
      });

      onMounted(() => {
        init();
        setEditBoxBg();
      });

      return {
        codeEditBox,
        fullRef,
        isFullscreen,
        currentTheme,
        themeOptions,
        toggle,
        t,
        handleThemeChange,
        insertContent,
      };
    },
  });
</script>

<style lang="less" scoped>
  .ms-code-editor {
    @apply z-10;

    padding: 16px 0;
    width: v-bind(width);
    height: v-bind(height);
    &[data-mode-id='plaintext'] {
      :deep(.mtk1) {
        color: rgb(var(--primary-5));
      }
    }
  }
  .ms-code-editor-full-screen {
    height: calc(100vh - 66px);
  }
</style>
