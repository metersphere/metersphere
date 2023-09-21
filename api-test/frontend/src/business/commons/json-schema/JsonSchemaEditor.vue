<template>
  <div id="app" v-loading="loading">
    <el-tabs v-model="activeName" @tab-click="handleClick">
      <el-tab-pane :label="$t('organization.message.template')" name="apiTemplate">
        <div>
          <el-button
            v-show="!jsonSchemaDisable"
            type="primary"
            size="mini"
            style="margin: 10px 10px 0px"
            :disabled="isReadOnly"
            @click="openOneClickOperation">
            {{ this.$t('commons.import') }}
          </el-button>
          <div style="float: right">
            <el-button style="margin-right: 5px" type="text" size="mini" @click="expandAll">
              {{ expandTitle }}
            </el-button>
            <api-params-config
              v-if="apiJsonSchemaConfigFields"
              :storage-key="storageKey"
              @refresh="refreshApiParamsField"
              :api-params-config-fields="apiJsonSchemaConfigFields" />
          </div>
        </div>
        <div :style="jsonSchemaDisable ? '' : 'min-height: 200px'">
          <div style="overflow: auto; width: 100%">
            <json-schema-editor
              v-if="reloadedApiVariable"
              class="schema"
              :disabled="jsonSchemaDisable || isReadOnly"
              :value="schema"
              :show-mock-vars="showMockVars"
              :expand-all-params="expandAllParams"
              :scenario-definition="scenarioDefinition"
              @editScenarioAdvance="editScenarioAdvance"
              @bodyReload="reloadBody"
              :param-columns="apiJsonSchemaShowColumns"
              :need-mock="needMock"
              lang="zh_CN"
              custom />
          </div>
        </div>
      </el-tab-pane>
      <el-tab-pane v-if="showPreview" :label="$t('schema.preview')" name="preview">
        <div style="min-height: 200px">
          <pre style="width: 100%; white-space: pre-wrap">{{ this.preview }}</pre>
        </div>
      </el-tab-pane>
    </el-tabs>

    <ms-import-json ref="importJson" @jsonData="jsonData" />
  </div>
</template>

<script>
import MsImportJson from './import/ImportJson';
import JsonSchemaEditor from '@/business/commons/json-schema/schema/editor/index';
import { getApiJsonSchemaConfigFields, getShowFields } from 'metersphere-frontend/src/utils/custom_field';
import ApiParamsConfig from '@/business/definition/components/request/components/ApiParamsConfig';

const Convert = require('./convert/convert.js');
const MsConvert = new Convert();

export default {
  name: 'App',
  components: { MsImportJson, JsonSchemaEditor, ApiParamsConfig },
  props: {
    body: {},
    showPreview: {
      type: Boolean,
      default: true,
    },
    jsonSchemaDisable: {
      type: Boolean,
      default: false,
    },
    showMockVars: {
      type: Boolean,
      default() {
        return false;
      },
    },
    scenarioDefinition: Array,
    needMock: {
      type: Boolean,
      default() {
        return true;
      },
    },
    isReadOnly: {
      type: Boolean,
      default: false,
    },
  },
  created() {
    if (!this.body.jsonSchema && this.body.raw && this.checkIsJson(this.body.raw)) {
      let obj = { root: MsConvert.format(JSON.parse(this.body.raw)) };
      this.schema = obj;
    } else if (this.body.jsonSchema) {
      this.schema = { root: this.body.jsonSchema };
    }
    this.body.jsonSchema = this.schema.root;
    this.apiJsonSchemaShowColumns = getShowFields(this.storageKey);
  },
  watch: {
    schema: {
      handler(newValue, oldValue) {
        this.body.jsonSchema = this.schema.root;
      },
      deep: true,
    },
    body: {
      handler(newValue, oldValue) {
        if (!this.body.jsonSchema && this.body.raw && this.checkIsJson(this.body.raw)) {
          let obj = { root: MsConvert.format(JSON.parse(this.body.raw)) };
          this.schema = obj;
        } else if (this.body.jsonSchema) {
          this.schema = { root: this.body.jsonSchema };
        }
        this.body.jsonSchema = this.schema.root;
      },
      deep: true,
    },
  },
  data() {
    return {
      schema: {
        root: {
          type: 'object',
          properties: {},
        },
      },
      loading: false,
      reloadedApiVariable: true,
      expandAllParams: false,
      preview: null,
      activeName: 'apiTemplate',
      storageKey: 'API_JSON_SCHEMA_SHOW_FIELD',
      apiJsonSchemaConfigFields: getApiJsonSchemaConfigFields(this),
      apiJsonSchemaShowColumns: [],
    };
  },
  methods: {
    reloadBody() {
      this.$emit('headersChange');
    },
    refreshApiParamsField() {
      this.apiJsonSchemaShowColumns = getShowFields(this.storageKey);
    },
    handleClick() {
      if (this.activeName === 'preview') {
        this.loading = true;
        // 后台转换
        MsConvert.preview(this.schema.root, (result) => {
          this.preview = result;
          this.loading = false;
        });
      }
    },
    openOneClickOperation() {
      this.$refs.importJson.openOneClickOperation();
    },
    checkIsJson(json) {
      try {
        JSON.parse(json);
        return true;
      } catch (e) {
        return false;
      }
    },
    jsonData(data) {
      this.schema.root = {};
      this.$nextTick(() => {
        this.schema.root = data;
        this.body.jsonSchema = this.schema.root;
      });
    },
    editScenarioAdvance(data) {
      this.$emit('editScenarioAdvance', data);
    },
    expandAll() {
      this.expandAllParams = !this.expandAllParams;
    },
  },
  computed: {
    expandTitle() {
      return this.expandAllParams ? this.$t('commons.close_all') : this.$t('commons.expand_all');
    },
  },
};
</script>
<style></style>
