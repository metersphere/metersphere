<template xmlns:v-slot="http://www.w3.org/1999/XSL/Transform">
  <!-- 全局前后置脚本 -->
  <div style="border:1px #DCDFE6 solid;" v-if="!loading">
    <el-tabs v-model="activeName" class="request-tabs">
      <!--全局前置脚本-->
      <el-tab-pane :label="$t('api_test.definition.request.all_pre_script')" name="allPreScript" v-if="request.script
      && (request.script.preProcessor_raw_1 || request.script.preProcessor_raw_2 || request.script.preStepProcessor_raw_1 || request.script.preStepProcessor_raw_2)">

          <div v-if="request.script.preProcessor_raw_1 || request.script.preProcessor_raw_2">
            <el-radio-group v-model="preProcessor" size="mini">
              <el-radio-button :label="$t('api_test.script.execute_before_step')" />
            </el-radio-group>
            <div v-html="getDiff(request.script.preProcessor_raw_2, request.script.preProcessor_raw_1)"></div>
          </div>

          <div v-if="request.script.preStepProcessor_raw_1 || request.script.preStepProcessor_raw_2">
            <el-radio-group v-model="preStepProcessor" size="mini">
              <el-radio-button :label="$t('api_test.script.execute_before_all_steps')" />
            </el-radio-group>
            <div v-html="getDiff(request.script.preStepProcessor_raw_2, request.script.preStepProcessor_raw_1)"></div>
          </div>
      </el-tab-pane>

      <!--全局后置脚本-->
      <el-tab-pane :label="$t('api_test.definition.request.all_post_script')" name="allPostScript" v-if="request.script
      && (request.script.postProcessor_raw_1 || request.script.postProcessor_raw_2 || request.script.postStepProcessor_raw_1 || request.script.postStepProcessor_raw_2)">

          <div v-if="request.script.postProcessor_raw_1 || request.script.postProcessor_raw_2">
            <el-radio-group v-model="postProcessor" size="mini">
              <el-radio-button :label="$t('api_test.script.execute_post_step')"/>
            </el-radio-group>
            <div v-html="getDiff(request.script.postProcessor_raw_2, request.script.postProcessor_raw_1)"></div>
          </div>

          <div v-if="request.script.postStepProcessor_raw_1 || request.script.postStepProcessor_raw_2">
            <el-radio-group v-model="postStepProcessor" size="mini">
              <el-radio-button :label="$t('api_test.script.execute_post_all_steps')"/>
            </el-radio-group>
            <div v-html="getDiff(request.script.postStepProcessor_raw_2, request.script.postStepProcessor_raw_1)"></div>
          </div>
      </el-tab-pane>

      <!--通用全局脚本配置-->
      <el-tab-pane :label="$t('api_test.script.global_script_config')" name="globalScriptConfig" v-if="request.script
      && (request.script.globalScriptConfig_raw_1 || request.script.globalScriptConfig_raw_2)">
        <template v-slot:default="scope">
          <div v-html="getDiff(request.script.globalScriptConfig_raw_2, request.script.globalScriptConfig_raw_1)"></div>
        </template>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
  import MsJsonCodeEdit from "./json-view/ComparedEditor";

  const jsondiffpatch = require('jsondiffpatch');
  const formattersHtml = jsondiffpatch.formatters.html;

  export default {
    name: "EnvironmentEditParams",
    components: {MsJsonCodeEdit},
    props: {
      method: String,
      request: {},
      type: String,
    },
    created() {
      this.active();
    },
    data() {
      return {
        activeName: "",
        preProcessor: "preProcessor",
        postProcessor: "preProcessor",
        preStepProcessor: "preStepProcessor",
        postStepProcessor: "preStepProcessor",
        loading: false,
      }
    },
    methods: {
      active() {
        if (this.request.script && (this.request.script.preProcessor_raw_1 || this.request.script.preProcessor_raw_2
            || this.request.script.preStepProcessor_raw_1 || this.request.script.preStepProcessor_raw_2)) {
          this.activeName = "allPreScript";
        }
        else if (this.request.script
          && (this.request.script.postProcessor_raw_1 || this.request.script.postProcessor_raw_2
            || this.request.script.postStepProcessor_raw_1 || this.request.script.postStepProcessor_raw_2)) {
          this.activeName = "allPostScript";
        }
        else if (this.request.script && (this.request.script.globalScriptConfig_raw_1 || this.request.script.globalScriptConfig_raw_2)) {
          this.activeName = "globalScriptConfig";
        }
      },
      getDiff(v1, v2) {
        let delta = jsondiffpatch.diff(v1 , v2);
        return formattersHtml.format(delta, v1);
      },
    }
  }
</script>

<style scoped>
  @import "~jsondiffpatch/dist/formatters-styles/html.css";
  @import "~jsondiffpatch/dist/formatters-styles/annotated.css";
</style>
