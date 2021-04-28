<template>
  <div>
    <el-form :model="editData" label-position="right" label-width="80px" size="small" ref="form" :rules="rules">
      <el-form-item :label="$t('api_test.variable_name')" prop="name">
        <el-input v-model="editData.name" :placeholder="$t('api_test.variable_name')" ref="nameInput"></el-input>
      </el-form-item>

      <el-form-item :label="$t('commons.description')" prop="description">
        <el-input class="ms-http-textarea"
                  v-model="editData.description"
                  type="textarea"
                  :autosize="{ minRows: 2, maxRows: 10}"
                  :rows="2" size="small"/>
      </el-form-item>

      <el-form-item :label="$t('api_test.value')" prop="value">
        <el-col class="item">
          <el-autocomplete
            size="small"
            :placeholder="$t('api_test.value')"
            style="width: 100%;"
            v-model="editData.value"
            value-key="name"
            :fetch-suggestions="funcSearch"
            highlight-first-item>
            <i slot="suffix" class="el-input__icon el-icon-edit pointer" @click="advanced"></i>
          </el-autocomplete>
        </el-col>
      </el-form-item>
    </el-form>
    <ms-api-variable-advance ref="variableAdvance"/>
  </div>
</template>

<script>
  import {JMETER_FUNC, MOCKJS_FUNC} from "@/common/js/constants";
  import MsApiVariableAdvance from "../../../test/components/ApiVariableAdvance";

  export default {
    name: "MsEditConstant",
    components: {MsApiVariableAdvance},
    props: {
      editData: {},
    },
    data() {
      return {
        rules: {
          name: [
            {required: true, message: this.$t('test_track.case.input_name'), trigger: 'blur'},
          ],
        },
      }
    },
    methods: {
      advanced() {
        this.$refs.variableAdvance.open();
      },
      createFilter(queryString) {
        return (variable) => {
          return (variable.value.toLowerCase().indexOf(queryString.toLowerCase()) === 0);
        };
      },
      funcFilter(queryString) {
        return (func) => {
          return (func.name.toLowerCase().indexOf(queryString.toLowerCase()) > -1);
        };
      },
      funcSearch(queryString, cb) {
        let funcs = MOCKJS_FUNC.concat(JMETER_FUNC);
        let results = queryString ? funcs.filter(this.funcFilter(queryString)) : funcs;
        // 调用 callback 返回建议列表的数据
        cb(results);
      },
    },
    created() {
      this.$nextTick(() => {
        this.$refs.nameInput.focus();
      });
    },
  }
</script>

<style scoped>

</style>
