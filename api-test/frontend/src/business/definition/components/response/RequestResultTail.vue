<template>
  <div class="request-result" v-loading="loading">
    <ms-request-metric v-if="showMetric" :response="reportId ? report : response" />
    <ms-response-result
      :currentProtocol="currentProtocol"
      :response="reportId ? report : response"
      :isTestPlan="isTestPlan" />
  </div>
</template>

<script>
import MsResponseResult from '../response/ResponseResult';
import MsRequestMetric from '../response/RequestMetric';
import { getApiReportDetail } from '../../../../api/definition-report';
import { baseSocket } from '@/api/base-network';

export default {
  name: 'MsRequestResultTail',
  components: { MsRequestMetric, MsResponseResult },
  props: {
    response: Object,
    currentProtocol: String,
    reportId: String,
    showMetric: {
      type: Boolean,
      default() {
        return true;
      },
    },
    isTestPlan: {
      type: Boolean,
      default() {
        return false;
      },
    },
  },
  watch: {
    reportId: {
      handler(v1, v2) {
        this.getExecResult();
      },
      immediate: true,
    },
  },
  data() {
    return {
      loading: false,
      report: {},
    };
  },
  methods: {
    getExecResult() {
      if (this.reportId) {
        this.loading = true;
        getApiReportDetail(this.reportId).then((response) => {
          this.loading = false;
          let data = response.data;
          if (data) {
            this.report = JSON.parse(data.content);
            if (data.status === 'RUNNING') {
              this.loading = true;
              this.socketSync();
            } else if (data.status === 'SUCCESS') {
              this.$EventBus.$emit('API_TEST_END', this.reportId);
            } else {
              this.$EventBus.$emit('API_TEST_ERROR', this.reportId);
            }
          }
        });
      }
    },
    socketSync() {
      this.websocket = baseSocket(this.reportId);
      this.websocket.onmessage = this.onMessages;
      this.websocket.onerror = this.onError;
    },
    onError() {
      this.$EventBus.$emit('API_TEST_ERROR', this.reportId);
    },
    onMessages(e) {
      if (e.data && e.data.startsWith('result_')) {
        try {
          let data = e.data.substring(7);
          this.report = JSON.parse(data);
          this.websocket.close();
          this.loading = false;
          this.$EventBus.$emit('API_TEST_END', this.reportId);
        } catch (e) {
          console.log(e); // for debug
          this.websocket.close();
          this.$EventBus.$emit('API_TEST_ERROR', this.reportId);
        }
      } else if (e.data === 'MS_TEST_END') {
        this.$EventBus.$emit('API_TEST_ERROR', this.reportId);
      }
    },
  },
};
</script>

<style scoped>
.request-result {
  width: 100%;
  min-height: 40px;
  padding: 2px 0;
}

.request-result .info {
  background-color: #f9f9f9;
  margin-left: 20px;
  cursor: pointer;
}

.request-result .method {
  color: #1e90ff;
  font-size: 14px;
  font-weight: 500;
  line-height: 40px;
  padding-left: 5px;
}

.request-result .url {
  color: #7f7f7f;
  font-size: 12px;
  font-weight: 400;
  margin-top: 4px;
  white-space: nowrap;
  text-overflow: ellipsis;
  overflow: hidden;
  word-break: break-all;
}

.request-result .tab .el-tabs__header {
  margin: 0;
}

.request-result .text {
  height: 300px;
  overflow-y: auto;
}

.sub-result .info {
  background-color: #fff;
}

.sub-result .method {
  border-left: 5px solid #1e90ff;
  padding-left: 20px;
}

.sub-result:last-child {
  border-bottom: 1px solid #ebeef5;
}

.request-result .icon.is-active {
  transform: rotate(90deg);
}
</style>
