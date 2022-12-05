<template>
  <div id="app" v-loading="loading">
    <el-row>
      <el-col></el-col>
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
    </el-row>
    <div :style="jsonSchemaDisable ? '' : 'min-height: 200px'">
      <div style="overflow: auto">
        <json-schema-panel
          class="schema"
          v-if="reloadedApiVariable"
          :disabled="jsonSchemaDisable"
          :value="schema"
          :show-mock-vars="showMockVars"
          :scenario-definition="scenarioDefinition"
          :param-columns="apiJsonSchemaShowColumns"
          :expand-all-params="expandAllParams"
          @editScenarioAdvance="editScenarioAdvance"
          lang="zh_CN"
          custom />
      </div>
    </div>
  </div>
</template>

<script>
import JsonSchemaPanel from '@/business/definition/components/document/components/JsonSchema/JsonSchemaPanel';
import { getApiJsonSchemaConfigFields, getShowFields } from 'metersphere-frontend/src/utils/custom_field';
import ApiParamsConfig from '@/business/definition/components/request/components/ApiParamsConfig';

const Convert = require('@/business/commons/json-schema/convert/convert.js');
const MsConvert = new Convert();

export default {
  name: 'JsonSchemaShow',
  components: { JsonSchemaPanel, ApiParamsConfig },
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
      reloadedApiVariable: true,
      storageKey: 'API_JSON_SCHEMA_SHOW_FIELD',
      apiJsonSchemaConfigFields: getApiJsonSchemaConfigFields(this),
      apiJsonSchemaShowColumns: [],
      loading: false,
      expandAllParams: false,
    };
  },
  computed: {
    expandTitle() {
      return this.expandAllParams ? this.$t('commons.close_all') : this.$t('commons.expand_all');
    },
  },
  methods: {
    refreshApiParamsField() {
      this.apiJsonSchemaShowColumns = getShowFields(this.storageKey);
    },
    expandAll() {
      this.expandAllParams = !this.expandAllParams;
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
  },
};
</script>
<style></style>
