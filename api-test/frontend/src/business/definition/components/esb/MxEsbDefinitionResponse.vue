<template>
  <el-row>
    <el-col :span="spanNum">
      <div style="border: 1px #dcdfe6 solid; height: 100%; border-radius: 4px; width: 100%">
        <el-form class="tcp" :model="request" ref="request" :disabled="isReadOnly" style="margin: 20px">
          <el-card v-if="showHeader" class="api-component">
            <div class="header" @click="active">
              <i class="icon el-icon-arrow-right" :class="{ 'is-active': isActive }" />
              <ms-request-metric :response="response" />
            </div>

            <el-collapse-transition>
              <div v-if="isActive">
                <el-divider></el-divider>
                <esb-response-result
                  :show-options-button="showOptionsButton"
                  :is-api-component="false"
                  :show-metric="false"
                  :response="response"
                  :request="request" />
              </div>
            </el-collapse-transition>
          </el-card>
          <esb-response-result
            v-else
            :show-options-button="showOptionsButton"
            :request="request"
            @refreshEsbDataStruct="refreshEsbDataStruct"
            :is-api-component="isApiComponent"
            :show-metric="false"
            :response="response" />
        </el-form>
      </div>
    </el-col>
  </el-row>
</template>

<script>
import { getApiReportDetail } from '@/api/definition-report';
import ApiBaseComponent from '@/business/automation/scenario/common/ApiBaseComponent';
import MsRequestResultTail from '@/business/definition/components/response/RequestResultTail';
import ElCollapseTransition from 'element-ui/src/transitions/collapse-transition';
import MsRequestMetric from '@/business/definition/components/response/RequestMetric';
import EsbResponseResult from './EsbResponseResult';

export default {
  name: 'MxEsbDefinitionResponse',
  components: {
    ElCollapseTransition,
    MsRequestResultTail,
    ApiBaseComponent,
    MsRequestMetric,
    EsbResponseResult,
  },
  props: {
    apiItem: {},
    result: {},
    showHeader: Boolean,
    showOptionsButton: Boolean,
    isApiComponent: Boolean,
    responseData: Object,
    request: {},
  },
  data() {
    return {
      spanNum: 21,
      isActive: false,
      isReadOnly: false,
      response: { responseResult: {} },
    };
  },
  created() {
    if (!this.referenced && this.showScript) {
      this.spanNum = 21;
    } else {
      this.spanNum = 24;
    }
    if (!this.result) {
      if (!this.isApiComponent) {
        this.getExecResult();
      }
    } else {
      this.response = this.result;
    }

    if (this.request.backEsbDataStruct) {
      this.refreshEsbDataStruct(this.request.backEsbDataStruct);
    } else {
      this.request.backEsbDataStruct = [];
    }
  },
  watch: {
    result() {
      this.response = this.result;
    },
    responseData() {
      this.response = this.responseData;
    },
  },
  methods: {
    getExecResult() {
      // 执行结果信息
      if (this.apiItem) {
        getApiReportDetail(this.apiItem.id).then((response) => {
          if (response.data) {
            let data = JSON.parse(response.data.content);
            this.response = data;
            this.$set(this.apiItem, 'responseData', data);
          }
        });
      }
    },
    active() {
      this.isActive = !this.isActive;
    },
    refreshEsbDataStruct(backEsbDataStruct) {
      if (backEsbDataStruct && backEsbDataStruct.length > 0) {
        backEsbDataStruct.forEach((row) => {
          row.status = '';
          if (row.children == null || row.children.length === 0) {
            row.children = [];
          } else if (row.children.length > 0) {
            this.refreshEsbDataStruct(row.children);
          }
        });
      }
    },
  },
};
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
