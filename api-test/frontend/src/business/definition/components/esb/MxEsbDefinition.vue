<template>
  <div>
    <el-row>
      <el-col :span="spanNum" style="padding-bottom: 20px">
        <div style="border:1px #DCDFE6 solid; height: 100%;border-radius: 4px ;width: 100% ;">
          <el-form class="tcp" :model="request" :rules="rules" ref="request" :disabled="isReadOnly"
                   style="margin: 20px">

            <el-tabs v-model="activeName" class="request-tabs" @tab-click="tabClick">
              <!--query 参数-->
              <el-tab-pane name="parameters" v-if="isBodyShow">
                <template v-slot:label>
                  {{ $t('api_test.definition.request.req_param') }}
                  <ms-instructions-icon :content="$t('api_test.definition.request.esb_title')"/>
                </template>
                <esb-table :table-data="request.esbDataStruct" :show-options-button="true"
                           @saveTableData="validateEsbDataStruct" ref="esbTable"></esb-table>
              </el-tab-pane>
              <!--报文模版-->
              <el-tab-pane :label="$t('api_test.definition.request.message_template')" name="request">
                <div class="send-request">
                  <ms-code-edit mode="text" :read-only="isReadOnly" :data.sync="request.request"
                                :modes="['text', 'json', 'xml', 'html']" theme="eclipse"/>
                </div>
              </el-tab-pane>

              <!--其他设置-->
              <el-tab-pane :label="$t('api_test.definition.request.other_config')" name="other" class="other-config">
                <el-row>
                  <el-col :span="8">
                    <el-form-item label="TCPClient" prop="classname">
                      <el-select v-model="request.classname" style="width: 100%" size="small">
                        <el-option v-for="c in classes" :key="c" :label="c" :value="c"/>
                      </el-select>
                    </el-form-item>
                  </el-col>
                  <el-col :span="8">
                    <el-form-item :label="$t('api_test.request.tcp.connect')" prop="ctimeout">
                      <el-input-number v-model="request.ctimeout" controls-position="right" :min="0" size="small"/>
                    </el-form-item>
                  </el-col>
                  <el-col :span="8">
                    <el-form-item :label="$t('api_test.request.tcp.response')" prop="timeout">
                      <el-input-number v-model="request.timeout" controls-position="right" :min="0" size="small"/>
                    </el-form-item>
                  </el-col>
                </el-row>
                <el-row :gutter="10">
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

                  <el-col :span="6">
                    <el-form-item :label="$t('api_test.request.tcp.username')" prop="username">
                      <el-input v-model="request.username" maxlength="100" show-word-limit size="small"/>
                    </el-form-item>
                  </el-col>
                  <el-col :span="6">
                    <el-form-item :label="$t('api_test.request.tcp.password')" prop="password">
                      <el-input v-model="request.password" maxlength="30" show-word-limit show-password
                                autocomplete="new-password" size="small"/>
                    </el-form-item>
                  </el-col>
                </el-row>


                <el-row :gutter="10" style="margin-left: 30px">
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
                  <el-col :span="6">
                    <el-form-item label="Connect encoding">
                      <el-select v-model="request.connectEncoding" style="width: 100px" size="small">
                        <el-option v-for="item in connectEncodingArr" :key="item.key" :label="item.value"
                                   :value="item.key"/>
                      </el-select>
                    </el-form-item>
                  </el-col>
                </el-row>
              </el-tab-pane>

              <!-- 脚本步骤/断言步骤 -->
              <el-tab-pane :label="$t('api_test.definition.request.pre_operation')" name="preOperate" v-if="showScript">
          <span class="item-tabs" effect="dark" placement="top-start" slot="label">
            {{ $t('api_test.definition.request.pre_operation') }}
            <div class="el-step__icon is-text ms-api-col ms-header" v-if="request.preSize > 0">
              <div class="el-step__icon-inner">{{ request.preSize }}</div>
            </div>
          </span>
                <ms-jmx-step :request="request" :apiId="request.id" :response="response" :tab-type="'pre'"
                             ref="preStep"/>
              </el-tab-pane>
              <el-tab-pane :label="$t('api_test.definition.request.post_operation')" name="postOperate"
                           v-if="showScript">
            <span class="item-tabs" effect="dark" placement="top-start" slot="label">
            {{ $t('api_test.definition.request.post_operation') }}
            <div class="el-step__icon is-text ms-api-col ms-header" v-if="request.postSize > 0">
              <div class="el-step__icon-inner">{{ request.postSize }}</div>
            </div>
          </span>
                <ms-jmx-step :request="request" :apiId="request.id" :response="response" :tab-type="'post'"
                             ref="postStep" protocol="TCP"/>
              </el-tab-pane>
              <el-tab-pane :label="$t('api_test.definition.request.assertions_rule')" name="assertionsRule"
                           v-if="showScript">
            <span class="item-tabs" effect="dark" placement="top-start" slot="label">
            {{ $t('api_test.definition.request.assertions_rule') }}
            <div class="el-step__icon is-text ms-api-col ms-header" v-if="request.ruleSize > 0">
              <div class="el-step__icon-inner">{{ request.ruleSize }}</div>
            </div>
          </span>
                <ms-jmx-step :request="request" :apiId="request.id" :response="response" @reload="reloadBody"
                             :tab-type="'assertionsRule'" ref="assertionsRule" protocol="TCP"/>
              </el-tab-pane>

            </el-tabs>
          </el-form>
        </div>
      </el-col>

    </el-row>
  </div>
</template>

<script>

import TCPSampler from "@/business/definition/components/jmeter/components/sampler/tcp-sampler";
import MsCodeEdit from "metersphere-frontend/src/components/MsCodeEdit";
import Jsr233Processor from "@/business/automation/scenario/component/Jsr233Processor";
import Jsr233ProcessorContent from "@/business/automation/scenario/common/Jsr233ProcessorContent";
import ApiDefinitionStepButton from "@/business/definition/components/request/components/ApiDefinitionStepButton";
import MsInstructionsIcon from "metersphere-frontend/src/components/MsInstructionsIcon";
import EsbTable from "./EsbTable";
import MsJmxStep from "@/business/definition/components/step/JmxStep";
import {stepCompute, hisDataProcessing} from "@/business/definition/api-definition";
import {operationConfirm} from "metersphere-frontend/src/utils";

export default {
  name: "MxEsbDefinition",
  components: {
    MsCodeEdit, Jsr233Processor, Jsr233ProcessorContent, MsInstructionsIcon, ApiDefinitionStepButton,
    EsbTable, MsJmxStep
  },
  data() {
    return {
      spanNum: 21,
      activeName: "parameters",
      isReloadData: false,
      isBodyShow: true,
      classes: TCPSampler.CLASSES,
      connectEncodingArr: [
        {
          'key': 'UTF-8',
          'value': 'UTF-8',
        },
        {
          'key': 'GBK',
          'value': 'GBK',
        },
      ],
      rules: {
        classname: [{required: true, message: "请选择TCPClient", trigger: 'change'}],
        server: [{required: true, message: this.$t('api_test.request.tcp.server_cannot_be_empty'), trigger: 'blur'}],
        port: [{required: true, message: this.$t('commons.port_cannot_be_empty'), trigger: 'change'}],
      },
      previewReadOnly: true,
    }
  },
  props: {
    request: {},
    basisData: {},
    response: {},
    moduleOptions: Array,
    esbDataStruct: String,
    isReadOnly: {
      type: Boolean,
      default: false
    },
    showScript: {
      type: Boolean,
      default: true,
    },
    showPreScript: {
      type: Boolean,
      default: false,
    },
    referenced: {
      type: Boolean,
      default: false,
    },
  },
  watch: {
    'request.hashTree': {
      handler(v) {
        this.initStepSize(this.request.hashTree);
      },
      deep: true
    }
  },
  created() {
    this.spanNum = 24;
    if (this.request.esbDataStruct) {
      this.refreshEsbDataStruct(this.request.esbDataStruct);
    } else {
      this.request.esbDataStruct = [];
    }
    if (this.request.tcpPreProcessor && this.request.tcpPreProcessor.script) {
      this.request.hashTree.push(this.request.tcpPreProcessor);
      this.request.tcpPreProcessor = undefined;
    }
    if (!this.request.connectEncoding) {
      this.request.connectEncoding = "UTF-8";
    }
    if (this.request.hashTree) {
      this.initStepSize(this.request.hashTree);
      this.historicalDataProcessing(this.request.hashTree);
    }
  },
  methods: {
    tabClick() {
      if (this.activeName === 'preOperate') {
        this.$refs.preStep.filter();
      }
      if (this.activeName === 'postOperate') {
        this.$refs.postStep.filter();
      }
      if (this.activeName === 'assertionsRule') {
        this.$refs.assertionsRule.filter();
      }
    },
    historicalDataProcessing(array) {
      hisDataProcessing(array, this.request);
    },
    initStepSize(array) {
      stepCompute(array, this.request);
      this.reloadBody();
    },
    reloadBody() {
      // 解决修改请求头后 body 显示错位
      this.isBodyShow = false;
      this.$nextTick(() => {
        this.isBodyShow = true;
      });
    },
    refreshEsbDataStruct(esbDataStruct) {
      if (esbDataStruct && esbDataStruct.length > 0) {
        esbDataStruct.forEach(row => {
          row.status = "";
          if (row.children == null || row.children.length === 0) {
            row.children = [];
          } else if (row.children.length > 0) {
            this.refreshEsbDataStruct(row.children);
          }
        });
      }
    },
    checkEsbDataStructData(esbDataStruct) {
      let allCheckResult = true;
      if (esbDataStruct && esbDataStruct.length > 0) {
        for (let i = 0; i < esbDataStruct.length; i++) {
          let row = esbDataStruct[i];
          allCheckResult = this.$refs.esbTable.validateRowData(row);
          if (allCheckResult) {
            if (row.children != null && row.children.length > 0) {
              allCheckResult = this.checkEsbDataStructData(row.children);
              if (!allCheckResult) {
                return false;
              }
            }
          } else {
            return false;
          }
        }
      }
      return allCheckResult;
    },
    validateEsbDataStruct(esbDataStruct) {
      this.refreshEsbDataStruct(esbDataStruct);
      const result = this.checkEsbDataStructData(esbDataStruct);
      return result;
    },
    validate() {
      let validateResult = this.validateEsbDataStruct(this.request.esbDataStruct);
      if (validateResult) {
        this.$emit('callback');
      }
    },
  },
}

</script>

<style scoped>
.tip {
  padding: 3px 5px;
  font-size: 16px;
  border-radius: 4px;
  border-left: 4px solid #783887;
  margin: 0px 20px 0px;
}

.ms-header {
  background: #783887;
  color: white;
  height: 18px;
  border-radius: 42%;
}

.send-request {
  padding: 0px 0;
  height: 300px;
  border: 1px #DCDFE6 solid;
  border-radius: 4px;
  width: 100%;
}
</style>
