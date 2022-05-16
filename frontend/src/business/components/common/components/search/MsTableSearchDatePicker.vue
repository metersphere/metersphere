<template>
  <ms-table-search-component v-model="component.operator.value" :component="component" v-bind="$attrs"
                             v-on="$listeners">
    <template v-slot="scope">
      <el-date-picker
        v-model="scope.component.value" v-bind="scope.component.props"
        :placeholder="$t('commons.date.select_date')" size="small"
        :type="type" :key="type" value-format="timestamp"
        class="ms-el-date-picker"
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
  inheritAttrs: false,
  methods: {
    change(value) {
      if (value === OPERATORS.BETWEEN.value) {
        if (!Array.isArray(this.component.value)) {
          this.component.value = [];
        }
      } else {
        if (Array.isArray(this.component.value)) {
          this.component.value = "";
        }
      }
    }
  },
  computed: {
    type() {
      if (this.component.operator.value === OPERATORS.BETWEEN.value) {
        return "daterange";
      } else {
        return "date";
      }
    }
  }
}
</script>

<style scoped>
.ms-el-date-picker >>> .el-date-editor, .el-range-editor.el-input__inner,
.el-date-editor--daterange {
  width: 100%;
}
</style>
