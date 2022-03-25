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

    <legend style="width: 100%">
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

export default {
  name: "MsJsr233Processor",
  components: {Jsr233ProcessorContent, ApiBaseComponent, MsDropdown, MsInstructionsIcon, MsCodeEdit, ApiResponseComponent},
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
    protocol:String,
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
  watch: {
    message() {
      this.forStatus();
      this.reload();
    },
  },
  data() {
    return {
      loading: false,
      reqSuccess: true
    }
  },
  methods: {
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
  color: #6D317C;
}

.ms-req-success {
  color: #67C23A;
}

</style>
