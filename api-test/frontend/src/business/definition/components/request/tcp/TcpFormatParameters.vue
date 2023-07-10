<template>
  <div v-if="!isReloadData">
    <el-row>
      <el-col :span="spanNum" style="padding-bottom: 20px">
        <div style="border: 1px #dcdfe6 solid; height: 100%; border-radius: 4px; width: 100%">
          <el-form class="tcp" :model="request" :rules="rules" ref="request" :disabled="isReadOnly">
            <el-tabs v-model="activeName" class="request-tabs ms-tabs__nav-scroll" @tab-click="tabClick">
              <!--test-->
              <el-tab-pane name="parameters">
                <template v-slot:label>
                  {{ $t('api_test.definition.request.req_param') }}
                  <ms-instructions-icon :content="$t('api_test.definition.request.tcp_parameter_tip')" />
                </template>
                <ms-api-variable :is-read-only="isReadOnly" :parameters="request.parameters" />
              </el-tab-pane>
              <!--test-->
              <el-tab-pane
                v-if="isBodyShow"
                :label="$t('api_test.definition.document.request_body')"
                name="request"
                v-loading="result">
                <el-radio-group v-model="reportType" size="mini" style="margin: 10px 0px">
                  <el-radio :disabled="isReadOnly" label="json" @change="changeReportType"> json </el-radio>
                  <el-radio :disabled="isReadOnly" label="xml" @change="changeReportType"> xml </el-radio>
                  <el-radio :disabled="isReadOnly" label="raw" @change="changeReportType"> raw </el-radio>
                </el-radio-group>
                <div v-if="request.reportType === 'xml'">
                  <tcp-xml-table
                    :table-data="request.xmlDataStruct"
                    :show-options-button="true"
                    @xmlTablePushRow="xmlTablePushRow"
                    @initXmlTableData="initXmlTableData"
                    @saveTableData="saveXmlTableData"
                    ref="treeTable"></tcp-xml-table>
                </div>
                <div v-if="request.reportType === 'json'">
                  <div class="send-request">
                    <ms-code-edit
                      mode="json"
                      :read-only="isReadOnly"
                      :data.sync="request.jsonDataStruct"
                      :modes="['text', 'json', 'xml', 'html']"
                      theme="eclipse" />
                  </div>
                </div>
                <div v-if="request.reportType === 'raw'">
                  <div class="send-request">
                    <ms-code-edit
                      mode="text"
                      :read-only="isReadOnly"
                      :data.sync="request.rawDataStruct"
                      :modes="['text', 'json', 'xml', 'html']"
                      theme="eclipse" />
                  </div>
                  <el-button
                    class="ht-btn-add"
                    size="mini"
                    p="$t('commons.add')"
                    icon="el-icon-circle-plus-outline"
                    @click="toXml">
                    {{ $t('api_test.buttons.to_xml') }}
                  </el-button>
                </div>
              </el-tab-pane>

              <el-tab-pane :label="$t('api_test.definition.request.other_config')" name="other" class="other-config">
                <el-row style="margin: 0px 10px">
                  <el-col :span="7">
                    <el-form-item label="TCPClient" prop="classname" label-width="120px">
                      <el-select v-model="request.classname" style="width: 100%" size="small">
                        <el-option v-for="c in classes" :key="c" :label="c" :value="c" />
                      </el-select>
                    </el-form-item>
                    <el-form-item :label="$t('api_test.request.tcp.username')" prop="username" label-width="120px">
                      <el-input v-model="request.username" maxlength="100" show-word-limit size="small" />
                    </el-form-item>
                    <el-form-item :label="$t('api_test.request.tcp.eol_byte')" prop="eolByte" label-width="120px">
                      <el-input v-model="request.eolByte" size="small" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="6" label-width="80px">
                    <el-form-item :label="$t('api_test.request.tcp.so_linger')" prop="soLinger" label-width="120px">
                      <el-input v-model="request.soLinger" size="small" />
                    </el-form-item>
                    <el-form-item :label="$t('api_test.request.tcp.password')" prop="password" label-width="120px">
                      <el-input
                        v-model="request.password"
                        maxlength="30"
                        show-word-limit
                        show-password
                        autocomplete="new-password"
                        size="small" />
                    </el-form-item>
                    <el-form-item label="Encoding" label-width="120px">
                      <el-select v-model="request.connectEncoding" size="small">
                        <el-option
                          v-for="item in connectEncodingArr"
                          :key="item.key"
                          :label="item.value"
                          :value="item.key" />
                      </el-select>
                    </el-form-item>
                  </el-col>
                  <el-col :span="5">
                    <el-form-item :label="$t('api_test.request.tcp.close_connection')" label-width="140px">
                      <el-checkbox v-model="request.closeConnection" />
                    </el-form-item>
                    <el-form-item :label="$t('api_test.request.tcp.no_delay')" label-width="140px">
                      <el-checkbox v-model="request.nodelay" />
                    </el-form-item>
                    <el-form-item :label="$t('api_test.request.tcp.re_use_connection')" label-width="140px">
                      <el-checkbox v-model="request.reUseConnection" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="3">
                    <el-form-item :label="$t('api_test.request.tcp.connect')" prop="ctimeout" label-width="80px">
                      <el-input-number v-model="request.ctimeout" controls-position="right" :min="0" size="small" />
                    </el-form-item>
                    <el-form-item :label="$t('api_test.request.tcp.response')" prop="timeout" label-width="80px">
                      <el-input-number v-model="request.timeout" controls-position="right" :min="0" size="small" />
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
                <ms-jmx-step
                  :request="request"
                  :scenarioId="scenarioId"
                  :apiId="request.id"
                  protocol="TCP"
                  :response="response"
                  :tab-type="'pre'"
                  ref="preStep" />
              </el-tab-pane>
              <el-tab-pane
                :label="$t('api_test.definition.request.post_operation')"
                name="postOperate"
                v-if="showScript">
                <span class="item-tabs" effect="dark" placement="top-start" slot="label">
                  {{ $t('api_test.definition.request.post_operation') }}
                  <div class="el-step__icon is-text ms-api-col ms-header" v-if="request.postSize > 0">
                    <div class="el-step__icon-inner">
                      {{ request.postSize }}
                    </div>
                  </div>
                </span>
                <ms-jmx-step
                  :request="request"
                  :scenarioId="scenarioId"
                  :apiId="request.id"
                  protocol="TCP"
                  :response="response"
                  :tab-type="'post'"
                  ref="postStep" />
              </el-tab-pane>
              <el-tab-pane
                :label="$t('api_test.definition.request.assertions_rule')"
                name="assertionsRule"
                v-if="showScript">
                <span class="item-tabs" effect="dark" placement="top-start" slot="label">
                  {{ $t('api_test.definition.request.assertions_rule') }}
                  <div class="el-step__icon is-text ms-api-col ms-header" v-if="request.ruleSize > 0">
                    <div class="el-step__icon-inner">
                      {{ request.ruleSize }}
                    </div>
                  </div>
                </span>
                <ms-jmx-step
                  :request="request"
                  :apiId="request.id"
                  protocol="TCP"
                  :scenario-id="scenarioId"
                  :response="response"
                  @reload="reloadBody"
                  :tab-type="'assertionsRule'"
                  ref="assertionsRule" />
              </el-tab-pane>
            </el-tabs>
          </el-form>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { definitionRawToXML } from '@/api/definition';
import MsApiKeyValue from '../../ApiKeyValue';
import MsApiAssertions from '../../assertion/ApiAssertions';
import MsApiExtract from '../../extract/ApiExtract';
import ApiRequestMethodSelect from '../../collapse/ApiRequestMethodSelect';
import MsCodeEdit from 'metersphere-frontend/src/components/MsCodeEdit';
import MsApiScenarioVariables from '../../ApiScenarioVariables';
import { parseEnvironment } from '@/business/environment/model/EnvironmentModel';
import ApiEnvironmentConfig from 'metersphere-frontend/src/components/environment/ApiEnvironmentConfig';
import { API_STATUS } from '../../../model/JsonData';
import TCPSampler from '../../jmeter/components/sampler/tcp-sampler';
import { getCurrentProjectID } from 'metersphere-frontend/src/utils/token';
import { getUUID } from 'metersphere-frontend/src/utils';
import MsApiVariable from '../../ApiVariable';
import MsInstructionsIcon from 'metersphere-frontend/src/components/MsInstructionsIcon';
import Jsr233ProcessorContent from '@/business/automation/scenario/common/Jsr233ProcessorContent';
import ApiDefinitionStepButton from '../components/ApiDefinitionStepButton';
import TcpXmlTable from '@/business/definition/components/complete/table/TcpXmlTable';
import { TYPE_TO_C } from '@/business/automation/scenario/Setting';
import MsJmxStep from '../../step/JmxStep';
import { hisDataProcessing, stepCompute } from '@/business/definition/api-definition';
import { getEnvironmentByProjectId } from 'metersphere-frontend/src/api/environment';

export default {
  name: 'MsTcpFormatParameters',
  components: {
    TcpXmlTable,
    ApiDefinitionStepButton,
    Jsr233ProcessorContent,
    MsInstructionsIcon,
    MsApiVariable,
    MsApiScenarioVariables,
    MsCodeEdit,
    ApiRequestMethodSelect,
    MsApiExtract,
    MsApiAssertions,
    MsApiKeyValue,
    ApiEnvironmentConfig,
    MsJmxStep,
  },
  props: {
    scenarioId: String,
    request: {},
    basisData: {},
    response: {},
    moduleOptions: Array,
    isReadOnly: {
      type: Boolean,
      default: false,
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
  data() {
    return {
      spanNum: 24,
      result: false,
      activeName: 'request',
      classes: TCPSampler.CLASSES,
      reportType: 'xml',
      isReloadData: false,
      refreshedXmlTable: true,
      isBodyShow: true,
      options: API_STATUS,
      currentProjectId: '',
      connectEncodingArr: [
        {
          key: 'UTF-8',
          value: 'UTF-8',
        },
        {
          key: 'GBK',
          value: 'GBK',
        },
      ],
      rules: {
        classname: [{ required: true, message: '请选择TCPClient', trigger: 'change' }],
        server: [
          {
            required: true,
            message: this.$t('api_test.request.tcp.server_cannot_be_empty'),
            trigger: 'blur',
          },
        ],
        port: [
          {
            required: true,
            message: this.$t('commons.port_cannot_be_empty'),
            trigger: 'change',
          },
        ],
      },
    };
  },
  watch: {
    reportType() {
      this.request.reportType = this.reportType;
    },
    'request.hashTree': {
      handler(v) {
        this.initStepSize(this.request.hashTree);
      },
      deep: true,
    },
  },
  created() {
    this.currentProjectId = getCurrentProjectID();
    if (!this.request.classname) {
      this.request.classname = 'TCPClientImpl';
    }
    if (!this.request.parameters) {
      this.$set(this.request, 'parameters', []);
      this.request.parameters = [];
    }
    if (this.request.tcpPreProcessor && this.request.tcpPreProcessor.script) {
      this.request.hashTree.push(this.request.tcpPreProcessor);
      this.request.tcpPreProcessor = undefined;
    }
    if (this.request.tcpPreProcessor) {
      this.request.tcpPreProcessor.clazzName = TYPE_TO_C.get(this.request.tcpPreProcessor.type);
    }
    if (!this.request.connectEncoding) {
      this.request.connectEncoding = 'UTF-8';
    }
    this.getEnvironments();

    if (this.request) {
      //处理各种旧数据
      if (!this.request.xmlDataStruct && !this.request.jsonDataStruct && !this.request.rawDataStruct) {
        this.request.rawDataStruct = this.request.request;
        this.request.reportType = 'raw';
      }

      if (!this.request.reportType) {
        this.request.reportType = 'raw';
      } else {
        if (
          this.request.reportType !== 'json' &&
          this.request.reportType !== 'xml' &&
          this.request.reportType !== 'raw'
        ) {
          this.request.reportType = 'raw';
        }
      }
      this.reportType = this.request.reportType;
      if (!this.request.xmlDataStruct) {
        this.initXmlTableData();
      }
      if (this.request.hashTree) {
        this.initStepSize(this.request.hashTree);
        this.historicalDataProcessing(this.request.hashTree);
      }
    }
  },
  methods: {
    toXml() {
      if (!this.request.rawDataStruct) {
        this.$warning(this.$t('api_definition.raw_not_null'));
        return;
      }
      this.result = definitionRawToXML({
        stringData: this.request.rawDataStruct,
      }).then((response) => {
        if (response.data) {
          this.request.xmlDataStruct = response.data;
          this.reportType = 'xml';
        }
      });
    },
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
    remove(row) {
      let index = this.request.hashTree.indexOf(row);
      this.request.hashTree.splice(index, 1);
      this.reload();
    },
    copyRow(row) {
      let obj = JSON.parse(JSON.stringify(row));
      obj.id = getUUID();
      this.request.hashTree.push(obj);
      this.reload();
    },
    setReportType(param) {
      if (param && param !== '') {
        this.reportType = param;
      }
    },
    reload() {
      this.isReloadData = true;
      this.$nextTick(() => {
        this.isReloadData = false;
      });
    },
    validateApi() {
      if (this.currentProjectId === null) {
        this.$error(this.$t('api_test.select_project'), 2000);
        return;
      }
      this.$refs['basicForm'].validate();
    },
    saveApi() {
      this.basisData.method = this.basisData.protocol;
      this.$emit('saveApi', this.basisData);
    },
    runTest() {},
    validate() {
      if (this.currentProjectId === null) {
        this.$error(this.$t('api_test.select_project'), 2000);
        return;
      }
      this.$refs['request'].validate((valid) => {
        if (valid) {
          this.$emit('callback');
        }
      });
    },
    getEnvironments() {
      if (this.currentProjectId) {
        this.environments = [];
        getEnvironmentByProjectId(this.currentProjectId).then((response) => {
          this.environments = response.data;
          this.environments.forEach((environment) => {
            parseEnvironment(environment);
          });
          this.initDataSource();
        });
      }
    },
    openEnvironmentConfig() {
      if (!this.currentProjectId) {
        this.$error(this.$t('api_test.select_project'));
        return;
      }
      this.$refs.environmentConfig.open(this.currentProjectId);
    },
    initDataSource() {
      for (let i in this.environments) {
        if (this.environments[i].id === this.request.environmentId) {
          this.databaseConfigsOptions = [];
          this.environments[i].config.databaseConfigs.forEach((item) => {
            this.databaseConfigsOptions.push(item);
          });
          break;
        }
      }
    },
    environmentChange(value) {
      this.request.dataSource = undefined;
      for (let i in this.environments) {
        if (this.environments[i].id === value) {
          this.databaseConfigsOptions = [];
          this.environments[i].config.databaseConfigs.forEach((item) => {
            this.databaseConfigsOptions.push(item);
          });
          break;
        }
      }
    },
    environmentConfigClose() {
      this.getEnvironments();
    },

    changeReportType() {},

    //以下是xml树形表格相关的方法
    updateXmlTableData(dataStruct) {
      this.request.xmlDataStruct = dataStruct;
    },
    saveXmlTableData(dataStruct) {
      let valedataResult = this.validateXmlDataStruct(dataStruct);
      if (valedataResult) {
        this.request.xmlDataStruct = dataStruct;
        this.refreshXmlTable();
      }
    },
    validateXmlDataStruct() {
      if (this.request.xmlDataStruct) {
        this.refreshXmlTableDataStruct(this.request.xmlDataStruct);
        let result = this.checkXmlTableDataStructData(this.request.xmlDataStruct);
        return result;
      }
    },
    refreshXmlTableDataStruct(dataStruct) {
      if (dataStruct && dataStruct.length > 0) {
        dataStruct.forEach((row) => {
          row.status = '';
          if (row.children == null || row.children.length === 0) {
            row.children = [];
          } else if (row.children.length > 0) {
            this.refreshXmlTableDataStruct(row.children);
          }
        });
      }
    },
    checkXmlTableDataStructData(dataStruct) {
      let allCheckResult = true;
      if (this.$refs.treeTable) {
        if (dataStruct && dataStruct.length > 0) {
          for (let i = 0; i < dataStruct.length; i++) {
            let row = dataStruct[i];
            allCheckResult = this.$refs.treeTable.validateRowData(row);
            if (allCheckResult) {
              if (row.children != null && row.children.length > 0) {
                allCheckResult = this.checkXmlTableDataStructData(row.children);
                if (!allCheckResult) {
                  return false;
                }
              }
            } else {
              return false;
            }
          }
        }
      }
      return allCheckResult;
    },
    initXmlTableData() {
      if (this.request) {
        this.request.xmlDataStruct = [];
        this.refreshXmlTable();
      }
    },
    xmlTableDataPushRow(newRow) {
      if (this.request) {
        if (!this.request.xmlDataStruct) {
          this.request.xmlDataStruct = [];
        }
        this.request.xmlDataStruct.push(newRow);
        this.refreshXmlTable();
      }
    },
    xmlTableDataRemoveRow(row) {
      if (this.request) {
        if (this.request.xmlDataStruct) {
          this.removeFromDataStruct(this.request.xmlDataStruct, row);
          this.refreshXmlTable();
        }
      }
    },
    removeFromDataStruct(dataStruct, row) {
      if (!dataStruct || dataStruct.length === 0) {
        return;
      }
      let rowIndex = dataStruct.indexOf(row);
      if (rowIndex >= 0) {
        dataStruct.splice(rowIndex, 1);
      } else {
        dataStruct.forEach((itemData) => {
          if (!itemData.children && itemData.children.length > 0) {
            this.removeFromDataStruct(itemData.children, row);
          }
        });
      }
    },
    refreshXmlTable() {
      this.refreshedXmlTable = true;
      this.$nextTick(() => {
        this.refreshedXmlTable = false;
      });
    },
    xmlTablePushRow(row) {
      this.request.xmlDataStruct.push(row);
    },
  },
};
</script>

<style scoped>
.send-request {
  padding: 0px 0;
  height: 300px;
  border: 1px #dcdfe6 solid;
  border-radius: 4px;
  width: 100%;
}

.ms-left-cell {
  margin-top: 40px;
}

.ms-header {
  background: #783887;
  color: white;
  height: 18px;
  font-size: xx-small;
  border-radius: 50%;
}

.ms-left-buttion {
  margin: 6px 0px 8px 30px;
}

:deep(.el-form-item) {
  margin-bottom: 15px;
}

.ms-left-cell {
  margin-top: 40px;
}

.ms-left-buttion {
  margin: 6px 0px 8px 30px;
}

:deep(.el-form-item) {
  margin-bottom: 15px;
}

:deep(.instructions-icon) {
  font-size: 14px !important;
}

.request-tabs {
  margin: 10px;
  min-height: 200px;
}

.other-config {
  padding: 15px 0px;
}

.ht-btn-add {
  border: 0px;
  margin-top: 10px;
  color: #783887;
  background-color: white;
}

.ms-tabs__nav-scroll :deep(.el-tabs__nav-wrap.is-top) {
  width: 100%;
}
</style>
