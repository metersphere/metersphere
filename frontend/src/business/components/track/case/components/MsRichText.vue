<template>
    <ckeditor :disabled="disabled"
              :editor="editor"
              v-model="textData"
              :config="editorConfig"
              :placeholder="$t('test_track.case.input_prerequisite')"
              >
    </ckeditor>
</template>

<script>
  import ClassicEditor from '@ckeditor/ckeditor5-build-classic';

  export default {
    name: "MsRichText",
    computed: {
      textData: {
        get: function () {
          if (this.content) {
            return this.content;
          }
          return '';
        },
        set: function (val) {
          this.$emit('updateRichText', val);
        }
      }
    },
    data() {
      return {
        editor: ClassicEditor,
        editorConfig: {
          toolbar: [ 'heading', '|', 'bold', 'italic', 'link', 'bulletedList', 'numberedList', 'blockQuote' ,'insertTable', '|','undo', 'redo'],
          // ckfinder: {
          //   uploadUrl: `/image/uploadCkEditor?imgPath=${JSON.stringify(this.imagePath)}`
          // },
          // filebrowserImageUploadUrl: '/image/uploadCkEditor'simpleUpload
        },
        // shiftEnterMode: window.CKEditor.ENTER_P
        // startupMode: 'source'
      };
    },
    props: {
      imagePath: {
        type: String,
        default: ''
      },
      content: {
        type: String,
        default: null
      },
      disabled:
        {
          type: Boolean,
          default: false
        }
    },
    methods: {
      // onReady(editor) {
      //   // 自定义上传图片插件
      //   editor.plugins.get("FileRepository").createUploadAdapter = loader => {
      //     return new MyUploadAdapter(loader);
      //   };
      // }
    }
  };
</script>

<style scoped>

</style>
