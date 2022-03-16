<template>
  <div>
    <el-row v-if="!isReloadData">
      <el-radio-group v-model="request.reportType" size="mini" style="margin: 10px 0px;">
        <el-radio :disabled="isReadOnly" label="json" @change="changeReportType">
          json
        </el-radio>
        <el-radio :disabled="isReadOnly" label="xml" @change="changeReportType">
          xml
        </el-radio>
        <el-radio :disabled="isReadOnly" label="raw" @change="changeReportType">
          raw
        </el-radio>
      </el-radio-group>
      <div style="min-width: 1200px;" v-if="request.reportType === 'xml'">
        <tcp-xml-table :table-data="request.xmlDataStruct" :show-options-button="true" :show-operation-col="showOperationCol"
                       @xmlTablePushRow="xmlTablePushRow"
                       @initXmlTableData="initXmlTableData"
                       @saveTableData="saveXmlTableData" ref="treeTable"></tcp-xml-table>
      </div>
      <div style="min-width: 1200px;" v-if="request.reportType === 'json'">
        <div class="send-request">
          <ms-code-edit mode="json" :read-only="isReadOnly" :data.sync="request.jsonDataStruct" :modes="['text', 'json', 'xml', 'html']" theme="eclipse"/>
        </div>
      </div>
      <div style="min-width: 1200px;" v-if="request.reportType === 'raw'">
        <div class="send-request">
          <ms-code-edit mode="text" :read-only="isReadOnly" :data.sync="request.rawDataStruct" :modes="['text', 'json', 'xml', 'html']" theme="eclipse"/>
        </div>
      </div>
    </el-row>
  </div>
</template>

<script>
import MsApiKeyValue from "../../ApiKeyValue";
import MsApiAssertions from "../../assertion/ApiAssertions";
import MsApiExtract from "../../extract/ApiExtract";
import ApiRequestMethodSelect from "../../collapse/ApiRequestMethodSelect";
import MsCodeEdit from "../../../../../common/components/MsCodeEdit";
import MsApiScenarioVariables from "../../ApiScenarioVariables";
import {createComponent} from "../../jmeter/components";
import {Assertions, Extract} from "../../../model/ApiTestModel";
import {parseEnvironment} from "../../../model/EnvironmentModel";
import ApiEnvironmentConfig from "../../environment/ApiEnvironmentConfig";
import {API_STATUS} from "../../../model/JsonData";
import TCPSampler from "../../jmeter/components/sampler/tcp-sampler";
import {getCurrentProjectID, getUUID} from "@/common/js/utils";
import MsApiVariable from "../../ApiVariable";
import MsInstructionsIcon from "../../../../../common/components/MsInstructionsIcon";
import Jsr233ProcessorContent from "../../../../automation/scenario/common/Jsr233ProcessorContent";
import JSR223PreProcessor from "../../jmeter/components/pre-processors/jsr223-pre-processor";
import ApiDefinitionStepButton from "../components/ApiDefinitionStepButton";
import TcpXmlTable from "@/business/components/api/definition/components/complete/table/TcpXmlTable";


export default {
  name: "MsTcpParams",
  components: {
    TcpXmlTable,
    ApiDefinitionStepButton,
    Jsr233ProcessorContent,
    MsInstructionsIcon,
    MsApiVariable,
    MsApiScenarioVariables,
    MsCodeEdit,
    ApiRequestMethodSelect, MsApiExtract, MsApiAssertions, MsApiKeyValue, ApiEnvironmentConfig
  },
  props: {
    request: {},
    basisData: {},
    moduleOptions: Array,
    isReadOnly: {
      type: Boolean,
      default: false
    },
    showOperationCol: {
      type: Boolean,
      default: false
    },
    showScript: {
      type: Boolean,
      default: true,
    },
    referenced: {
      type: Boolean,
      default: false,
    },
  },
  data() {
    return {
      reportType:"raw",
      isReloadData: false,
      refreshedXmlTable:true,
      currentProjectId: "",
      rawDataStruct: "",
      jsonDataStruct: "",
    }
  },
  watch:{
    reportType(){
      this.request.reportType = this.reportType;
    }
  },
  created() {
    this.currentProjectId = getCurrentProjectID();
    if (!this.request.parameters) {
      this.$set(this.request, 'parameters', []);
      this.request.parameters = [];
    }
    if(!this.request.connectEncoding){
      this.request.connectEncoding = "UTF-8";
    }
    if(!this.request.reportType){
      this.request.reportType = 'raw';
    }
    if(!this.request.jsonDataStruct){
      this.request.jsonDataStruct = "";
    }
    if(!this.request.rawDataStruct){
      this.request.rawDataStruct = "";
    }
    if(!this.request.xmlDataStruct){
      this.request.xmlDataStruct = [];
    }

  },
  methods: {
    reload() {
      this.isReloadData = true
      this.$nextTick(() => {
        this.isReloadData = false
      })
    },
    runTest() {

    },
    changeReportType(){
    },
    getParmas(){
      return this.request;
    },

    //以下是xml树形表格相关的方法
    updateXmlTableData(dataStruct){
      this.request.xmlDataStruct = dataStruct;
    },
    saveXmlTableData(dataStruct){
      let valedataResult = this.validateXmlDataStruct(dataStruct);
      if(valedataResult){
        this.request.xmlDataStruct = dataStruct;
        this.refreshXmlTable();
      }
    },
    validateXmlDataStruct(){
      if(this.request.xmlDataStruct){
        this.refreshXmlTableDataStruct(this.request.xmlDataStruct);
        let result = this.checkXmlTableDataStructData(this.request.xmlDataStruct);
        return result;
      }
    },
    refreshXmlTableDataStruct(dataStruct){
      if(dataStruct && dataStruct.length > 0){
        dataStruct.forEach( row => {
          row.status = "";
          if(row.children == null || row.children.length === 0){
            row.children = [];
          }else if(row.children.length>0){
            this.refreshXmlTableDataStruct(row.children);
          }
        });
      }
    },
    saveData(){
      if(this.request && this.request.reportType === 'xml' && this.$refs.treeTable){
        this.$refs.treeTable.saveTableData();
      }
    },
    checkXmlTableDataStructData(dataStruct){
      let allCheckResult = true;
      if(this.$refs.treeTable){
        if(dataStruct && dataStruct.length > 0){
          for(let i = 0;i<dataStruct.length;i++){
            let row = dataStruct[i];
            allCheckResult = this.$refs.treeTable.validateRowData(row);
            if(allCheckResult){
              if(row.children != null && row.children.length > 0){
                allCheckResult = this.checkXmlTableDataStructData(row.children);
                if(!allCheckResult){
                  return false;
                }
              }
            }else{
              return false;
            }
          }
        }
      }
      return allCheckResult;
    },
    initXmlTableData(){
      if(this.request){
        this.request.xmlDataStruct = [];
        this.refreshXmlTable();
      }
    },
    xmlTableDataPushRow(newRow){
      if(this.request){
        if(!this.request.xmlDataStruct){
          this.request.xmlDataStruct = [];
        }
        this.request.xmlDataStruct.push(newRow);
        this.refreshXmlTable();
      }
    },
    xmlTableDataRemoveRow(row){
      if(this.request){
        if(this.request.xmlDataStruct){
          this.removeFromDataStruct(this.request.xmlDataStruct,row);
          this.refreshXmlTable();
        }
      }
    },
    removeFromDataStruct(dataStruct,row){
      if(!dataStruct || dataStruct.length === 0){
        return;
      }
      let rowIndex = dataStruct.indexOf(row);
      if(rowIndex >= 0){
        dataStruct.splice(rowIndex,1);
      }else {
        dataStruct.forEach( itemData => {
          if(!itemData.children && itemData.children.length > 0){
            this.removeFromDataStruct(itemData.children,row);
          }
        });
      }
    },
    refreshXmlTable(){
      this.refreshedXmlTable = true
      this.$nextTick(() => {
        this.refreshedXmlTable = false
      })
    },
    xmlTablePushRow(row){
      this.request.xmlDataStruct.push(row);
    }
  }

}
</script>

<style scoped>
.tcp >>> .el-input-number {
  width: 100%;
}

.send-request {
  padding: 0px 0;
  height: 300px;
  border: 1px #DCDFE6 solid;
  border-radius: 4px;
  width: 100%;
}

.ms-left-cell {
  margin-top: 40px;
}

.ms-left-buttion {
  margin: 6px 0px 8px 30px;
}

/deep/ .el-form-item {
  margin-bottom: 15px;
}

.ms-left-cell {
  margin-top: 40px;
}

.ms-left-buttion {
  margin: 6px 0px 8px 30px;
}

/deep/ .el-form-item {
  margin-bottom: 15px;
}

/deep/ .instructions-icon {
  font-size: 14px !important;
}

.request-tabs {
  margin: 10px;
  min-height: 200px;
}

.other-config {
  padding: 15px;
}
</style>
