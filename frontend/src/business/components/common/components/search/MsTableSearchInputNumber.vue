<template>
  <ms-table-search-component v-model="component.operator.value" :component="component" v-bind="$attrs" v-on="$listeners">
    <template v-slot="scope">
      <el-input-number v-model="scope.component.value" :min="0" :max="1000" v-bind="props" size="small"></el-input-number>
    </template>
  </ms-table-search-component>
</template>

<script>
import MsTableSearchComponent from "./MsTableSearchComponet";

export default {
  name: "MsTableSearchInputNumber",
  components: {
    MsTableSearchComponent
  },
  inheritAttrs: false,
  props: ['component'],
  data() {
    return {
      props: {
        ...this.component.props
      }
    }
  }
}
</script>

<style scoped>
/deep/ .el-input-number--small {
  width: 100%;
}
</style>
