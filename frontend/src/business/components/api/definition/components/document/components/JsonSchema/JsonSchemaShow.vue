<template>
  <div id="app" v-loading="loading">
    <el-row>
      <el-col>
        <el-button style="float: right" type="text" size="mini" @click="expandAll">
          {{ expandTitle }}
        </el-button>
      </el-col>
    </el-row>
    <div :style="jsonSchemaDisable ? '' : 'min-height: 200px'">
      <json-schema-panel class="schema"
                         :disabled="jsonSchemaDisable"
                         :value="schema"
                         :show-mock-vars="showMockVars"
                         :scenario-definition="scenarioDefinition"
                         :expand-all-params="expandAllParams"
                         @editScenarioAdvance="editScenarioAdvance"
                         lang="zh_CN" custom/>
    </div>
  </div>
</template>

<script>
import JsonSchemaPanel
  from "@/business/components/api/definition/components/document/components/JsonSchema/JsonSchemaPanel";

const Convert = require('../../../../../../common/json-schema/convert/convert.js');
const MsConvert = new Convert();

export default {
  name: 'JsonSchemaShow',
  components: {JsonSchemaPanel},
  props: {
    body: {},
    showPreview: {
      type: Boolean,
      default: true
    },
    jsonSchemaDisable: {
      type: Boolean,
      default: false
    },
    showMockVars: {
      type: Boolean,
      default() {
        return false;
      }
    },
    scenarioDefinition: Array,
  },
  created() {
    if (!this.body.jsonSchema && this.body.raw && this.checkIsJson(this.body.raw)) {
      let obj = {"root": MsConvert.format(JSON.parse(this.body.raw))}
      this.schema = obj;
    } else if (this.body.jsonSchema) {
      this.schema = {"root": this.body.jsonSchema};
    }
    this.body.jsonSchema = this.schema.root;
  },
  watch: {
    schema: {
      handler(newValue, oldValue) {
        this.body.jsonSchema = this.schema.root;
      },
      deep: true
    },
    body: {
      handler(newValue, oldValue) {
        if (!this.body.jsonSchema && this.body.raw && this.checkIsJson(this.body.raw)) {
          let obj = {"root": MsConvert.format(JSON.parse(this.body.raw))}
          this.schema = obj;
        } else if (this.body.jsonSchema) {
          this.schema = {"root": this.body.jsonSchema};
        }
        this.body.jsonSchema = this.schema.root;
      },
      deep: true
    }
  },
  data() {
    return {
      schema:
        {
          "root": {
            "type": "object",
            "properties": {},
          }
        },
      loading: false,
      expandAllParams: false,
    }
  },
  computed: {
    expandTitle() {
      return this.expandAllParams ? this.$t("commons.close_all") : this.$t("commons.expand_all");
    }
  },
  methods: {
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
      })
    },
    editScenarioAdvance(data) {
      this.$emit('editScenarioAdvance', data);
    },
  }
}
</script>
<style>

</style>
