import { PropType } from 'vue';

export type CustomTheme = 'MS-text';
export type Theme = 'vs' | 'hc-black' | 'vs-dark' | CustomTheme;
export type FoldingStrategy = 'auto' | 'indentation';
export type RenderLineHighlight = 'all' | 'line' | 'none' | 'gutter';
export type WordWrap = 'off' | 'on' | 'wordWrapColumn' | 'bounded';
export const LanguageEnum = {
  PLAINTEXT: 'PLAINTEXT' as const,
  JAVASCRIPT: 'JAVASCRIPT' as const,
  TYPESCRIPT: 'TYPESCRIPT' as const,
  CSS: 'CSS' as const,
  LESS: 'LESS' as const,
  SASS: 'SASS' as const,
  HTML: 'HTML' as const,
  SQL: 'SQL' as const,
  JSON: 'JSON' as const,
  JAVA: 'JAVA' as const,
  PYTHON: 'PYTHON' as const,
  XML: 'XML' as const,
  YAML: 'YAML' as const,
  SHELL: 'SHELL' as const,
  BEANSHELL: 'BEANSHELL' as const,
  BEANSHELL_JSR233: 'BEANSHELL_JSR233' as const,
  GROOVY: 'GROOVY' as const,
  NASHORNSCRIPT: 'NASHORNSCRIPT' as const,
  RHINOSCRIPT: 'RHINOSCRIPT' as const,
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
  wordWrap: WordWrap; // 自动缩进
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
  wordWrap: {
    type: String as PropType<WordWrap>,
    default: 'on',
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
        wordWrap: 'on',
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
  // 是否代码语言切换
  showLanguageChange: {
    type: Boolean as PropType<boolean>,
    default: false,
  },
  // 是否显示字符集切换
  showCharsetChange: {
    type: Boolean as PropType<boolean>,
    default: false,
  },
  // 是否显示主题切换
  showThemeChange: {
    type: Boolean as PropType<boolean>,
    default: false,
  },
  // 是否显示代码格式化
  showCodeFormat: {
    type: Boolean as PropType<boolean>,
    default: false,
  },
  widthClass: {
    type: String as PropType<string>,
    default: '',
  },
  // 是否自适应 开启后按照代码高度计算代码器高度最大1000px 未开启则按照外侧传入容器高度
  isAdaptive: {
    type: Boolean as PropType<boolean>,
    default: false,
  },
  // diff对比模式
  diffMode: {
    type: Boolean as PropType<boolean>,
    default: false,
  },
  // 原来值
  originalValue: {
    type: String as PropType<string>,
    default: null,
  },
};
