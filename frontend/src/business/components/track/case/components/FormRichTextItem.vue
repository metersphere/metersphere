<template>
  <el-form-item :disable="true" :label="title" :prop="prop" :label-width="labelWidth">
    <mavon-editor v-if="active" :editable="!disabled" @imgAdd="imgAdd" :default-open="disabled ? 'preview' : null" class="mavon-editor"
                  :subfield="disabled ? false : true" :toolbars="toolbars" :toolbarsFlag="disabled ? false : true" @imgDel="imgDel" v-model="data[prop]"  ref="md"/>
  </el-form-item>
</template>

<script>
import {getUUID} from "@/common/js/utils";
export default {
  name: "FormRichTextItem",
  components: {},
  props: ['data', 'title', 'prop', 'disabled', 'labelWidth'],
  data() {
    return {
      toolbars: {
        bold: true, // 粗体
        italic: true, // 斜体
        header: true, // 标题
        underline: true, // 下划线
        strikethrough: true, // 中划线
        mark: true, // 标记
        superscript: true, // 上角标
        subscript: true, // 下角标
        quote: true, // 引用
        ol: true, // 有序列表
        ul: true, // 无序列表
        link: true, // 链接
        imagelink: true, // 图片链接
        code: true, // code
        table: true, // 表格
        fullscreen: true, // 全屏编辑
        readmodel: true, // 沉浸式阅读
        htmlcode: true, // 展示html源码
        help: true, // 帮助
        /* 1.3.5 */
        undo: true, // 上一步
        redo: true, // 下一步
        trash: true, // 清空
        save: false, // 保存（触发events中的save事件）
        /* 1.4.2 */
        navigation: true, // 导航目录
        /* 2.1.8 */
        alignleft: true, // 左对齐
        aligncenter: true, // 居中
        alignright: true, // 右对齐
        /* 2.2.1 */
        subfield: true, // 单双栏模式
        preview: true, // 预览
      }
    }
  },
  computed: {
    active() {
      if (this.data[this.prop] !== undefined) {
        return true;
      }
      return false;
    }
  },
  methods: {
    imgAdd(pos, file){
      let param = {
        id: getUUID().substring(0, 8)
      };
      file.prefix = param.id;
      this.result = this.$fileUpload('/resource/md/upload', file, null, param, () => {
        this.$success(this.$t('commons.save_success'));
        this.$refs.md.$img2Url(pos, '/resource/md/get/'  + param.id + '_' + file.name);
      });
      this.$emit('imgAdd', file);
    },
    imgDel(file) {
      if (file) {
        this.$get('/resource/md/delete/' + file[1].prefix + "_" + file[1].name);
      }
    },
  }
}
</script>

<style scoped>

.mavon-editor {
  min-height: 20px;
}

</style>
