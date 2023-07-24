import * as monaco from 'monaco-editor';
// import EditorWorker from 'monaco-editor/esm/vs/editor/editor.worker?worker';
// import JsonWorker from 'monaco-editor/esm/vs/language/json/json.worker?worker';
// import CssWorker from 'monaco-editor/esm/vs/language/css/css.worker?worker';
// import HtmlWorker from 'monaco-editor/esm/vs/language/html/html.worker?worker';
// import TsWorker from 'monaco-editor/esm/vs/language/typescript/ts.worker?worker';

// @ts-ignore
// eslint-disable-next-line no-restricted-globals
self.MonacoEnvironment = {
  async getWorker(_: any, label: string) {
    if (label === 'json') {
      const JsonWorker = ((await import('monaco-editor/esm/vs/language/json/json.worker?worker')) as any).default;
      return new JsonWorker();
    }
    if (label === 'css' || label === 'scss' || label === 'less') {
      const CssWorker = ((await import('monaco-editor/esm/vs/language/css/css.worker?worker')) as any).default;
      return new CssWorker();
    }
    if (label === 'html' || label === 'handlebars' || label === 'razor') {
      const HtmlWorker = ((await import('monaco-editor/esm/vs/language/html/html.worker?worker')) as any).default;
      return new HtmlWorker();
    }
    if (label === 'typescript' || label === 'javascript') {
      const TsWorker = ((await import('monaco-editor/esm/vs/language/typescript/ts.worker?worker')) as any).default;

      return new TsWorker();
    }
    const EditorWorker = ((await import('monaco-editor/esm/vs/editor/editor.worker?worker')) as any).default;
    return new EditorWorker();
  },
};

monaco.languages.typescript.typescriptDefaults.setEagerModelSync(true);
