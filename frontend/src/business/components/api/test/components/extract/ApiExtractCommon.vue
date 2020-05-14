<template>
  <div>
    <el-row :gutter="10" type="flex" justify="space-between" align="middle">
      <el-col :span="10">
        <ms-api-variable-input v-model="common.variable" size="small" maxlength="60" @change="change"
                               :placeholder="$t('api_test.variable_name')"/>
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
  import MsApiVariableInput from "../ApiVariableInput";

  export default {
    name: "MsApiExtractCommon",
    components: {MsApiVariableInput},
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

    data() {
      return {
        visible: false
      }
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
      },
      copy() {
        let input = document.createElement("input");
        document.body.appendChild(input);
        input.value = this.common.value;
        input.select();
        if (input.setSelectionRange) {
          input.setSelectionRange(0, input.value.length);
        }
        document.execCommand("copy");
        document.body.removeChild(input);
        this.visible = true;
        setTimeout(() => {
          this.visible = false;
        }, 1000);
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
  .variable {
    position: relative;
  }

  .variable-combine {
    color: #7F7F7F;
    max-width: 80px;
    line-height: 32px;
    position: absolute;
    top: 0;
    right: 25px;
    margin-right: -20px;
    display: flex;
    align-items: center;
  }

  .variable-combine .value {
    display: inline-block;
    max-width: 60px;
    margin-right: 10px;
    overflow-x: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .variable-combine .copy {
    font-size: 14px;
    cursor: pointer;
    color: #1E90FF;
  }

  .extract-btn {
    width: 60px;
  }
</style>
