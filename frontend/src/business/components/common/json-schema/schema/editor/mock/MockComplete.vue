<template>
  <div>
    <el-autocomplete
      size="small"
      class="input-with-autocomplete"
      v-model="mock.mock"
      :fetch-suggestions="funcSearch"
      :disabled="disabled"
      :placeholder="$t('api_test.value')"
      value-key="name"
      highlight-first-item
      @select="change">
      <i slot="suffix" v-if="!disabled" class="el-input__icon el-icon-edit pointer" @click="advanced(mock)"></i>
    </el-autocomplete>
    <ms-api-variable-advance :show-mock-vars="showMockVars" :scenario-definition="scenarioDefinition" :current-item="mock" ref="variableAdvance"/>

  </div>
</template>

<script>
  import {JMETER_FUNC, MOCKJS_FUNC} from "@/common/js/constants";
  import MsApiVariableAdvance from "../../../../../api/definition/components/ApiVariableAdvance";

  export default {
    name: 'MsMock',
    components: {MsApiVariableAdvance},
    props: {
      schema: {
        type: Object,
        default: () => {
        }
      },
      disabled: Boolean,
      scenarioDefinition: Array,
      showMockVars: {
        type: Boolean,
        default() {
          return false;
        }
      },
    },
    data() {
      return {
        mock: {mock: ""}
      }
    },
    created() {
      if (this.schema.mock && Object.prototype.toString.call(this.schema.mock).match(/\[object (\w+)\]/)[1].toLowerCase() === 'object') {
        this.mock = this.schema.mock;
      } else {
        this.schema.mock = this.mock;
      }
      if (this.schema.type === 'object') {
        this.$delete(this.schema, 'mock')
      }
      this.mock.mock = this.mock.mock + "";
    },
    watch: {
      schema: {
        handler(newValue, oldValue) {
          this.schema.mock = this.mock;
        },
        deep: true
      }
    },
    mounted() {
    },
    methods: {
      funcSearch(queryString, cb) {
        let results = [];
        if(!this.showMockVars){
          let funcs = MOCKJS_FUNC.concat(JMETER_FUNC);
          results = queryString ? funcs.filter(this.funcFilter(queryString)) : funcs;
        }
        // 调用 callback 返回建议列表的数据
        cb(results);
      },
      funcFilter(queryString) {
        return (func) => {
          return (func.name.toLowerCase().indexOf(queryString.toLowerCase()) > -1);
        };
      },
      change: function () {
      },
      advanced(item) {
        this.mock = item;
        // 冒泡到父组件，调用父组件的参数设置打开方法
        if (this.scenarioDefinition != undefined) {
          this.$emit('editScenarioAdvance', this.mock);
        } else {
          this.$refs.variableAdvance.open();
        }
      },
      showEdit() {
        this.$emit('showEdit')
      },
      handleChange(e) {
        this.$emit('change', e)
      },
      querySearchAsync(queryString, cb) {
        const arr = this.mock || []
        const results = queryString
          ? arr.filter(this.createStateFilter(queryString))
          : arr

        cb(results)
      },
      createStateFilter(queryString) {
        return state => {
          return (
            state.value.toLowerCase().indexOf(queryString.toLowerCase()) === 0
          )
        }
      },
      editScenarioAdvance(data) {
        this.$emit('editScenarioAdvance', data);
      },
    }
  }
</script>

<style lang="scss" scoped>
</style>
