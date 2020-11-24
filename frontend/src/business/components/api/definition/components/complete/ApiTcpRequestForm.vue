<template xmlns:el-col="http://www.w3.org/1999/html">
  <!-- 操作按钮 -->
  <div style="background-color: white;">
    <el-row>
      <el-col>
        <!--操作按钮-->
        <div style="float: right;margin-right: 20px;margin-top: 20px">
          <el-button type="primary" size="small" @click="validateApi">{{$t('commons.save')}}</el-button>
          <el-button type="primary" size="small" @click="runTest">{{$t('commons.test')}}</el-button>
        </div>
      </el-col>
    </el-row>
    <!-- 基础信息 -->
    <p class="tip">{{$t('test_track.plan_view.base_info')}} </p>
    <br/>
    <el-row>
      <el-col>
        <ms-basis-api :moduleOptions="moduleOptions" :basisData="basisData" ref="basicForm" @callback="saveApi"/>
      </el-col>
    </el-row>

    <!-- 请求参数 -->
    <p class="tip">{{$t('api_test.definition.request.req_param')}} </p>
    <el-row>
      <el-col :span="21" style="padding-bottom: 50px">
        <el-form class="tcp" :model="request" :rules="rules" ref="request" label-width="auto" :disabled="isReadOnly">

          <el-form-item/>
          <!-- <el-form-item :label="$t('api_test.request.name')" prop="name">
             <el-input v-model="request.name" size="small" maxlength="300" show-word-limit/>
           </el-form-item>-->

          <el-form-item label="TCPClient" prop="classname">
            <el-select v-model="request.classname" style="width: 100%" size="small">
              <el-option v-for="c in classes" :key="c" :label="c" :value="c"/>
            </el-select>
          </el-form-item>

          <el-row :gutter="10">
            <el-col :span="16">
              <el-form-item :label="$t('api_test.request.tcp.server')" prop="server">
                <el-input v-model="request.server" maxlength="300" show-word-limit size="small"/>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item :label="$t('api_test.request.tcp.port')" prop="port" label-width="60px">
                <el-input-number v-model="request.port" controls-position="right" :min="0" :max="65535" size="small"/>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="10">
            <el-col :span="6">
              <el-form-item :label="$t('api_test.request.tcp.connect')" prop="ctimeout">
                <el-input-number v-model="request.ctimeout" controls-position="right" :min="0" size="small"/>
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item :label="$t('api_test.request.tcp.response')" prop="timeout">
                <el-input-number v-model="request.timeout" controls-position="right" :min="0" size="small"/>
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item :label="$t('api_test.request.tcp.so_linger')" prop="soLinger">
                <el-input v-model="request.soLinger" size="small"/>
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item :label="$t('api_test.request.tcp.eol_byte')" prop="eolByte">
                <el-input v-model="request.eolByte" size="small"/>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="10">
            <el-col :span="6">
              <el-form-item :label="$t('api_test.request.refer_to_environment')">
                <el-switch
                  v-model="request.useEnvironment"
                  @change="useEnvironmentChange">
                </el-switch>
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item :label="$t('api_test.request.tcp.re_use_connection')">
                <el-checkbox v-model="request.reUseConnection"/>
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item :label="$t('api_test.request.tcp.close_connection')">
                <el-checkbox v-model="request.closeConnection"/>
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item :label="$t('api_test.request.tcp.no_delay')">
                <el-checkbox v-model="request.nodelay"/>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="10">
            <el-col :span="12">
              <el-form-item :label="$t('api_test.request.tcp.username')" prop="username">
                <el-input v-model="request.username" maxlength="100" show-word-limit size="small"/>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item :label="$t('api_test.request.tcp.password')" prop="password">
                <el-input v-model="request.password" maxlength="30" show-word-limit show-password
                          autocomplete="new-password" size="small"/>
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item :label="$t('api_test.request.tcp.request')" prop="request">
            <div class="send-request">
              <ms-code-edit mode="text" :read-only="isReadOnly" :data.sync="request.request"
                            :modes="['text', 'json', 'xml', 'html']" theme="eclipse"/>
            </div>
          </el-form-item>

        </el-form>

        <div v-for="row in request.hashTree" :key="row.id" v-loading="isReloadData" style="margin-left: 30px">
          <!-- 前置脚本 -->
          <ms-jsr233-processor v-if="row.label ==='JSR223 PreProcessor'" @remove="remove" :is-read-only="false" :title="$t('api_test.definition.request.pre_script')" style-type="warning"
                               :jsr223-processor="row"/>
          <!--后置脚本-->
          <ms-jsr233-processor v-if="row.label ==='JSR223 PostProcessor'" @remove="remove" :is-read-only="false" :title="$t('api_test.definition.request.post_script')" style-type="success"
                               :jsr223-processor="row"/>
          <!--断言规则-->
          <ms-api-assertions v-if="row.type==='Assertions'" @remove="remove" :is-read-only="isReadOnly" :assertions="row"/>
          <!--提取规则-->
          <ms-api-extract :is-read-only="isReadOnly" @remove="remove" v-if="row.type==='Extract'" :extract="row"/>

        </div>
      </el-col>

      <el-col :span="3" class="ms-left-cell">

        <el-button class="ms-left-buttion" size="small" type="warning" @click="addPre" plain>+{{$t('api_test.definition.request.pre_script')}}</el-button>
        <br/>
        <el-button class="ms-left-buttion" size="small" type="success" @click="addPost" plain>+{{$t('api_test.definition.request.post_script')}}</el-button>
        <br/>
        <el-button class="ms-left-buttion" size="small" type="danger" @click="addAssertions" plain>+{{$t('api_test.definition.request.assertions_rule')}}</el-button>
        <br/>
        <el-button class="ms-left-buttion" size="small" type="info" @click="addExtract" plain>+{{$t('api_test.definition.request.extract_param')}}</el-button>
      </el-col>
    </el-row>
  </div>


</template>

<script>
  import MsApiAssertions from "../assertion/ApiAssertions";
  import MsApiExtract from "../extract/ApiExtract";
  import MsJsr233Processor from "../processor/Jsr233Processor";
  import MsCodeEdit from "@/business/components/common/components/MsCodeEdit";
  import TCPSampler from "../jmeter/components/sampler/tcp-sampler";
  import {createComponent} from "../jmeter/components";
  import {Assertions, Extract} from "../../model/ApiTestModel";
  import {API_STATUS} from "../../model/JsonData";
  import MsBasisApi from "./BasisApi";

  export default {
    name: "MsApiTcpRequestForm",
    components: {MsCodeEdit, MsJsr233Processor, MsApiExtract, MsApiAssertions, MsBasisApi},
    props: {
      request: {},
      basisData: {},
      currentProject: {},
      maintainerOptions: Array,
      moduleOptions: Array,
      isReadOnly: {
        type: Boolean,
        default: false
      }
    },
    data() {
      return {
        activeName: "assertions",
        classes: TCPSampler.CLASSES,
        rules: {
          name: [
            {required: true, message: this.$t('test_track.case.input_name'), trigger: 'blur'},
            {max: 50, message: this.$t('test_track.length_less_than') + '50', trigger: 'blur'}
          ],
        },
        isReloadData: false,
        options: API_STATUS,
      }
    },

    methods: {
      useEnvironmentChange(value) {
        if (value && !this.request.environment) {
          this.$error(this.$t('api_test.request.please_add_environment_to_scenario'), 2000);
          this.request.useEnvironment = false;
        }
        this.$refs["request"].clearValidate();
      },
      addPre() {
        let jsr223PreProcessor = createComponent("JSR223PreProcessor");
        this.request.hashTree.push(jsr223PreProcessor);
        this.reload();
      },
      addPost() {
        let jsr223PostProcessor = createComponent("JSR223PostProcessor");
        this.request.hashTree.push(jsr223PostProcessor);
        this.reload();
      },
      addAssertions() {
        let assertions = new Assertions();
        this.request.hashTree.push(assertions);
        this.reload();
      },
      addExtract() {
        let jsonPostProcessor = new Extract();
        this.request.hashTree.push(jsonPostProcessor);
        this.reload();
      },
      remove(row) {
        let index = this.request.hashTree.indexOf(row);
        this.request.hashTree.splice(index, 1);
        this.reload();
      },
      reload() {
        this.isReloadData = true
        this.$nextTick(() => {
          this.isReloadData = false
        })
      },
      setParameter() {
        this.request.modulePath = this.getPath(this.basisData.moduleId);
        this.request.useEnvironment = undefined;
      },
      getPath(id) {
        if (id === null) {
          return null;
        }
        let path = this.moduleOptions.filter(function (item) {
          return item.id === id ? item.path : "";
        });
        return path[0].path;
      },
      validateApi() {
        this.basisData.method = "TCP";
        if (this.currentProject === null) {
          this.$error(this.$t('api_test.select_project'), 2000);
          return;
        }
        this.$refs['basicForm'].validate();
      },
      saveApi() {
        this.$emit('saveApi', this.request);
      },
      runTest() {
        alert(444);
      }
    },
  }
</script>

<style scoped>
  .tcp >>> .el-input-number {
    width: 100%;
  }

  .send-request {
    padding: 0px 0;
    height: 300px;
    border: 1px #DCDFE6 solid;
    border-radius: 4px;
    width: 100%;
  }

  .ms-left-cell {
    margin-top: 40px;
  }

  .ms-left-buttion {
    margin: 6px 0px 8px 30px;
  }

  .ms-query {
    background: #7F7F7F;
    color: white;
    height: 18px;
    border-radius: 42%;
  }

  .ms-header {
    background: #783887;
    color: white;
    height: 18px;
    border-radius: 42%;
  }

  /deep/ .el-form-item {
    margin-bottom: 15px;
  }

  .tip {
    padding: 3px 5px;
    font-size: 16px;
    border-radius: 4px;
    border-left: 4px solid #783887;
    margin: 0px 20px 0px;
  }
</style>
