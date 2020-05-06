<template>
  <div>
    <el-row :gutter="10" type="flex" justify="space-between" align="middle">
      <el-col class="assertion-select">
        <el-select class="assertion-item" v-model="regex.subject" size="small"
                   :placeholder="$t('api_test.request.assertions.select_subject')">
          <el-option label="Response Code" :value="subjects.RESPONSE_CODE"/>
          <el-option label="Response Headers" :value="subjects.RESPONSE_HEADERS"/>
          <el-option label="Response Data" :value="subjects.RESPONSE_DATA"/>
        </el-select>
      </el-col>
      <el-col>
        <el-input v-model="regex.expression" maxlength="255" size="small" show-word-limit
                  :placeholder="$t('api_test.request.assertions.expression')"/>
      </el-col>
      <el-col class="assertion-btn">
        <el-button type="danger" size="mini" icon="el-icon-delete" circle @click="remove" v-if="edit"/>
        <el-button type="primary" size="small" icon="el-icon-plus" plain @click="add" v-else/>
      </el-col>
    </el-row>
  </div>
</template>

<script>
  import {ASSERTION_REGEX_SUBJECT, Regex} from "../model/ScenarioModel";

  export default {
    name: "MsApiAssertionRegex",

    props: {
      regex: {
        type: Regex,
        default: () => {
          return new Regex();
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
        subjects: ASSERTION_REGEX_SUBJECT,
      }
    },

    methods: {
      add: function () {
        this.list.push(new Regex(this.regex));
      },
      remove: function () {
        this.list.splice(this.index, 1);
      }
    }
  }
</script>

<style scoped>
  .assertion-select {
    width: 250px;
  }

  .assertion-item {
    width: 100%;
  }

  .assertion-btn {
    text-align: center;
    width: 60px;
  }
</style>
