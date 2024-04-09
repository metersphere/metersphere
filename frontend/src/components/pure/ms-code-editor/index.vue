<template>
  <div
    ref="fullRef"
    class="flex h-full flex-col rounded-[var(--border-radius-small)] bg-[var(--color-fill-1)] p-[12px]"
  >
    <div v-if="showTitleLine" class="mb-[8px] flex items-center justify-between">
      <div class="flex flex-wrap gap-[4px]">
        <a-select
          v-if="showLanguageChange"
          v-model:model-value="currentLanguage"
          :options="languageOptions"
          class="w-[100px]"
          size="mini"
          @change="(val) => handleLanguageChange(val as Language)"
        />
        <a-select
          v-if="showCharsetChange"
          v-model:model-value="currentCharset"
          :options="charsetOptions"
          class="w-[100px]"
          size="mini"
          @change="(val) => handleCharsetChange(val as string)"
        />
        <a-select
          v-if="showThemeChange"
          v-model:model-value="currentTheme"
          :options="themeOptions"
          class="w-[100px]"
          size="mini"
          @change="(val) => handleThemeChange(val as Theme)"
        />
        <slot name="leftTitle">
          <span class="flex items-center gap-[4px] font-medium">{{ title }}</span>
        </slot>
      </div>
      <div class="ml-auto flex items-center gap-[8px]">
        <slot name="rightTitle"> </slot>
        <a-button
          v-if="showCodeFormat"
          type="outline"
          class="arco-btn-outline--secondary p-[0_8px]"
          size="mini"
          @click="format"
        >
          <div class="flex items-center gap-[4px]">
            <icon-code-square class="text-[var(--color-text-4)]" />
            <div class="text-[var(--color-text-1)]">{{ t('msCodeEditor.format') }}</div>
          </div>
        </a-button>
        <div
          v-if="showFullScreen"
          class="cursor-pointer text-right !text-[var(--color-text-4)]"
          @click="toggleFullScreen"
        >
          <MsIcon v-if="isFullScreen" type="icon-icon_minify_outlined" />
          <MsIcon v-else type="icon-icon_magnify_outlined" />
          {{ t('msCodeEditor.fullScreen') }}
        </div>
      </div>
    </div>
    <!-- 这里的 32px 是顶部标题的 32px -->
    <div class="flex w-full flex-1 flex-row rounded-[var(--border-radius-small)]">
      <div
        ref="codeContainerRef"
        :class="['ms-code-editor', isFullScreen ? 'ms-code-editor-full-screen' : '', currentTheme]"
      ></div>
      <slot name="rightBox"> </slot>
    </div>
  </div>
</template>

<script lang="ts">
  import { computed, defineComponent, onBeforeUnmount, onMounted, ref, watch } from 'vue';

  import { codeCharset } from '@/config/apiTest';
  import useFullScreen from '@/hooks/useFullScreen';
  import { useI18n } from '@/hooks/useI18n';
  import { decodeStringToCharset } from '@/utils';

  import './userWorker';
  import MsCodeEditorTheme from './themes';
  import { CustomTheme, editorProps, Language, LanguageEnum, Theme } from './types';
  import * as monaco from 'monaco-editor/esm/vs/editor/editor.api';
  import XmlBeautify from 'xml-beautify';

  export default defineComponent({
    name: 'MonacoEditor',
    props: editorProps,
    emits: ['update:modelValue', 'change'],
    setup(props, { emit, slots }) {
      const { t } = useI18n();
      // 编辑器实例，每次调用组件都会创建独立的实例
      let editor: monaco.editor.IStandaloneCodeEditor;
      const codeContainerRef = ref();

      // 用于全屏的容器 ref
      const fullRef = ref<HTMLElement | null>();
      // 当前主题
      const currentTheme = ref<Theme>(props.theme);
      // 主题选项
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
      function handleThemeChange(val: Theme) {
        editor.updateOptions({
          theme: val,
        });
      }

      // 当前语言
      const currentLanguage = ref<Language>(props.language);
      // 语言选项
      const languageOptions = Object.values(LanguageEnum)
        .map((e) => {
          if (props.languages) {
            // 如果传入了语言种类数组，则过滤选项
            if (props.languages.includes(e)) {
              return {
                label: e.toLowerCase(),
                value: e,
              };
            }
            return false;
          }
          return {
            label: e.toLowerCase(),
            value: e,
          };
        })
        .filter(Boolean) as { label: string; value: Language }[];
      function handleLanguageChange(val: Language) {
        monaco.editor.setModelLanguage(editor.getModel()!, val.toLowerCase());
      }

      // 当前字符集
      const currentCharset = ref('UTF-8');
      // 字符集选项
      const charsetOptions = codeCharset.map((e) => ({
        label: e,
        value: e,
      }));
      function handleCharsetChange(val: string) {
        editor.setValue(decodeStringToCharset(props.modelValue, val));
      }

      // 是否显示标题栏
      const showTitleLine = computed(
        () =>
          props.title ||
          props.showThemeChange ||
          props.showLanguageChange ||
          props.showCharsetChange ||
          props.showFullScreen ||
          props.showCodeFormat ||
          slots.leftTitle ||
          slots.rightTitle
      );

      watch(
        () => props.theme,
        (val) => {
          currentTheme.value = val;
        }
      );

      const setEditBoxBg = () => {
        const codeBgEl = document.querySelector('.monaco-editor-background');
        if (codeBgEl) {
          // 获取计算后的样式对象
          const computedStyle = window.getComputedStyle(codeBgEl);

          // 获取背景颜色
          const { backgroundColor } = computedStyle;
          codeContainerRef.value.style.backgroundColor = backgroundColor;
        }
      };

      const { isFullScreen, toggleFullScreen } = useFullScreen(fullRef);

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

      // 撤销
      function undo() {
        if (editor) {
          editor.trigger('source', 'undo', {});
        }
      }
      // 重做
      function redo() {
        if (editor) {
          editor.trigger('source', 'redo', {});
        }
      }

      function format() {
        if (editor && editor.getValue() !== '' && props.language !== LanguageEnum.PLAINTEXT) {
          if (props.readOnly) {
            // 只读模式下的格式化
            editor.updateOptions({ readOnly: false });
            // 执行格式化代码的动作
            editor
              .getAction('editor.action.formatDocument')
              ?.run()
              .then(() => {
                const value = editor.getValue(); // 给父组件实时返回最新文本
                editor.updateOptions({ readOnly: true });
                emit('update:modelValue', value);
                emit('change', value);
              });
          } else if (currentLanguage.value === LanguageEnum.XML) {
            // XML需要手动格式化
            const value = editor.getValue();
            const formattedCode = new XmlBeautify().beautify(value);
            editor.setValue(formattedCode);
            emit('update:modelValue', formattedCode);
            emit('change', formattedCode);
          } else {
            editor
              .getAction('editor.action.formatDocument')
              ?.run()
              .then(() => {
                const value = editor.getValue(); // 给父组件实时返回最新文本
                emit('update:modelValue', value);
                emit('change', value);
              });
          }
        }
      }

      const init = () => {
        // 注册自定义主题 TODO:自定义主题高亮色还没配置
        Object.keys(MsCodeEditorTheme).forEach((e) => {
          monaco.editor.defineTheme(e, MsCodeEditorTheme[e as CustomTheme]);
        });
        editor = monaco.editor.create(codeContainerRef.value, {
          value: props.modelValue,
          automaticLayout: true,
          padding: {
            top: 12,
            bottom: 12,
          },
          minimap: {
            enabled: false, // 将代码块预览隐藏
          },
          contextmenu: !props.readOnly, // 只读模式下禁用右键菜单
          ...props,
          language: props.language.toLowerCase(),
          theme: currentTheme.value,
        });

        // 监听值的变化
        editor.onDidChangeModelContent(() => {
          const value = editor.getValue(); // 给父组件实时返回最新文本
          emit('update:modelValue', value);
          emit('change', value);
        });
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
        },
        { immediate: true }
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
          currentLanguage.value = newValue;
          monaco.editor.setModelLanguage(editor.getModel()!, newValue.toLowerCase()); // 设置语言，语言 ENUM 是大写的，但是 monaco 需要小写
        }
      );

      onBeforeUnmount(() => {
        editor.dispose();
      });

      onMounted(() => {
        init();
        setEditBoxBg();
        if (props.readOnly) {
          format();
        }
      });

      onUpdated(() => {
        setTimeout(() => {
          if (props.readOnly) {
            format();
          }
        }, 100); // TODO:暂未找到准确的格式化时机
      });

      return {
        codeContainerRef,
        fullRef,
        isFullScreen,
        currentTheme,
        themeOptions,
        currentLanguage,
        languageOptions,
        currentCharset,
        charsetOptions,
        showTitleLine,
        toggleFullScreen,
        t,
        handleThemeChange,
        handleLanguageChange,
        handleCharsetChange,
        insertContent,
        undo,
        redo,
        format,
      };
    },
  });
</script>

<style lang="less" scoped>
  .ms-code-editor {
    @apply z-10;

    width: v-bind(width);
    height: v-bind(height);
    &.MS-text[data-mode-id='plaintext'] {
      :deep(.mtk1) {
        color: rgb(var(--primary-5));
      }
    }
    :deep(.overflowingContentWidgets) {
      z-index: 9999;
    }
  }
  .ms-code-editor-full-screen {
    height: calc(100vh - 66px);
  }
</style>
