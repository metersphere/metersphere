<template>
  <ms-table-search-component v-model="component.operator" :component="component">
    <template v-slot="scope">
      <el-date-picker
        v-model="scope.component.value" v-bind="scope.component.props"
        :placeholder="$t('commons.select_date_time')" size="small"
        :type="type" value-format="timestamp"
        :range-separator="$t('commons.range_separator')"
        :start-placeholder="$t('commons.start_date_time')"
        :end-placeholder="$t('commons.end_date_time')">
      </el-date-picker>
    </template>
  </ms-table-search-component>

</template>

<script>
  import MsTableSearchComponent from "./MsTableSearchComponet";
  import {OPERATORS} from "./search-components"

  export default {
    name: "MsTableSearchDateTimePicker",
    components: {MsTableSearchComponent},
    props: ['component'],
    computed: {
      type() {
        if (this.component.operator === OPERATORS.BETWEEN.value) {
          return "datetimerange";
        } else {
          return "datetime";
        }
      }
    }
  }
</script>

<style scoped>

</style>
