<template>
  <div class="scenario-result" @click.stop="handleClick">
    <div
      v-if="
        (node.children && node.children.length > 0) ||
        node.unsolicited ||
        (node.type && this.stepFilter.get('AllSamplerProxy').indexOf(node.type) === -1)
      ">
      <el-card class="ms-card">
        <el-row>
          <el-col :span="22">
            <div class="el-step__icon is-text ms-api-col">
              <div class="el-step__icon-inner">
                {{ node.index }}
              </div>
            </div>
            <el-tooltip effect="dark" :content="node.label" placement="top">
              <el-link v-if="node.redirect" class="report-label-head" @click="isLink">
                {{ getLabel(node.label) }}
              </el-link>
              <span v-else class="ms-req-name">{{ getLabel(node.label) }}</span>
            </el-tooltip>
          </el-col>
          <el-col :span="2">
            <div style="float: right">
              <ms-api-report-status
                :status="node.totalStatus"
                v-if="
                  node.type !== 'ConstantTimer' &&
                  node.type !== 'Assertions' &&
                  node.children
                " />
            </div>
          </el-col>
        </el-row>
      </el-card>
    </div>
    <div v-else>
      <ms-request-result
        :step-id="node.stepId"
        :request="node.value"
        :redirect="node.redirect"
        :indexNumber="node.index"
        :error-code="node.errorCode"
        :scenarioName="node.label"
        :resourceId="node.resourceId"
        :total-status="node.totalStatus"
        :expanded.sync="innerExpanded"
        :console="console"
        :is-share="isShare"
        :share-id="shareId"
        v-on:requestResult="requestResult" />
    </div>
  </div>
</template>

<script>
import MsRequestResult from './RequestResult';
import { STEP } from '../../../../business/automation/scenario/Setting';
import { getCurrentByResourceId } from '../../../../api/user';
import MsApiReportStatus from '../ApiReportStatus';

export default {
  name: 'MsScenarioResult',
  components: {
    MsRequestResult,
    MsApiReportStatus,
  },
  props: {
    scenario: Object,
    node: Object,
    console: String,
    isShare: Boolean,
    expanded: Boolean,
    shareId: String,
  },
  data() {
    return {
      stepFilter: new STEP(),
      innerExpanded: false,
    };
  },
  watch: {
    expanded(val) {
      this.innerExpanded = val;
    },
    innerExpanded(val) {
      this.$emit('update:expanded', val);
    },
  },
  methods: {
    getLabel(label) {
      switch (label) {
        case 'ConstantTimer':
          return this.$t('api_test.automation.wait_controller');
        case 'LoopController':
          return this.$t('api_test.automation.loop_controller');
        case 'Assertions':
          return this.$t('api_test.definition.request.scenario_assertions');
        case 'IfController':
          return this.$t('api_test.automation.if_controller');
        case 'TransactionController':
          return this.$t('api_test.automation.transaction_controller');
        default:
          return label;
      }
    },
    isLink() {
      let uri = '/#/api/automation?resourceId=' + this.node.resourceId;
      this.clickResource(uri);
    },
    clickResource(uri) {
      getCurrentByResourceId(this.node.resourceId).then(() => {
        this.toPage(uri);
      });
    },
    toPage(uri) {
      let id = 'new_a';
      let a = document.createElement('a');
      a.setAttribute('href', uri);
      a.setAttribute('target', '_blank');
      a.setAttribute('id', id);
      document.body.appendChild(a);
      a.click();

      let element = document.getElementById(id);
      element.parentNode.removeChild(element);
    },
    requestResult(requestResult) {
      this.$emit('requestResult', requestResult);
    },
    handleClick() {
      this.innerExpanded = !this.innerExpanded;
    },
  },

  computed: {
    assertion() {
      return this.scenario.passAssertions + ' / ' + this.scenario.totalAssertions;
    },
    success() {
      return this.scenario.error === 0;
    },
  },
};
</script>

<style scoped>
.scenario-result {
  width: 100%;
  padding: 2px 0;
}

.scenario-result + .scenario-result {
  border-top: 1px solid #dcdfe6;
}

.ms-card :deep(.el-card__body) {
  padding: 10px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.scenario-result .info {
  height: 40px;
  cursor: pointer;
}

.scenario-result .icon {
  padding: 5px;
}

.scenario-result .icon.is-active {
  transform: rotate(90deg);
}

.ms-api-col {
  background-color: #eff0f0;
  border-color: #eff0f0;
  margin-right: 10px;
  font-size: 12px;
  color: #64666a;
}

.ms-card .ms-api-col-create {
  background-color: #ebf2f2;
  border-color: #008080;
  margin-right: 10px;
  font-size: 12px;
  color: #008080;
}

.report-label-head {
  border-bottom: 1px solid #303133;
  color: #303133;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', Arial, sans-serif;
  font-size: 13px;
}

:deep(.el-step__icon) {
  width: 20px;
  height: 20px;
  font-size: 12px;
}

:deep(.ms-card .el-card__body) {
  padding-right: 1px;
}

.ms-req-name {
  display: inline-block;
  margin: 0 5px;
  padding-bottom: 0;
  overflow-x: hidden;
  text-overflow: ellipsis;
  vertical-align: middle;
  white-space: nowrap;
  width: 350px;
}
</style>
