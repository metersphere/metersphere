<template>
  <api-base-component
    v-loading="loading"
    @copy="copyRow"
    @remove="remove"
    @active="active"
    :data="assertions"
    :draggable="draggable"
    :is-max="isMax"
    :show-btn="showBtn"
    :inner-step="innerStep"
    :show-version="showVersion"
    :show-copy="false"
    color="#A30014"
    background-color="#F7E6E9"
    :title="$t('api_test.definition.request.scenario_assertions')">
    <el-row>
      <span>{{ $t('api_test.request.assertions.description') }}</span>
      <span style="float: right">
        <api-json-path-suggest-button
          :show-suggest-button="false"
          :clear-tip="$t('api_test.request.assertions.json_path_clear')"
          :isReadOnly="request.disabled"
          @clear="clearJson" />
      </span>
    </el-row>
    <div class="assertion-add" :draggable="draggable">
      <el-row :gutter="10">
        <el-col :span="4">
          <el-select
            :disabled="request.disabled"
            class="assertion-item"
            v-model="type"
            :placeholder="$t('api_test.request.assertions.select_type')"
            size="small">
            <el-option :label="$t('api_test.request.assertions.text')" :value="options.TEXT" />
            <el-option :label="$t('api_test.request.assertions.regex')" :value="options.REGEX" />
            <el-option :label="'JSONPath'" :value="options.JSON_PATH" />
            <el-option :label="'XPath'" :value="options.XPATH2" />
            <el-option :label="$t('api_test.request.assertions.response_time')" :value="options.DURATION" />
            <el-option :label="$t('api_test.request.assertions.jsr223')" :value="options.JSR223" />
            <el-option :label="$t('api_test.definition.request.document_structure')" :value="options.DOCUMENT" />
          </el-select>
        </el-col>
        <el-col :span="20">
          <ms-api-assertion-text
            :is-read-only="request.disabled"
            :list="assertions.regex"
            :callback="after"
            v-if="type === options.TEXT" />
          <ms-api-assertion-regex
            :is-read-only="request.disabled"
            :list="assertions.regex"
            :callback="after"
            @callback="after"
            v-if="type === options.REGEX" />
          <ms-api-assertion-json-path
            :is-read-only="request.disabled"
            :list="assertions.jsonPath"
            :callback="after"
            v-if="type === options.JSON_PATH" />
          <ms-api-assertion-x-path2
            :is-read-only="request.disabled"
            :list="assertions.xpath2"
            :callback="after"
            v-if="type === options.XPATH2" />
          <ms-api-assertion-duration
            v-model="time"
            :is-read-only="request.disabled"
            :duration="assertions.duration"
            :callback="after"
            v-if="type === options.DURATION" />
          <ms-api-assertion-jsr223
            :is-read-only="request.disabled"
            :list="assertions.jsr223"
            :callback="after"
            v-if="type === options.JSR223" />
          <ms-api-assertion-document
            :is-read-only="request.disabled"
            v-model="time"
            :document="assertions.document"
            :callback="after"
            v-if="type === options.DOCUMENT" />
          <el-button v-if="!type" :disabled="true" type="primary" size="mini">
            {{ $t('api_test.request.assertions.add') }}
          </el-button>
        </el-col>
      </el-row>
    </div>

    <ms-api-assertions-edit
      :is-read-only="request.disabled"
      :assertions="assertions"
      :apiId="apiId"
      :reloadData="reloadData"
      style="margin-bottom: 20px" />

    <ms-api-jsonpath-suggest
      :tip="$t('api_test.request.extract.suggest_tip')"
      @addSuggest="addJsonPathSuggest"
      ref="jsonpathSuggest" />
  </api-base-component>
</template>

<script>
import MsApiAssertionText from './ApiAssertionText';
import MsApiAssertionRegex from './ApiAssertionRegex';
import MsApiAssertionDuration from './ApiAssertionDuration';
import { ASSERTION_TYPE, JSONPath } from '../../model/ApiTestModel';
import MsApiAssertionsEdit from './ApiAssertionsEdit';
import MsApiAssertionJsonPath from './ApiAssertionJsonPath';
import MsApiAssertionJsr223 from './ApiAssertionJsr223';
import MsApiAssertionXPath2 from './ApiAssertionXPath2';
import { getUUID } from 'metersphere-frontend/src/utils';
import ApiJsonPathSuggestButton from './ApiJsonPathSuggestButton';
import MsApiJsonpathSuggest from './ApiJsonpathSuggest';
import ApiBaseComponent from '../../../automation/scenario/common/ApiBaseComponent';
import MsApiAssertionDocument from './document/DocumentHeader';

export default {
  name: 'MsApiAssertions',
  components: {
    ApiBaseComponent,
    MsApiJsonpathSuggest,
    ApiJsonPathSuggestButton,
    MsApiAssertionXPath2,
    MsApiAssertionJsr223,
    MsApiAssertionJsonPath,
    MsApiAssertionsEdit,
    MsApiAssertionDuration,
    MsApiAssertionRegex,
    MsApiAssertionText,
    MsApiAssertionDocument,
  },
  props: {
    draggable: {
      type: Boolean,
      default: false,
    },
    isMax: {
      type: Boolean,
      default: false,
    },
    innerStep: {
      type: Boolean,
      default: false,
    },
    showBtn: {
      type: Boolean,
      default: true,
    },
    showVersion: {
      type: Boolean,
      default: true,
    },
    assertions: {},
    node: {},
    request: {},
    apiId: String,
    response: {},
    customizeStyle: {
      type: String,
      default: 'margin-top: 10px',
    },
    isReadOnly: {
      type: Boolean,
      default: false,
    },
  },
  data() {
    return {
      options: ASSERTION_TYPE,
      time: '',
      type: '',
      loading: false,
      reloadData: '',
    };
  },
  methods: {
    after() {
      this.type = '';
      this.reloadData = getUUID().substring(0, 8);
      this.reload();
    },
    copyRow() {
      this.$emit('copyRow', this.assertions, this.node);
    },
    suggestJsonOpen() {
      this.$emit('suggestClick');
      this.$nextTick(() => {
        if (!this.response || !this.response.responseResult || !this.response.responseResult.body) {
          this.$message(this.$t('api_test.request.assertions.debug_first'));
          return;
        }
        this.$refs.jsonpathSuggest.open(this.response.responseResult.body);
      });
    },
    reload() {
      this.loading = true;
      this.$nextTick(() => {
        this.loading = false;
      });
    },
    active() {
      this.assertions.active = !this.assertions.active;
      this.reload();
    },
    remove() {
      this.$emit('remove', this.assertions, this.node);
    },
    addJsonPathSuggest(data) {
      let jsonItem = new JSONPath();
      jsonItem.expression = data.path;
      jsonItem.expect = data.value;
      jsonItem.setJSONPathDescription();
      let expect = jsonItem.expect;
      if (expect) {
        expect = expect
          .replaceAll('\\', '\\\\')
          .replaceAll('(', '\\(')
          .replaceAll(')', '\\)')
          .replaceAll('+', '\\+')
          .replaceAll('?', '\\?')
          .replaceAll('/', '\\/')
          .replaceAll('*', '\\*')
          .replaceAll('^', '\\^')
          .replaceAll('$', '\\$');
      }
      jsonItem.expect = expect;
      jsonItem.enable = true;
      this.assertions.jsonPath.push(jsonItem);
    },
    clearJson() {
      this.assertions.jsonPath = [];
    },
  },
};
</script>

<style scoped>
.assertion-item {
  width: 100%;
}

.assertion-add {
  padding: 10px;
  margin: 5px 0;
  border-radius: 5px;
  border: #dcdfe6 solid 1px;
}

.icon.is-active {
  transform: rotate(90deg);
}
:deep(.header-right) {
  margin-top: 4px;
}
:deep(.enable-switch) {
  margin: 0px 0px 0px;
  padding-bottom: 3px;
  width: 30px;
}
</style>
