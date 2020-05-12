<template>
  <div>
    <el-row :gutter="10" type="flex" justify="space-between" align="middle">
      <el-col :span="10">
        <el-input v-model="common.variable" maxlength="60" size="small" @input="change"
                  :placeholder="$t('api_test.request.extract.variable_name')"/>
      </el-col>
      <el-col>
        <el-input v-model="common.expression" maxlength="255" size="small" :placeholder="expression"/>
      </el-col>
      <el-col class="extract-btn">
        <el-button type="danger" size="mini" icon="el-icon-delete" circle @click="remove" v-if="edit"/>
        <el-button type="primary" size="small" icon="el-icon-plus" plain @click="add" v-else/>
      </el-col>
    </el-row>
  </div>
</template>

<script>
  import {EXTRACT_TYPE, ExtractCommon} from "../../model/ScenarioModel";

  export default {
    name: "MsApiExtractCommon",

    props: {
      extractType: {
        type: String,
        validator: function (value) {
          return [EXTRACT_TYPE.XPATH, EXTRACT_TYPE.JSON_PATH, EXTRACT_TYPE.REGEX].indexOf(value) !== -1
        }
      },
      common: {
        type: ExtractCommon,
        default: () => {
          return new ExtractCommon();
        }
      },
      edit: {
        type: Boolean,
        default: false
      },
      index: Number,
      list: Array
    },

    methods: {
      add() {
        this.list.push(new ExtractCommon(this.extractType, this.common));
        this.clear();
      },
      change(variable) {
        this.common.value = "${" + variable + "}";
      },
      remove() {
        this.list.splice(this.index, 1);
      },
      clear() {
        this.common.variable = null;
        this.common.expression = null;
        this.common.value = null;
      }
    },

    computed: {
      expression() {
        switch (this.extractType) {
          case EXTRACT_TYPE.REGEX:
            return this.$t('api_test.request.extract.regex_expression');
          case EXTRACT_TYPE.JSON_PATH:
            return this.$t('api_test.request.extract.json_path_expression');
          case EXTRACT_TYPE.XPATH:
            return this.$t('api_test.request.extract.xpath_expression');
          default:
            return "";
        }
      }
    }
  }
</script>

<style scoped>
  .extract-btn {
    width: 60px;
  }
</style>
