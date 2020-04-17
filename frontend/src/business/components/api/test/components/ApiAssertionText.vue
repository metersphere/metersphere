<template>
  <div>
    <el-row :gutter="10">
      <el-col :span="4">
        <el-select class="assertion-item" v-model="subject" size="small"
                   :placeholder="$t('api_test.request.assertions.select_subject')">
          <el-option label="Http-Code" value="HTTP-CODE"></el-option>
          <el-option label="Header" value="HEADER"></el-option>
          <el-option label="Body" value="BODY"></el-option>
        </el-select>
      </el-col>
      <el-col :span="4">
        <el-select class="assertion-item" v-model="condition" size="small"
                   :placeholder="$t('api_test.request.assertions.select_contains')">
          <el-option :label="$t('api_test.request.assertions.contains')" value="CONTAINS"></el-option>
          <el-option :label="$t('api_test.request.assertions.not_contains')" value="NOT_CONTAINS"></el-option>
          <el-option :label="$t('api_test.request.assertions.equals')" value="EQUALS"></el-option>
          <el-option :label="$t('api_test.request.assertions.start_with')" value="START_WITH"></el-option>
          <el-option :label="$t('api_test.request.assertions.end_with')" value="END_WITH"></el-option>
        </el-select>
      </el-col>
      <el-col :span="15">
        <el-input v-model="value" maxlength="255" size="small" show-word-limit
                  :placeholder="$t('api_test.request.assertions.value')"/>
      </el-col>
      <el-col :span="1">
        <el-button type="primary" size="small" icon="el-icon-plus" plain @click="add"/>
      </el-col>
    </el-row>
  </div>
</template>

<script>
  import {Regex} from "../model/APIModel";

  export default {
    name: "MsApiAssertionText",

    props: {
      list: Array
    },

    data() {
      return {
        subject: "",
        condition: "",
        value: ""
      }
    },

    methods: {
      add: function () {
        this.list.push(this.toRegex());
      },
      toRegex: function () {
        let expression = "";
        let description = "";
        switch (this.condition) {
          case "CONTAINS":
            expression = ".*" + this.value + ".*";
            description = "contains: " + this.value;
            break;
          case "NOT_CONTAINS":
            expression = "^((?!" + this.value + ").)*$";
            description = "not contains: " + this.value;
            break;
          case "EQUALS":
            expression = "^" + this.value + "$";
            description = "equals: " + this.value;
            break;
          case "START_WITH":
            expression = "^" + this.value;
            description = "start with: " + this.value;
            break;
          case "END_WITH":
            expression = this.value + "$";
            description = "end with: " + this.value;
            break;
        }
        return new Regex({
            subject: this.subject,
            expression: expression,
            description: description
          }
        );
      }
    }
  }
</script>

<style scoped>
  .assertion-item {
    width: 100%;
  }
</style>
