<template>
  <ms-table-search-component v-model="component.operator.value" :component="component">
    <template v-slot="scope">
      <el-date-picker
        v-model="scope.component.value" v-bind="scope.component.props"
        :placeholder="$t('commons.date.select_date')" size="small"
        :type="type" :key="type" value-format="timestamp"
        :range-separator="$t('commons.date.range_separator')"
        :start-placeholder="$t('commons.date.start_date')"
        :end-placeholder="$t('commons.date.end_date')">
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
    data() {
      return {
        type: "daterange"
      }
    },
    methods: {
      change(value) {
        if (value === OPERATORS.BETWEEN.value) {
          if (!Array.isArray(this.component.value)) {
            this.component.value = [];
          }
          this.type = "daterange";
        } else {
          if (Array.isArray(this.component.value)) {
            this.component.value = "";
          }
          this.type = "date";
        }
      }
    }
  }
</script>

<style scoped>

</style>
