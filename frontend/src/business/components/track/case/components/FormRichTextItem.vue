<template>
  <el-form-item :disable="true" :label="title" :prop="prop" :label-width="labelWidth">
    <test-case-rich-text :disabled="disabled" :content="data[prop]" @updateRichText="updateData"/>
  </el-form-item>
</template>

<script>
import TestCaseRichText from "@/business/components/track/case/components/MsRichText";
export default {
  name: "FormRichTextItem",
  components: {TestCaseRichText},
  props: ['data', 'title', 'prop', 'disabled', 'labelWidth'],
  methods: {
    updateData(value) {
      this.data[this.prop] = value;
    }
  }
}
</script>

<style scoped>

</style>
