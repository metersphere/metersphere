<template>
  <div id="app">
    <el-tabs v-model="activeName" @tab-click="handleClick">
      <el-tab-pane label="模版" name="apiTemplate">
        <el-button type="primary" size="mini" style="margin-left: 10px" @click="openOneClickOperation">导入</el-button>
        <div style="min-height: 400px">
          <json-schema-editor class="schema" :value="schema" lang="zh_CN" custom/>
        </div>
      </el-tab-pane>
      <el-tab-pane label="预览" name="preview">
        <div style="min-height: 400px">
          <pre>{{this.preview}}</pre>
        </div>
      </el-tab-pane>
    </el-tabs>

    <ms-import-json ref="importJson" @jsonData="jsonData"/>
  </div>
</template>

<script>
  import {schemaToJson} from './common';
  import json5 from 'json5';
  import MsImportJson from './import/ImportJson';

  const GenerateSchema = require('generate-schema/src/schemas/json.js');

  export default {
    name: 'App',
    components: {MsImportJson},
    props: {
      body: {},
    },
    created() {
      if (!this.body.jsonSchema && this.body.raw) {
        let obj = {"root": GenerateSchema(JSON.parse(this.body.raw))}
        this.body.jsonSchema = obj;
        this.schema = this.body.jsonSchema;
      }
      else if (this.body.jsonSchema) {
        this.schema = this.body.jsonSchema;
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
        preview: null,
        activeName: "apiTemplate",
      }
    },
    methods: {
      handleClick() {
        if (this.activeName === 'preview') {
          console.log(this.schema)
          this.preview = schemaToJson(json5.parse(JSON.stringify(this.schema)));
        }
      },
      openOneClickOperation() {
        this.$refs.importJson.openOneClickOperation();
      },
      jsonData(data) {
        let obj = {"root": data}
        this.schema = obj;
      }
    }
  }
</script>
<style>

</style>
