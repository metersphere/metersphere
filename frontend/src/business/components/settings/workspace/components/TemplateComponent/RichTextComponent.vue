<template>

  <common-component>

    <template v-slot:header>
      <el-input v-model="preview.title"></el-input>
    </template>

    <template>
      <ckeditor :editor="editor" v-model="preview.content" :config="editorConfig"></ckeditor>
    </template>

  </common-component>

</template>

<script>
    import CommonComponent from "./CommonComponent";
    import ClassicEditor from '@ckeditor/ckeditor5-build-classic';

    export default {
      name: "RichTextComponent",
      components: {CommonComponent},
      data() {
        return {
          editor: ClassicEditor,
          // editorData: '<p>Content of the editor.</p>',
          editorConfig: {
            // The configuration of the editor.
          }
        }
      },
      props: {
        preview: Object,
        default() {
          return {
            title: '',
            content: '<p>Content of the editor.</p>'
          }
        }
      }
    }
</script>

<style scoped>

</style>
