<template>
  <v-ace-editor
    :value="currentValue"
    :lang="props.lang"
    :theme="props.theme"
    :style="{ height: props.height, width: props.width }"
    @init="editorInit"
  />
</template>

<script lang="ts" setup>
  import { computed, PropType, ref, onUnmounted, watch } from 'vue';
  import { VAceEditor } from 'vue3-ace-editor';
  import ace from 'ace-builds';
  import workerJavascriptUrl from 'ace-builds/src-min-noconflict/worker-javascript?url';
  import workerJsonUrl from 'ace-builds/src-min-noconflict/worker-json?url';
  import workerHtmlUrl from 'ace-builds/src-min-noconflict/worker-html?url';
  import workerXmlUrl from 'ace-builds/src-min-noconflict/worker-xml?url';
  import 'ace-builds/src-min-noconflict/ext-language_tools';
  import 'ace-builds/src-min-noconflict/ext-beautify';
  import 'ace-builds/src-min-noconflict/theme-github_dark';
  import 'ace-builds/src-min-noconflict/theme-github';
  import 'ace-builds/src-min-noconflict/theme-chrome';
  import 'ace-builds/src-min-noconflict/mode-javascript';
  import 'ace-builds/src-min-noconflict/mode-html';
  import 'ace-builds/src-min-noconflict/mode-xml';
  import 'ace-builds/src-min-noconflict/mode-json';
  import 'ace-builds/src-min-noconflict/mode-java';

  export type LangType = 'javascript' | 'html' | 'xml' | 'json' | 'java' | 'text';
  export type ThemeType = 'github_dark' | 'github' | 'chrome';

  const props = defineProps({
    content: {
      type: String,
      required: true,
      default: '',
    },
    init: {
      type: Function,
    },
    lang: {
      type: String as PropType<LangType>,
      default: 'javascript',
    },
    theme: {
      type: String as PropType<ThemeType>,
      default: 'github_dark',
    },
    height: {
      type: String,
      default: '500px',
    },
    width: {
      type: String,
      default: '100%',
    },
  });

  const emit = defineEmits(['update:content']);
  const editorInstance = ref<any>(null);

  const currentValue = computed({
    get() {
      return props.content;
    },
    set(val) {
      emit('update:content', val);
    },
  });

  const onLangChange = () => {
    let useWorker = true;
    if (props.lang === 'javascript') {
      ace.config.setModuleUrl('ace/mode/javascript_worker', workerJavascriptUrl);
    } else if (props.lang === 'json') {
      ace.config.setModuleUrl('ace/mode/json_worker', workerJsonUrl);
    } else if (props.lang === 'xml') {
      ace.config.setModuleUrl('ace/mode/xml_worker', workerXmlUrl);
    } else if (props.lang === 'html') {
      ace.config.setModuleUrl('ace/mode/html_worker', workerHtmlUrl);
    } else {
      useWorker = false;
    }
    return useWorker;
  };

  const editorInit = (editor: any) => {
    if (props.init) props.init(editor);
    editorInstance.value = editor;
    const useWorker = onLangChange();
    editor.setOptions({
      enableBasicAutocompletion: true,
      enableSnippets: false,
      enableLiveAutocompletion: true,
      wrap: true,
      useWorker,
    });
  };

  watch(
    () => props.lang,
    () => {
      onLangChange();
    }
  );

  onUnmounted(() => {
    editorInstance.value.destroy();
  });
</script>
