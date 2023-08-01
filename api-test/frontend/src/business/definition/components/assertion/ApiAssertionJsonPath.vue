<template>
  <div v-loading="loading">
    <el-row :gutter="10" type="flex" justify="space-between" align="middle">
      <el-col>
        <el-tooltip :disabled="showTip" placement="top" :content="jsonPath.expression">
          <el-input
            :disabled="(isReadOnly && !jsonPath.label) || caseEnable"
            v-model="jsonPath.expression"
            maxlength="500"
            size="small"
            show-word-limit
            :placeholder="$t('api_test.request.extract.json_path_expression')"/>
        </el-tooltip>
      </el-col>
      <el-col>
        <el-select
          :disabled="(isReadOnly && !jsonPath.label) || caseEnable"
          v-model="jsonPath.option"
          class="ms-col-type"
          size="small"
          style="width: 40%; margin-right: 10px"
          @change="setJSONPathDescription">
          <el-option :label="$t('api_test.request.assertions.contains')" value="CONTAINS"/>
          <el-option :label="$t('api_test.request.assertions.not_contains')" value="NOT_CONTAINS"/>
          <el-option :label="$t('api_test.request.assertions.equals')" value="EQUALS"/>
          <el-option :label="$t('commons.adv_search.operators.not_equals')" value="NOT_EQUALS"/>
          <el-option :label="$t('commons.adv_search.operators.gt')" value="GT"/>
          <el-option :label="$t('commons.adv_search.operators.lt')" value="LT"/>
          <el-option :label="$t('api_test.request.assertions.regular_match')" value="REGEX"/>
        </el-select>
        <el-input
          :disabled="(isReadOnly && !jsonPath.label) || caseEnable"
          v-model="jsonPath.expect"
          size="small"
          show-word-limit
          :placeholder="$t('api_test.request.assertions.expect')"
          style="width: 50%"/>
        <el-tooltip placement="top" v-if="jsonPath.option === 'REGEX'">
          <div slot="content">
            {{ $t('api_test.request.assertions.regex_info') }}
          </div>
          <i class="el-icon-question" style="cursor: pointer"/>
        </el-tooltip>
      </el-col>
      <el-col class="assertion-btn">
        <el-tooltip :content="$t('test_resource_pool.enable_disable')" placement="top" v-if="edit">
          <el-switch
            v-model="jsonPath.enable"
            class="enable-switch"
            size="mini"
            :disabled="(isReadOnly && !jsonPath.label) || caseEnable"
            style="width: 30px; margin-right: 10px"/>
        </el-tooltip>
        <el-button
          :disabled="(isReadOnly && !jsonPath.label) || caseEnable"
          size="mini"
          icon="el-icon-copy-document"
          circle
          @click="copyRow"
          v-if="edit"/>
        <el-button
          :disabled="(isReadOnly && !jsonPath.label) || caseEnable"
          type="danger"
          size="mini"
          icon="el-icon-delete"
          circle
          @click="remove"
          v-if="edit"/>
        <el-button :disabled="(isReadOnly && !jsonPath.label) || caseEnable" type="primary" size="mini" @click="add" v-else>
          {{ $t('api_test.request.assertions.add') }}
        </el-button>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import {JSONPath} from '../../model/ApiTestModel';

export default {
  name: 'MsApiAssertionJsonPath',

  props: {
    jsonPath: {
      default: () => {
        return new JSONPath();
      },
    },
    edit: {
      type: Boolean,
      default: false,
    },
    index: Number,
    list: Array,
    callback: Function,
    isReadOnly: {
      type: Boolean,
      default: false,
    },
    caseEnable: {
      type: Boolean,
      default: false,
    },
  },

  created() {
    if (!this.jsonPath.option) {
      this.jsonPath.option = 'REGEX';
      if (this.jsonPath && this.isReadOnly) {
        this.jsonPath.label = "SCENARIO-REF-STEP";
      }
    }
    this.showTip = !this.jsonPath || !this.jsonPath.expression || this.jsonPath.expression.length < 50;
  },

  data() {
    return {
      loading: false,
      showTip: true,
    };
  },

  watch: {
    'jsonPath.expect'() {
      this.setJSONPathDescription();
    },
    'jsonPath.expression'() {
      this.setJSONPathDescription();
    },
  },

  methods: {
    add: function () {
      this.list.push(this.getJSONPath());
      this.callback();
    },
    remove: function () {
      this.list.splice(this.index, 1);
    },
    getJSONPath() {
      let jsonPath = new JSONPath(this.jsonPath);
      jsonPath.enable = true;
      jsonPath.description = jsonPath.expression + ' expect: ' + (jsonPath.expect ? jsonPath.expect : '');
      return jsonPath;
    },
    reload() {
      this.loading = true;
      this.$nextTick(() => {
        this.loading = false;
      });
    },
    setJSONPathDescription() {
      this.showTip = !this.jsonPath || !this.jsonPath.expression || this.jsonPath.expression.length < 50;
      this.jsonPath.description =
        this.jsonPath.expression + " " + this.jsonPath.option + ": " + (this.jsonPath.expect ? this.jsonPath.expect : '');
      this.reload();
    },
    copyRow() {
      let jsonPath = new JSONPath(this.jsonPath);
      jsonPath.description = jsonPath.expression + ' expect: ' + (jsonPath.expect ? jsonPath.expect : '');
      this.list.splice(this.index + 1, 0, jsonPath);
    },
  },
};
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
  width: 120px;
}
</style>
