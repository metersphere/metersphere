<template>
  <div class="metric-container">
    <el-row type="flex">
      <el-col>
        <div style="font-size: 14px; color: #aaaaaa; float: left">{{ $t('api_report.response_code') }} :</div>
        <el-tooltip v-if="responseResult.responseCode" :content="responseResult.responseCode" placement="top">
          <div
            v-if="
              response.attachInfoMap &&
              response.attachInfoMap.FAKE_ERROR &&
              response.attachInfoMap.status === 'FAKE_ERROR'
            "
            class="node-title"
            :class="'ms-req-error-report-result'">
            {{ responseResult && responseResult.responseCode ? responseResult.responseCode : '0' }}
          </div>
          <div v-else class="node-title" :class="response && response.success ? 'ms-req-success' : 'ms-req-error'">
            {{ responseResult && responseResult.responseCode ? responseResult.responseCode : '0' }}
          </div>
        </el-tooltip>
        <div v-else class="node-title" :class="response && response.success ? 'ms-req-success' : 'ms-req-error'">
          {{ responseResult && responseResult.responseCode ? responseResult.responseCode : '0' }}
        </div>
        <div v-if="response && response.attachInfoMap && response.attachInfoMap.FAKE_ERROR">
          <div
            class="node-title ms-req-error-report-result"
            v-if="response.attachInfoMap.status === 'FAKE_ERROR'"
            style="margin-left: 0px; padding-left: 0px">
            {{ response.attachInfoMap.FAKE_ERROR }}
          </div>
          <div
            class="node-title ms-req-success"
            v-else-if="response.success"
            style="margin-left: 0px; padding-left: 0px">
            {{ response.attachInfoMap.FAKE_ERROR }}
          </div>
          <div class="node-title ms-req-error" v-else style="margin-left: 0px; padding-left: 0px">
            {{ response.attachInfoMap.FAKE_ERROR }}
          </div>
        </div>
      </el-col>
      <el-col>
        <div style="font-size: 14px; color: #aaaaaa; float: left">{{ $t('api_report.response_time') }} :</div>
        <div style="font-size: 14px; color: #61c550; margin-top: 2px; margin-left: 10px; float: left">
          {{ responseResult && responseResult.responseTime ? responseResult.responseTime : 0 }}
          ms
        </div>
      </el-col>
      <el-col>
        <div style="font-size: 14px; color: #aaaaaa; float: left">{{ $t('api_report.response_size') }} :</div>
        <div style="font-size: 14px; color: #61c550; margin-top: 2px; margin-left: 10px; float: left">
          {{ responseResult && responseResult.responseSize ? responseResult.responseSize : 0 }}
          bytes
        </div>
      </el-col>
    </el-row>
    <el-row type="flex" style="margin-top: 5px">
      <el-col v-if="response && response.poolName">
        <div style="font-size: 14px; color: #aaaaaa; float: left">
          <span> {{ $t('load_test.select_resource_pool') + ':' }} </span>
        </div>
        <div style="font-size: 14px; color: #61c550; margin-left: 10px; float: left">
          {{ response.poolName }}
        </div>
      </el-col>
      <el-col type="flex" v-if="response && response.envName">
        <div style="font-size: 14px; color: #aaaaaa; float: left">
          <span> {{ $t('commons.environment') + ':' }} </span>
        </div>
        <div style="font-size: 14px; color: #61c550; margin-left: 10px; float: left">
          {{ response.envName }}
        </div>
      </el-col>
      <el-col></el-col>
    </el-row>
  </div>
</template>

<script>
export default {
  name: 'MsRequestMetric',

  props: {
    response: {
      type: Object,
      default() {
        return {};
      },
    },
  },
  computed: {
    responseResult() {
      return this.response && this.response.responseResult ? this.response.responseResult : {};
    },
    error() {
      return this.response && this.response.responseCode && this.response.responseCode >= 400;
    },
  },
};
</script>

<style scoped>
.metric-container {
  padding-bottom: 10px;
}

.node-title {
  /*width: 150px;*/
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1 1 auto;
  padding: 0px 5px;
  overflow: hidden;
  font-size: 14px;
  color: #61c550;
  margin-top: 2px;
  margin-left: 10px;
  margin-right: 10px;
  float: left;
}

.ms-req-error {
  color: #f56c6c;
}

.ms-req-error-report-result {
  color: #f6972a;
}

.ms-req-success {
  color: #67c23a;
}
</style>
