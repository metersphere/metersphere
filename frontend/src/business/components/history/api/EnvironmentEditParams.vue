<template xmlns:v-slot="http://www.w3.org/1999/XSL/Transform">
  <!-- 全局前后置脚本 -->
  <div style="border:1px #DCDFE6 solid;" v-if="!loading">
    <el-tabs v-model="activeName" class="request-tabs">
      <!--全局前置脚本-->
      <el-tab-pane :label="$t('api_test.definition.request.all_pre_script')" name="allPreScript" v-if="showPre">


        <div v-if="(request.script.globalScriptConfigRaw1 || request.script.globalScriptConfigRaw2)">
          <div v-if="request.script.globalScriptConfig">
            <div v-if="request.script.globalScriptConfig.filterRequestPreScriptRaw1 || request.script.globalScriptConfig.filterRequestPreScriptRaw2">
              <el-radio-group size="mini">
                <el-radio-button :label="$t('api_test.script.pre_script_filter_request_type')"/>
              </el-radio-group>
              <div v-html="getDiff(request.script.globalScriptConfig.filterRequestPreScriptRaw2, request.script.globalScriptConfig.filterRequestPreScriptRaw1)"></div>
            </div>
            <div v-if="request.script.globalScriptConfig.isPreScriptExecAfterPrivateScriptRaw1 || request.script.globalScriptConfig.isPreScriptExecAfterPrivateScriptRaw2">
              <el-radio-group size="mini">
                <el-radio-button :label="$t('api_test.script.pre_script_exec_order')"/>
              </el-radio-group>
              <div v-html="getDiff(request.script.globalScriptConfig.isPreScriptExecAfterPrivateScriptRaw2, request.script.globalScriptConfig.isPreScriptExecAfterPrivateScriptRaw1)"></div>
            </div>
            <div v-if="request.script.globalScriptConfig.connScenarioPreScriptRaw1 || request.script.globalScriptConfig.connScenarioPreScriptRaw2">
              <el-radio-group size="mini">
                <el-radio-button :label="$t('api_test.script.pre_link_scenario_result')"/>
              </el-radio-group>
              <div v-html="getDiff(request.script.globalScriptConfig.connScenarioPreScriptRaw2, request.script.globalScriptConfig.connScenarioPreScriptRaw1)"></div>
            </div>
          </div>
        </div>


        <div v-if="request.script.preProcessorRaw1 || request.script.preProcessorRaw2">
          <el-radio-group size="mini">
            <el-radio-button :label="$t('api_test.script.execute_before_step')" />
          </el-radio-group>
          <div v-html="getDiff(request.script.preProcessorRaw2, request.script.preProcessorRaw1)"></div>
        </div>

        <div v-if="request.script.preStepProcessorRaw1 || request.script.preStepProcessorRaw2">
          <el-radio-group size="mini">
            <el-radio-button :label="$t('api_test.script.execute_before_all_steps')" />
          </el-radio-group>
          <div v-html="getDiff(request.script.preStepProcessorRaw2, request.script.preStepProcessorRaw1)"></div>
        </div>
      </el-tab-pane>

      <!--全局后置脚本-->
      <el-tab-pane :label="$t('api_test.definition.request.all_post_script')" name="allPostScript" v-if="showPost">

        <div v-if="(request.script.globalScriptConfigRaw1 || request.script.globalScriptConfigRaw2)">
          <div v-if="request.script.globalScriptConfig">
            <div v-if="request.script.globalScriptConfig.filterRequestPostScriptRaw1 || request.script.globalScriptConfig.filterRequestPostScriptRaw2">
              <el-radio-group size="mini">
                <el-radio-button :label="$t('api_test.script.post_script_filter_request_type')"/>
              </el-radio-group>
              <div v-html="getDiff(request.script.globalScriptConfig.filterRequestPostScriptRaw2, request.script.globalScriptConfig.filterRequestPostScriptRaw1)"></div>
            </div>
            <div v-if="request.script.globalScriptConfig.isPostScriptExecAfterPrivateScriptRaw1 || request.script.globalScriptConfig.isPostScriptExecAfterPrivateScriptRaw2">
              <el-radio-group size="mini">
                <el-radio-button :label="$t('api_test.script.post_script_exec_order')"/>
              </el-radio-group>
              <div v-html="getDiff(request.script.globalScriptConfig.isPostScriptExecAfterPrivateScriptRaw2, request.script.globalScriptConfig.isPostScriptExecAfterPrivateScriptRaw1)"></div>
            </div>
            <div v-if="request.script.globalScriptConfig.connScenarioPostScriptRaw1 || request.script.globalScriptConfig.connScenarioPostScriptRaw2">
              <el-radio-group size="mini">
                <el-radio-button :label="$t('api_test.script.post_link_scenario_result')"/>
              </el-radio-group>
              <div v-html="getDiff(request.script.globalScriptConfig.connScenarioPostScriptRaw2, request.script.globalScriptConfig.connScenarioPostScriptRaw1)"></div>
            </div>
          </div>
        </div>

        <div v-if="request.script.postProcessorRaw1 || request.script.postProcessorRaw2">
          <el-radio-group size="mini">
            <el-radio-button :label="$t('api_test.script.execute_post_step')"/>
          </el-radio-group>
          <div v-html="getDiff(request.script.postProcessorRaw2, request.script.postProcessorRaw1)"></div>
        </div>

        <div v-if="request.script.postStepProcessorRaw1 || request.script.postStepProcessorRaw2">
          <el-radio-group size="mini">
            <el-radio-button :label="$t('api_test.script.execute_post_all_steps')"/>
          </el-radio-group>
          <div v-html="getDiff(request.script.postStepProcessorRaw2, request.script.postStepProcessorRaw1)"></div>
        </div>
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

        showPre: false,
        showPost: false,
      }
    },
    methods: {
      active() {
        if (this.request.script && (this.request.script.postProcessorRaw1 || this.request.script.postProcessorRaw2
          || this.request.script.postStepProcessorRaw1 || this.request.script.postStepProcessorRaw2)
          || ((this.request.script.globalScriptConfigRaw1 || this.request.script.globalScriptConfigRaw2) &&
            (this.request.script.globalScriptConfig.filterRequestPostScriptRaw1 || this.request.script.globalScriptConfig.filterRequestPostScriptRaw2
              || this.request.script.globalScriptConfig.isPostScriptExecAfterPrivateScriptRaw1 || this.request.script.globalScriptConfig.isPostScriptExecAfterPrivateScriptRaw2
              || this.request.script.globalScriptConfig.connScenarioPostScriptRaw1 || this.request.script.globalScriptConfig.connScenarioPostScriptRaw2))){
          this.showPost = true;
          this.activeName = 'allPostScript';
        }else {
          this.showPost = false;
        }
        if (this.request.script && (this.request.script.preProcessorRaw1 || this.request.script.preProcessorRaw2
          || this.request.script.preStepProcessorRaw1 || this.request.script.preStepProcessorRaw2)
          || ((this.request.script.globalScriptConfigRaw1 || this.request.script.globalScriptConfigRaw2) &&
            (this.request.script.globalScriptConfig.filterRequestPreScriptRaw1 || this.request.script.globalScriptConfig.filterRequestPreScriptRaw2
              || this.request.script.globalScriptConfig.isPreScriptExecAfterPrivateScriptRaw1 || this.request.script.globalScriptConfig.isPreScriptExecAfterPrivateScriptRaw2
              || this.request.script.globalScriptConfig.connScenarioPreScriptRaw1 || this.request.script.globalScriptConfig.connScenarioPreScriptRaw2))) {
          this.showPre = true;
          this.activeName = 'allPreScript';
        }else {
          this.showPre = false;
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
