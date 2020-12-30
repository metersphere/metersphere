<template>
  <div id="app">
    <el-tabs v-model="activeName" @tab-click="handleClick">
      <el-tab-pane label="模版" name="apiTemplate">
        <div style="min-height: 400px">
          <json-schema-editor class="schema" :value.sync="schema" lang="zh_CN" custom/>
        </div>
      </el-tab-pane>
      <el-tab-pane label="预览" name="preview">
        <div style="min-height: 400px">
          <pre>{{this.preview}}</pre>
        </div>
      </el-tab-pane>

    </el-tabs>
  </div>
</template>

<script>
  import {schemaToJson} from './common';
  import json5 from 'json5';

  export default {
    name: 'App',
    components: {},
    data() {
      return {
        schema:
          {
            "root": {
              "type": "object",
              "mock": {"mock": ""},
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
          this.preview = schemaToJson(json5.parse(JSON.stringify(this.schema)));
        }
      }
    }
  }
</script>
<style>

</style>
