import { PropType } from 'vue';

export type CustomTheme = 'MS-text';
export type Theme = 'vs' | 'hc-black' | 'vs-dark' | CustomTheme;
export type FoldingStrategy = 'auto' | 'indentation';
export type RenderLineHighlight = 'all' | 'line' | 'none' | 'gutter';
export const LanguageEnum = {
  PLAINTEXT: 'plaintext' as const,
  JAVASCRIPT: 'javascript' as const,
  TYPESCRIPT: 'typescript' as const,
  CSS: 'css' as const,
  LESS: 'less' as const,
  SASS: 'sass' as const,
  HTML: 'html' as const,
  SQL: 'sql' as const,
  JSON: 'json' as const,
  JAVA: 'java' as const,
  PYTHON: 'python' as const,
  XML: 'xml' as const,
  YAML: 'yaml' as const,
  SHELL: 'shell' as const,
} as const;
export type Language = (typeof LanguageEnum)[keyof typeof LanguageEnum];
export interface Options {
  automaticLayout: boolean; // 自适应布局
  foldingStrategy: FoldingStrategy; // 折叠方式  auto | indentation
  renderLineHighlight: RenderLineHighlight; // 行亮
  selectOnLineNumbers: boolean; // 显示行号
  minimap: {
    // 关闭小地图
    enabled: boolean;
  };
  readOnly: boolean; // 只读
  fontSize: number; // 字体大小
  scrollBeyondLastLine: boolean; // 取消代码后面一大段空白
  overviewRulerBorder: boolean; // 不要滚动条的边框
}

export const editorProps = {
  modelValue: {
    type: String as PropType<string>,
    default: null,
  },
  width: {
    type: [String, Number] as PropType<string | number>,
    default: '100%',
  },
  height: {
    type: [String, Number] as PropType<string | number>,
    default: '50vh',
  },
  language: {
    type: String as PropType<Language>,
    default: 'plaintext',
  },
  theme: {
    type: String as PropType<Theme>,
    validator(value: string): boolean {
      return ['vs', 'hc-black', 'vs-dark', 'MS-text'].includes(value);
    },
    default: 'vs-dark',
  },
  readOnly: {
    type: Boolean as PropType<boolean>,
    default: false,
  },
  options: {
    type: Object as PropType<Options>,
    default() {
      return {
        automaticLayout: true,
        foldingStrategy: 'indentation',
        renderLineHighlight: 'all',
        selectOnLineNumbers: true,
        minimap: {
          enabled: true,
        },
        readOnly: false,
        fontSize: 16,
        scrollBeyondLastLine: false,
        overviewRulerBorder: false,
      };
    },
  },
  title: {
    type: String as PropType<string>,
  },
  showFullScreen: {
    type: Boolean as PropType<boolean>,
    default: true,
  },
  languages: {
    // 支持选择的语言种类
    type: Array as PropType<Array<Language>>,
    default: undefined,
  },
  showLanguageChange: {
    type: Boolean as PropType<boolean>,
    default: false,
  },
  showThemeChange: {
    type: Boolean as PropType<boolean>,
    default: true,
  },
};
