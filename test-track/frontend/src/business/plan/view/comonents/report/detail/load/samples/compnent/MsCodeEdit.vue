<template>
  <editor
    v-model="formatData"
    :lang="mode"
    @init="editorInit"
    :theme="theme"
    :height="height"
    :key="readOnly"
    ref="msEditor" />
</template>

<script>
import { formatJson, formatXml } from 'metersphere-frontend/src/utils/format-utils';
import toDiffableHtml from 'diffable-html';
import editor from 'vue2-ace-editor';
import 'brace/ext/language_tools'; //language extension prerequisite...
import 'brace/mode/text';
import 'brace/mode/json';
import 'brace/mode/xml';
import 'brace/mode/html';
import 'brace/mode/java';
import 'brace/mode/python';
import 'brace/mode/sql';
import 'brace/mode/javascript';
import 'brace/mode/yaml';
import 'brace/theme/chrome';
import 'brace/theme/eclipse';
import 'brace/snippets/javascript'; //snippet

export default {
  name: 'MsCodeEdit',
  components: { editor },
  data() {
    return {
      formatData: '',
    };
  },
  props: {
    height: [String, Number],
    data: {
      type: String,
      default() {
        return '';
      },
    },
    theme: {
      type: String,
      default() {
        return 'chrome';
      },
    },
    init: {
      type: Function,
    },
    readOnly: {
      type: Boolean,
      default() {
        return false;
      },
    },
    mode: {
      type: String,
      default() {
        return 'text';
      },
    },
    modes: {
      type: Array,
      default() {
        return ['text', 'json', 'xml', 'html'];
      },
    },
  },
  mounted() {
    this.format();
  },
  watch: {
    formatData() {
      this.$emit('update:data', this.formatData);
    },
    mode() {
      this.format();
    },
    data() {
      this.formatData = this.data;
    },
  },
  methods: {
    insert(code) {
      if (this.$refs.msEditor.editor) {
        this.$refs.msEditor.editor.insert(code);
      }
    },
    editorInit: function (editor) {
      // require('brace/ext/language_tools'); //language extension prerequisite...
      // require('brace/mode/text');
      // require('brace/mode/json');
      // require('brace/mode/xml');
      // require('brace/mode/html');
      // require('brace/mode/java');
      // require('brace/mode/python');
      // require('brace/mode/sql');
      // require('brace/mode/javascript');
      // require('brace/mode/yaml');
      // // this.modes.forEach((mode) => {
      // //   require('brace/mode/' + mode); //language
      // // });
      // require('brace/theme/' + this.theme);
      // require('brace/snippets/javascript'); //snippet
      if (this.readOnly) {
        editor.setReadOnly(true);
      }
      if (this.init) {
        this.init(editor);
      }
    },
    format() {
      switch (this.mode) {
        case 'json':
          this.formatData = formatJson(this.data);
          break;
        case 'html':
          this.formatData = toDiffableHtml(this.data);
          break;
        case 'xml':
          this.formatData = formatXml(this.data);
          break;
        default:
          if (this.data) {
            this.formatData = this.data;
          }
      }
    },
  },
};
</script>

<style scoped>

</style>
