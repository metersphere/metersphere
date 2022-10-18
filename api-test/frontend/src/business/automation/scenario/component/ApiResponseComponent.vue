<template>
  <el-card class="api-component">
    <div class="header" @click="active">
      <i class="icon el-icon-arrow-right" :class="{'is-active': isActive}"/>
      <ms-request-metric :response="response"/>
    </div>

    <el-collapse-transition>
      <div v-if="isActive">
        <el-divider></el-divider>
        <ms-request-result-tail :currentProtocol="currentProtocol" :show-metric="false" :response="response"/>
      </div>
    </el-collapse-transition>
  </el-card>

</template>

<script>
import ApiBaseComponent from "../common/ApiBaseComponent";
import MsRequestResultTail from "../../../definition/components/response/RequestResultTail";
import ElCollapseTransition from "element-ui/src/transitions/collapse-transition";
import MsRequestMetric from "../../../definition/components/response/RequestMetric";
import {getApiReportDetail} from "@/api/definition-report";

export default {
  name: "ApiResponseComponent",
  components: {ElCollapseTransition, MsRequestResultTail, ApiBaseComponent, MsRequestMetric},
  props: {apiItem: {}, result: {}, currentProtocol: String, apiActive: {type: Boolean, default: false}},
  data() {
    return {
      isActive: false,
      response: {responseResult: {}}
    }
  },
  created() {
    if (!this.result || !this.result.responseResult) {
      this.getExecResult();
    } else {
      this.response = this.result;
    }
    if (this.apiActive) {
      this.isActive = false
    }
  },
  watch: {
    result() {
      if (this.result && this.result.responseResult) {
        this.response = this.result;
      } else {
        this.getExecResult();
      }
      this.isActive = false;
    },
    apiItem() {
      this.getExecResult();
    }
  },
  methods: {
    getExecResult() {
      // 执行结果信息
      if (this.apiItem && this.apiItem.lastResultId) {
        getApiReportDetail(this.apiItem.lastResultId).then(response => {
          if (response.data && response.data.content) {
            try {
              let data = JSON.parse(response.data.content);
              this.response = data;
              this.apiItem.responseData = data;
              this.isActive = true;
            } catch (error) {
              this.isActive = true;
            }
          }
        });
      } else {
        this.response = null;
      }
    },
    active() {
      this.isActive = !this.isActive;
    }
  }
}
</script>

<style scoped>

.header {
  height: 30px;
  line-height: 30px;
}

:deep(.el-card__body) {
  padding: 15px;
}

.icon.is-active {
  transform: rotate(90deg);
}

.el-icon-arrow-right {
  float: left;
  display: block;
  height: 30px;
  line-height: 30px;
}

.metric-container {
  margin-left: 25px;
}

</style>
