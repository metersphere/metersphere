<template>
  <api-base-component
    @copy="copyRow"
    @remove="remove"
    @active="active"
    :data="jsr223Processor"
    :draggable="draggable"
    :color="color"
    :is-max="isMax"
    :show-btn="showBtn"
    :show-version="showVersion"
    :background-color="backgroundColor"
    :if-from-variable-advance="ifFromVariableAdvance"
    :title="title" v-loading="loading">
    <!--自定义脚本-->
    <legend style="width: 100%" v-if="request && this.request.type === 'JSR223Processor'">
      <p class="ms-tip">{{ $t('api_test.definition.request.req_param') }} </p>
      <el-tabs v-model="activeName" class="request-tabs" @tab-click="tabClick">
        <!-- 请求头-->
        <el-tab-pane label="脚本内容" name="baseScript">
          <jsr233-processor-content
            :jsr223-processor="jsr223Processor"
            :is-pre-processor="isPreProcessor"
            :node="node"
            :protocol="protocol"
            :is-read-only="this.jsr223Processor.disabled"/>
        </el-tab-pane>
        <!-- 脚本步骤/断言步骤 -->
        <el-tab-pane :label="$t('api_test.definition.request.pre_operation')" name="preOperate">
          <span class="item-tabs" effect="dark" placement="top-start" slot="label" :key="request.preSize">
            {{ $t('api_test.definition.request.pre_operation') }}
            <div class="el-step__icon is-text ms-api-col ms-header" v-if="request.preSize > 0">
              <div class="el-step__icon-inner">{{ request.preSize }}</div>
            </div>
          </span>
          <ms-jmx-step
            :request="request"
            :apiId="request.id"
            :response="response"
            :tab-type="'pre'"
            ref="preStep"
            v-if="activeName === 'preOperate'"
          />
        </el-tab-pane>
        <el-tab-pane :label="$t('api_test.definition.request.post_operation')" name="postOperate">
            <span class="item-tabs" effect="dark" placement="top-start" slot="label">
            {{ $t('api_test.definition.request.post_operation') }}
            <div class="el-step__icon is-text ms-api-col ms-header" v-if="request.postSize > 0">
              <div class="el-step__icon-inner" :key="request.postSize">{{ request.postSize }}</div>
            </div>
          </span>
          <ms-jmx-step
            :request="request"
            :apiId="request.id"
            :response="response"
            :tab-type="'post'"
            ref="postStep"
            v-if="activeName === 'postOperate'"
          />
        </el-tab-pane>
        <el-tab-pane :label="$t('api_test.definition.request.assertions_rule')" name="assertionsRule">
            <span class="item-tabs" effect="dark" placement="top-start" slot="label">
            {{ $t('api_test.definition.request.assertions_rule') }}
            <div class="el-step__icon is-text ms-api-col ms-header" v-if="request.ruleSize > 0">
              <div class="el-step__icon-inner" :key="request.ruleSize">{{ request.ruleSize }}</div>
            </div>
          </span>
          <ms-jmx-step
            :request="request"
            :apiId="request.id"
            :response="response"
            :tab-type="'assertionsRule'"
            ref="assertionsRule"
            v-if="activeName === 'assertionsRule'"/>
        </el-tab-pane>
      </el-tabs>
    </legend>

    <!-- 前后置脚本 -->
    <legend style="width: 100%" v-else>
      <jsr233-processor-content
        :jsr223-processor="jsr223Processor"
        :is-pre-processor="isPreProcessor"
        :node="node"
        :protocol="protocol"
        :is-read-only="this.jsr223Processor.disabled"/>
    </legend>
    <template v-slot:debugStepCode>
         <span v-if="jsr223Processor.testing" class="ms-test-running">
           <i class="el-icon-loading" style="font-size: 16px"/>
           {{ $t('commons.testing') }}
         </span>
      <span class="ms-step-debug-code" :class="jsr223Processor.requestResult[0].success && reqSuccess?'ms-req-success':'ms-req-error'"
            v-if="!loading &&!jsr223Processor.testing && jsr223Processor.debug && jsr223Processor.requestResult[0] && jsr223Processor.requestResult[0].responseResult">
          {{ jsr223Processor.requestResult[0].success && reqSuccess ? 'success' : 'error' }}
        </span>
    </template>

    <!-- 执行结果内容 -->
    <template v-slot:result>
      <div v-loading="loading" v-if="jsr223Processor && jsr223Processor.requestResult && jsr223Processor.requestResult.length > 0">
        <p class="tip">{{ $t('api_test.definition.request.res_param') }} </p>
        <el-tabs v-model="jsr223Processor.activeName" closable class="ms-tabs" v-if="jsr223Processor.requestResult && jsr223Processor.requestResult.length > 1">
          <el-tab-pane v-for="(item,i) in jsr223Processor.requestResult" :label="'循环'+(i+1)" :key="i" style="margin-bottom: 5px">
            <api-response-component
              :currentProtocol="jsr223Processor.protocol"
              :apiActive="true"
              :result="item"/>
          </el-tab-pane>
        </el-tabs>
        <api-response-component
          :currentProtocol="'HTTP'"
          :apiActive="true"
          :result="jsr223Processor.requestResult[0]"
          v-else/>
      </div>
    </template>
  </api-base-component>
</template>

<script>
import MsCodeEdit from "../../../../common/components/MsCodeEdit";
import MsInstructionsIcon from "../../../../common/components/MsInstructionsIcon";
import MsDropdown from "../../../../common/components/MsDropdown";
import ApiBaseComponent from "../common/ApiBaseComponent";
import Jsr233ProcessorContent from "../common/Jsr233ProcessorContent";
import ApiResponseComponent from "./ApiResponseComponent";
import {stepCompute, hisDataProcessing} from "@/business/components/api/definition/api-definition";

export default {
  name: "MsJsr233Processor",
  components: {
    Jsr233ProcessorContent,
    ApiBaseComponent,
    MsDropdown,
    MsInstructionsIcon,
    MsCodeEdit,
    ApiResponseComponent,
    MsJmxStep: () => import( "@/business/components/api/definition/components/step/JmxStep")
  },
  props: {
    request: {},
    message: String,
    draggable: {
      type: Boolean,
      default: false,
    },
    isMax: {
      type: Boolean,
      default: false,
    },
    showBtn: {
      type: Boolean,
      default: true,
    },
    showVersion: {
      type: Boolean,
      default: true,
    },
    protocol: String,
    isReadOnly: {
      type: Boolean,
      default:
        false
    },
    jsr223Processor: {
      type: Object,
    },
    isPreProcessor: {
      type: Boolean,
      default:
        false
    },
    title: String,
    color: String,
    backgroundColor: String,
    node: {},
    ifFromVariableAdvance: {
      type: Boolean,
      default: false,
    },
  },
  created() {
    if (this.request && this.request.type === 'JSR223Processor' && this.request.hashTree) {
      this.initStepSize(this.request.hashTree);
      this.historicalDataProcessing(this.request.hashTree);
    }
  },
  watch: {
    message() {
      this.forStatus();
      this.reload();
    },
    'request.hashTree': {
      handler(v) {
        this.initStepSize(this.request.hashTree);
      },
      deep: true
    }
  },
  data() {
    return {
      loading: false,
      reqSuccess: true,
      response: {},
      activeName: "baseScript"
    }
  },
  methods: {
    historicalDataProcessing(array) {
      hisDataProcessing(array, this.request);
    },
    initStepSize(array) {
      stepCompute(array, this.request);
      this.reload();
    },

    tabClick() {
      this.$nextTick(() => {
        setTimeout(() => {
          this.filter(this.activeName);
        });
      });
    },
    filter(activeName) {
      if (activeName === 'preOperate' && this.$refs.preStep) {
        this.$refs.preStep.filter();
      }
      if (activeName === 'postOperate' && this.$refs.postStep) {
        this.$refs.postStep.filter();
      }
      if (activeName === 'assertionsRule' && this.$refs.assertionsRule) {
        this.$refs.assertionsRule.filter();
      }
    },
    forStatus() {
      if (this.jsr223Processor && this.jsr223Processor.result && this.jsr223Processor.result.length > 0) {
        this.jsr223Processor.result.forEach(item => {
          item.requestResult.forEach(req => {
            if (!req.success) {
              this.reqSuccess = req.success;
            }
          })
        })
      } else if (this.jsr223Processor && this.jsr223Processor.requestResult && this.jsr223Processor.requestResult.length > 1) {
        this.jsr223Processor.requestResult.forEach(item => {
          if (!item.success) {
            this.reqSuccess = item.success;
            if (this.node && this.node.parent && this.node.parent.data) {
              this.node.parent.data.code = 'error';
            }
          }
        })
      }
      if (this.jsr223Processor.requestResult && this.jsr223Processor.requestResult.length > 0) {
        this.response = this.jsr223Processor.requestResult[0]
      }
    },
    remove() {
      this.$emit('remove', this.jsr223Processor, this.node);
    },
    copyRow() {
      this.$emit('copyRow', this.jsr223Processor, this.node);
    },
    reload() {
      this.loading = true
      this.$nextTick(() => {
        this.loading = false
      })
    },
    active() {
      this.jsr223Processor.active = !this.jsr223Processor.active;
      this.reload();
    },
  }
}
</script>

<style scoped>
/deep/ .el-divider {
  margin-bottom: 10px;
}

.ms-req-error {
  color: #F56C6C;
}

.ms-test-running {
  color: #783887;
}

.ms-req-success {
  color: #67C23A;
}

.ms-header {
  background: #783887;
  color: white;
  height: 18px;
  font-size: xx-small;
  border-radius: 50%;
}

.request-tabs {
  margin: 0px 5px 0px;
  min-height: 200px;
  width: 100%;
}

.ms-tip {
  padding: 3px 5px;
  font-size: 16px;
  border-radius: 0;
  border-left: 4px solid #783887;
  margin: 5px 5px 0px 5px;
}
</style>
