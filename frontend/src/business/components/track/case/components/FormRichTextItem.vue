<template>
  <el-form-item :disable="true" :label="title" :prop="prop" :label-width="labelWidth">
    <ms-mark-down-text :prop="prop" :data="data" :disabled="disabled"/>
  </el-form-item>
</template>

<script>
import MsMarkDownText from "@/business/components/track/case/components/MsMarkDownText";
export default {
  name: "FormRichTextItem",
  components: {MsMarkDownText},
  props: ['data', 'title', 'prop', 'disabled', 'labelWidth']
}
</script>

<style scoped>
/deep/ .v-note-wrapper {
  position: initial;
}
</style>
