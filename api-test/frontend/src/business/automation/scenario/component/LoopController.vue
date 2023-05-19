<template>
  <div>
    <ms-run
      :debug="true"
      :environment="envMap"
      :reportId="reportId"
      :saved="false"
      :runMode="'DEFINITION'"
      :run-data="debugData"
      @errorRefresh="errorRefresh"
      @runRefresh="runRefresh"
      ref="runTest" />
    <api-base-component
      :if-from-variable-advance="ifFromVariableAdvance"
      :data="controller"
      :draggable="draggable"
      :is-max="isMax"
      :show-btn="showBtn"
      :show-version="showVersion"
      :title="$t('api_test.automation.loop_controller')"
      @copy="copyRow"
      @active="active(controller)"
      @remove="remove"
      color="#02A7F0"
      background-color="#F4F4F5">
      <template v-slot:headerLeft>
        <i
          class="icon el-icon-arrow-right"
          :class="{ 'is-active': controller.active }"
          style="margin-right: 10px"
          v-if="!isMax" />
        <el-radio
          :disabled="controller.disabled"
          @change="changeRadio"
          class="ms-radio ms-radio-margin"
          v-model="controller.loopType"
          label="LOOP_COUNT">
          {{ $t('loop.loops_title') }}
        </el-radio>
        <el-radio
          :disabled="controller.disabled"
          @change="changeRadio"
          class="ms-radio ms-radio-margin"
          v-model="controller.loopType"
          label="FOREACH">
          {{ $t('loop.foreach') }}
        </el-radio>
        <el-radio
          :disabled="controller.disabled"
          @change="changeRadio"
          class="ms-radio ms-radio-margin"
          v-model="controller.loopType"
          label="WHILE">
          {{ $t('loop.while') }}
        </el-radio>
      </template>

      <template v-slot:message>
        <span
          v-if="requestResult && requestResult.scenarios && requestResult.scenarios.length > 0"
          style="color: #8c939d; margin-right: 10px" />
      </template>

      <template v-slot:button>
        <el-button
          :disabled="!controller.enable"
          :tip="$t('api_test.run')"
          @click="conn"
          icon="el-icon-video-play"
          class="ms-conn"
          size="mini"
          circle />
      </template>
      <div v-if="controller.loopType === 'LOOP_COUNT'" draggable v-loading="loading">
        <el-row>
          <el-col :span="9">
            <span class="ms-span ms-radio">{{ $t('loop.loops') }}</span>
            <el-input size="small" v-model="controller.countController.loops" style="width: auto"/>
            <span class="ms-span ms-radio">次</span>
          </el-col>
          <el-col :span="9">
            <span class="ms-span ms-radio">{{ $t('loop.interval') }}</span>
            <el-input-number
              v-model="controller.countController.interval"
              :disabled="controller.disabled"
              :placeholder="$t('commons.millisecond')"
              :max="1000 * 1000000"
              :min="0"
              :step="1000"
              size="small" />
            <span class="ms-span ms-radio">ms</span>
          </el-col>
          <el-col :span="6">
              <el-switch
                :inactive-text="$t('loop.proceed')"
                :disabled="controller.disabled"
                v-model="controller.countController.proceed"
                class="ms-span ms-radio"
                style="margin-top: 6px"
                @change="switchChange" />
          </el-col>
        </el-row>
      </div>

      <div v-else-if="controller.loopType === 'FOREACH'" draggable v-loading="loading">
        <el-row>
          <el-col :span="8">
            <el-input
              :disabled="controller.disabled"
              :placeholder="$t('api_test.automation.loop_return_val')"
              v-model="controller.forEachController.returnVal"
              size="small" />
          </el-col>
          <el-col :span="1" style="margin-top: 6px">
            <span style="margin: 10px 10px 10px">in</span>
          </el-col>
          <el-col :span="8">
            <el-input
              :disabled="controller.disabled"
              :placeholder="$t('api_test.automation.loop_input_val')"
              v-model="controller.forEachController.inputVal"
              size="small" />
          </el-col>
          <el-col :span="7">
            <span class="ms-span ms-radio">{{ $t('loop.interval') }}</span>
            <el-input-number
              v-model="controller.forEachController.interval"
              :disabled="controller.disabled"
              size="small"
              :placeholder="$t('commons.millisecond')"
              :max="1000 * 1000000"
              :min="0"
              :step="1000" />
            <span class="ms-span ms-radio">ms</span>
          </el-col>
        </el-row>
      </div>
      <div v-else draggable v-loading="loading">
        <el-input
          v-model="controller.whileController.variable"
          :disabled="controller.disabled"
          size="small"
          :placeholder="$t('api_test.request.condition_variable')"
          style="width: 20%" />
        <el-select
          v-model="controller.whileController.operator"
          :disabled="controller.disabled"
          :placeholder="$t('commons.please_select')"
          size="small"
          @change="change"
          style="width: 10%; margin-left: 10px">
          <el-option v-for="o in operators" :key="o.value" :label="$t(o.label)" :value="o.value" />
        </el-select>
        <el-input
          v-model="controller.whileController.value"
          :disabled="controller.disabled"
          size="small"
          :placeholder="$t('api_test.value')"
          v-if="!hasEmptyOperator"
          style="width: 20%; margin-left: 20px" />
        <span class="ms-span ms-radio">{{ $t('loop.timeout') }}</span>
        <el-input-number
          v-model="controller.whileController.timeout"
          :disabled="controller.disabled"
          size="small"
          :placeholder="$t('commons.millisecond')"
          :max="1000 * 1000000"
          :min="3000"
          :step="1000" />
        <span class="ms-span ms-radio">ms</span>
      </div>
      <template v-slot:debugStepCode>
        <span v-if="node.data.testing" class="ms-test-running">
          <i class="el-icon-loading" style="font-size: 16px" />
          {{ $t('commons.testing') }}
        </span>
        <span
          class="ms-step-debug-code"
          :class="'ms-req-error-report'"
          v-if="
            !loading && !node.data.testing && node.data.debug &&
            node.data.code === 'FAKE_ERROR'
          ">
          FakeError
        </span>
        <span
          class="ms-step-debug-code"
          :class="node.data.code && node.data.code.toUpperCase() === 'ERROR' ? 'ms-req-error' : 'ms-req-success'"
          v-if="!loading && !node.data.testing && node.data.debug &&
                  node.data.code !== 'FAKE_ERROR'">
          {{ getCode() }}
        </span>
      </template>
    </api-base-component>
  </div>
</template>

<script>
import ApiBaseComponent from '../common/ApiBaseComponent';
import ApiResponseComponent from './ApiResponseComponent';
import MsRun from '../DebugRun';
import { getUUID } from 'metersphere-frontend/src/utils';
import { STEP } from '../Setting';
import { getReportMessageSocket } from '@/business/automation/api-automation';
import { useApiStore } from '@/store';

const store = useApiStore();
export default {
  name: 'MsLoopController',
  components: { ApiBaseComponent, ApiResponseComponent, MsRun },
  props: {
    controller: {},
    currentScenario: {},
    node: {},
    message: String,
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
    index: Object,
    draggable: {
      type: Boolean,
      default: false,
    },
    envMap: Map,
    ifFromVariableAdvance: {
      type: Boolean,
      default: false,
    },
  },
  data() {
    return {
      loading: false,
      requestResult: new Map(),
      success: 0,
      error: 0,
      debugData: {},
      report: [],
      reportId: '',
      operators: {
        EQ: {
          label: 'commons.adv_search.operators.equals',
          value: '==',
        },
        NE: {
          label: 'commons.adv_search.operators.not_equals',
          value: '!=',
        },
        LIKE: {
          label: 'commons.adv_search.operators.like',
          value: '=~',
        },
        NOT_LIKE: {
          label: 'commons.adv_search.operators.not_like',
          value: '!~',
        },
        GT: {
          label: 'commons.adv_search.operators.gt',
          value: '>',
        },
        LT: {
          label: 'commons.adv_search.operators.lt',
          value: '<',
        },
        IS_EMPTY: {
          label: 'commons.adv_search.operators.is_empty',
          value: 'is empty',
        },
        IS_NOT_EMPTY: {
          label: 'commons.adv_search.operators.is_not_empty',
          value: 'is not empty',
        },
      },
      stepFilter: new STEP(),
      messageWebSocket: {},
      uuid: '',
    };
  },
  watch: {
    message() {
      this.reload();
    },
  },
  methods: {
    initMessageSocket() {
      this.uuid = getUUID();
      this.messageWebSocket = getReportMessageSocket(this.uuid);
      this.messageWebSocket.onmessage = this.onDebugMessage;
    },
    onDebugMessage(e) {
      // 确认连接建立成功，开始执行
      if (e && e.data === 'CONN_SUCCEEDED') {
        this.runDebug();
      }
      if (e && e.data === 'DEBUG_ERROR') {
        this.$error(this.$t('api_definition.debug_pool_warning'));
        this.loading = false;
        this.node.expanded = true;
        this.messageWebSocket.close();
        this.reload();
      }
      if (e.data && e.data.startsWith('result_')) {
        let data = JSON.parse(e.data.substring(7));
        this.debugCode(data);
        let resourceId = data.resourceId.split('_')[0];
        if (this.requestResult.has(resourceId)) {
          this.requestResult.get(resourceId).push(data);
        } else {
          this.requestResult.set(resourceId, [data]);
        }
      } else if (e.data && e.data.indexOf('MS_TEST_END') !== -1) {
        this.loading = false;
        this.node.expanded = true;
        this.messageWebSocket.close();
        // // 把请求结果分给各个请求
        this.setResult(this.controller.hashTree);
        store.currentApiCase = { debugLoop: getUUID() };
        this.reload();
      }
    },
    clear(hashTree) {
      if (hashTree) {
        hashTree.forEach((item) => {
          if (this.stepFilter.get('AllSamplerProxy').indexOf(item.type) !== -1) {
            item.requestResult = [];
            item.result = undefined;
            item.code = undefined;
          }
          if (item.hashTree && item.hashTree.length > 0) {
            this.setResult(item.hashTree);
          }
        });
      }
    },
    debugCode(data) {
      if (data && this.node && this.node.data) {
        if (!this.node.data.code) {
          this.node.data.code = 'SUCCESS';
        }
        if (this.node.data.code ==='SUCCESS' && data.status && data.status === 'SUCCESS') {
          this.node.data.code = 'SUCCESS';
        }
        if ((this.node.data.code ==='SUCCESS' ||
            this.node.data.code === 'FAKE_ERROR') &&
          data.status && data.status === 'FAKE_ERROR') {
          this.node.data.code = 'FAKE_ERROR';
        }
        if (data.status && data.status === 'ERROR') {
          this.node.data.code = 'ERROR';
        }
      }
      this.reload();
    },
    getCode() {
      if (this.node && this.node.data.code && this.node.data.debug) {
        let status = this.node.data.code;
        return status.toLowerCase()[0].toUpperCase() + status.toLowerCase().substr(1);
      }
      return '';
    },
    switchChange() {
      if (this.controller.hashTree && this.controller.hashTree.length > 1) {
        this.$warning(this.$t('api_test.automation.loop_message'));
        this.controller.countController.proceed = true;
        return;
      }
      // 递归遍历所有请求数量
      if (
        this.controller.hashTree &&
        this.controller.hashTree.length === 1 &&
        this.controller.hashTree[0].hashTree &&
        this.controller.hashTree[0].hashTree.length > 0
      ) {
        let count = 0;
        this.controller.hashTree[0].hashTree.forEach((item) => {
          if (this.stepFilter.get('AllSamplerProxy').indexOf(item.type) !== -1) {
            count++;
          }
          if (item.hashTree && item.hashTree.length > 0) {
            this.recursive(item.hashTree, count);
          }
        });
        if (count > 1) {
          this.$warning(this.$t('api_test.automation.loop_message'));
          this.controller.countController.proceed = true;
          return;
        }
      }
    },
    recursive(arr, count) {
      for (let i in arr) {
        if (this.stepFilter.get('AllSamplerProxy').indexOf(arr[i].type) !== -1) {
          count++;
        }
        if (arr[i].hashTree && arr[i].hashTree.length > 0) {
          this.recursive(arr[i].hashTree, count);
        }
      }
    },
    conn() {
      if (!this.controller.hashTree || this.controller.hashTree.length < 1) {
        this.$warning('当前循环下没有请求，不能执行');
        return;
      }
      if (!this.controller.enable) {
        this.$warning(this.$t('api_test.automation.debug_message'));
        return;
      }
      this.requestResult.clear();
      this.clear(this.controller.hashTree);
      this.initMessageSocket();
    },
    runDebug() {
      this.loading = true;
      let currentEnvironmentId;
      let resourceId = this.currentScenario.id + '_' + this.controller.projectId;
      if (store.scenarioEnvMap && store.scenarioEnvMap instanceof Map && store.scenarioEnvMap.has(resourceId)) {
        currentEnvironmentId = store.scenarioEnvMap.get(resourceId);
      }
      this.debugData = {
        id: this.currentScenario.id,
        name: this.currentScenario.name,
        type: 'scenario',
        variables: this.currentScenario.variables,
        headers: this.currentScenario.headers,
        referenced: 'Created',
        enableCookieShare: this.enableCookieShare,
        environmentId: currentEnvironmentId,
        hashTree: [this.controller],
      };
      if (this.node && this.node.data) {
        this.node.data.debug = true;
      }
      this.reportId = this.uuid;
      this.node.data.code = '';
      this.node.data.testing = false;
      this.node.data.debug = true;
    },

    remove() {
      this.$emit('remove', this.controller, this.node);
    },
    copyRow() {
      this.$emit('copyRow', this.controller, this.node);
    },
    active(item) {
      item.active = !item.active;
      if (this.node) {
        this.node.expanded = item.active;
      }
      this.reload();
    },
    changeRadio() {
      this.controller.active = true;
      this.reload();
    },
    change(value) {
      if (value.indexOf('empty') > 0 && !!this.controller.value) {
        this.controller.value = '';
      }
    },
    reload() {
      this.loading = true;
      this.$nextTick(() => {
        this.loading = false;
      });
    },
    runRefresh() {},
    errorRefresh() {
      this.loading = false;
    },
    setResult(hashTree) {
      if (hashTree) {
        hashTree.forEach((item) => {
          if (this.stepFilter.get('AllSamplerProxy').indexOf(item.type) !== -1 && this.requestResult.has(item.id)) {
            item.activeName = '0';
            item.active = true;
            item.requestResult = this.requestResult.get(item.id);
            this.$set(item, 'debug', true);
          }
          if (item.hashTree && item.hashTree.length > 0) {
            this.setResult(item.hashTree);
          }
        });
      }
    },
  },
  computed: {
    hasEmptyOperator() {
      return !!this.controller.operator && this.controller.operator.indexOf('empty') > 0;
    },
  },
};
</script>

<style scoped>
.ms-span {
  margin-left: 10px;
  margin-right: 10px;
}

.ms-radio {
  color: #606266;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', Arial, sans-serif;
  font-size: 13px;
  font-weight: normal;
}
:deep(.el-switch__label *){
  color: #606266;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', Arial, sans-serif;
  font-size: 13px;
  font-weight: normal;
}
.icon.is-active {
  transform: rotate(90deg);
}

.ms-req-error {
  color: #f56c6c;
}

.ms-req-success {
  color: #67c23a;
}

:deep(.el-radio) {
  margin-right: 5px;
}

.ms-test-running {
  color: #783887;
}

.ms-step-debug-code {
  display: inline-block;
  margin: 0 5px;
  overflow-x: hidden;
  padding-bottom: 0;
  text-overflow: ellipsis;
  vertical-align: middle;
  white-space: nowrap;
  width: 60px;
}

.ms-conn {
  background-color: #409eff;
  color: white;
  padding: 5px;
}

.ms-req-error-report {
  color: #f6972a;
}
</style>
