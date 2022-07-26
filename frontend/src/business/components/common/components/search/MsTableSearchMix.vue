<template>
  <ms-table-search-component v-model="component.operator.value" :component="component" v-bind="$attrs" v-on="$listeners">
    <template v-slot="scope">
      <el-select v-if="!component.showInput" v-model="scope.component.value" :placeholder="$t('commons.please_select')" size="small"
                 filterable v-bind="scope.component.props" class="search-select" v-loading="result.loading">
        <el-option v-for="op in options" :key="op.value" :label="label(op)" :value="op.value">
          <span class="demand-span">{{label(op)}}</span>
        </el-option>
      </el-select>
      <el-input v-model="scope.component.value" v-if="component.showInput" :placeholder="$t('commons.input_content')" size="small"/>
    </template>
  </ms-table-search-component>
</template>

<script>
import MsTableSearchComponent from "./MsTableSearchComponet";
import {getCurrentProjectID, getCurrentUser} from "@/common/js/utils";

export default {
  name: "MsTableSearchMix",
  components: {MsTableSearchComponent},
  props: ['component'],
  inheritAttrs: false,
  data() {
    return {
      result: {
        loading: false
      },
      options: !(this.component.options instanceof Array) ? [] : this.component.options || []
    }
  },
  created() {
    if (!(this.component.options instanceof Array) && this.component.options.url) {
      // is need to add project properties
      let projectId = getCurrentProjectID();
      if (!projectId) {
        projectId = getCurrentUser().lastProjectId;
      }
      this.component.options.url += '/' + projectId;
      this.result = this.$get(this.component.options.url, response => {
        if (response.data) {
          response.data.forEach(item => {
            this.options.push({
              label: item[this.component.options.labelKey],
              value: item[this.component.options.valueKey]
            })
          })
        }
      })
    }
  },
  computed: {
    label() {
      return op => {
        if (this.component.options.showLabel) {
          return this.component.options.showLabel(op);
        }
        if (op.label) {
          return op.label.indexOf(".") !== -1 ? this.$t(op.label) : op.label;
        } else {
          // 自定义字段
          return op.text;
        }
      }
    }
  }
}
</script>

<style scoped>
.search-select {
  display: inline-block;
  width: 100%;
}
.demand-span {
  display: inline-block;
  max-width: 400px;
  white-space: nowrap;
  text-overflow: ellipsis;
  overflow: hidden;
  word-break: break-all;
  margin-right: 5px;
}
</style>
