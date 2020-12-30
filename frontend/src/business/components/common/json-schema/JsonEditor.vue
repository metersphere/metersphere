<template>
  <div id="app">
    <el-tabs v-model="activeName" @tab-click="handleClick">
      <el-tab-pane label="模版" name="apiTemplate">
        <div>
          <json-schema-editor :schema.sync="schema" :is-mock="true"></json-schema-editor>
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

  export default {
    name: 'App',
    components: {},
    data() {
      return {
        schema: {
          type: 'object',
          title: 'title',
          properties: {
            field_1: {
              type: 'string'
            }
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
          this.preview = schemaToJson(this.schema);
        }
      }
    }
  }
</script>
<style>

</style>
