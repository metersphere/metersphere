<template>
  <el-form-item :label="title">
    <test-case-rich-text :content="data[prop]" @updateRichText="updateData"/>
  </el-form-item>
</template>

<script>
import TestCaseRichText from "@/business/components/track/case/components/TestCaseStepRichText";
export default {
  name: "FormRIchTextItem",
  components: {TestCaseRichText},
  props: ['data', 'title', 'prop'],
  methods: {
    updateData(value) {
      this.data[this.prop] = value;
    }
  }
}
</script>

<style scoped>

</style>
