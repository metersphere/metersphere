<template>
  <ms-table-search-component v-model="component.operator" :component="component">
    <template v-slot="scope">
      <el-date-picker
        v-model="scope.component.value" v-bind="scope.component.props"
        :placeholder="$t('commons.select_date')" size="small"
        :type="type" value-format="timestamp"
        :range-separator="$t('commons.range_separator')"
        :start-placeholder="$t('commons.start_date')"
        :end-placeholder="$t('commons.end_date')">
      </el-date-picker>
    </template>
  </ms-table-search-component>

</template>

<script>
  import MsTableSearchComponent from "./MsTableSearchComponet";
  import {OPERATORS} from "./search-components"

  export default {
    name: "MsTableSearchDatePicker",
    components: {MsTableSearchComponent},
    props: ['component'],
    computed: {
      type() {
        if (this.component.operator === OPERATORS.BETWEEN.value) {
          return "daterange";
        } else {
          return "date";
        }
      }
    }
  }
</script>

<style scoped>

</style>
