<template>
  <el-form-item :disable="true" :label="title" :prop="prop" :label-width="labelWidth">
    <ms-mark-down-text :prop="prop" :data="data" :disabled="disabled" :default-open="defaultOpen"/>
  </el-form-item>
</template>

<script>
import MsMarkDownText from "@/business/components/track/case/components/MsMarkDownText";
export default {
  name: "FormRichTextItem",
  components: {MsMarkDownText},
  props: {
    data: Object,
    prop: String,
    disabled: Boolean,
    title: String,
    defaultOpen: {
      type: String,
      default() {
        return 'preview';
      }
    },
    labelWidth: [String, Number]
  }
}
</script>

<style scoped>
</style>
