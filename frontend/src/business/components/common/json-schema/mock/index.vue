<template>
  <div>
    <el-autocomplete
      size="small"
      :disabled="false"
      class="input-with-autocomplete"
      v-model="schema.mock"
      :fetch-suggestions="funcSearch"
      :placeholder="$t('api_test.value')"
      value-key="name"
      highlight-first-item
      @select="change">
      <i slot="suffix" class="el-input__icon el-icon-edit pointer" @click="advanced()"></i>
    </el-autocomplete>

    <ms-advance ref="variableAdvance" :current-item="schema"/>
  </div>

</template>

<script>
  import {JMETER_FUNC, MOCKJS_FUNC} from "@/common/js/constants";
  import MsAdvance from "./Advance";

  export default {
    name: 'MockSelect',
    components: {MsAdvance},
    props: {
      schema: {
        type: Object,
        default: () => {
        }
      },
      mock: {
        type: Array,
        default: () => []
      }
    },
    data() {
      return {
        mockValue: ''
      }
    },
    created() {
    },
    mounted() {
    },
    methods: {
      funcSearch(queryString, cb) {
        let funcs = MOCKJS_FUNC.concat(JMETER_FUNC);
        let results = queryString ? funcs.filter(this.funcFilter(queryString)) : funcs;
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
      advanced() {
        this.$refs.variableAdvance.open();
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
      }
    }
  }
</script>

<style lang="scss" scoped>
</style>
