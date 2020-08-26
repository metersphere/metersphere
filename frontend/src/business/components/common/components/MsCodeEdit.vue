<template>
    <editor v-model="formatData" :lang="mode" @init="editorInit" :theme="theme"/>
</template>

<script>
    export default {
      name: "MsCodeEdit",
      components: { editor: require('vue2-ace-editor')},
      data() {
        return {
          formatData: ''
        }
      },
      props: {
        data: {
          type: String
        },
        theme: {
          type: String,
          default() {
            return 'chrome'
          }
        },
        init: {
          type: Function
        },
        readOnly: {
          type: Boolean,
          default() {
            return false;
          }
        },
        mode: {
          type: String,
          default() {
            return 'text';
          }
        },
        modes: {
          type: Array,
          default() {
            return ['text', 'json', 'xml', 'html'];
          }
        }
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
        }
      },
      methods: {
        editorInit: function (editor) {
          require('brace/ext/language_tools') //language extension prerequsite...
          this.modes.forEach(mode => {
            require('brace/mode/' + mode); //language
          });
          require('brace/theme/' + this.theme)
          require('brace/snippets/javascript') //snippet
          if (this.readOnly) {
            editor.setReadOnly(true);
          }
          if (this.init) {
            this.init(editor);
          }
        },
        format() {
          if (this.mode === 'json') {
            try {
              var JSONbig = require('json-bigint')({"storeAsString": false});
              this.formatData = JSON.stringify(JSONbig.parse(this.data), null, '\t');
            } catch (e) {
              if (this.data) {
                this.formatData = this.data;
              }
            }
          } else {
            if (this.data) {
              this.formatData = this.data;
            }
          }
        }
      }
    }
</script>

<style scoped>

</style>
