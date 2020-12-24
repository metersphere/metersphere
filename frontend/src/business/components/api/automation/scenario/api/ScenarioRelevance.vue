<template>
  <el-dialog class="api-relevance" :title="'场景导入'"
             :visible.sync="dialogVisible"
             width="60%"
             :close-on-click-modal="false"
             top="50px">

    <ms-container>
      <ms-aside-container :enable-aside-hidden="false">
        <ms-api-scenario-module
          @nodeSelectEvent="nodeChange"
          @refreshTable="refresh"
          @setModuleOptions="setModuleOptions"
          @enableTrash="false"
          :is-read-only="true"
          ref="nodeTree"/>
      </ms-aside-container>

      <ms-main-container>
        <ms-api-scenario-list
          :select-node-ids="selectNodeIds"
          :referenced="true"
          :trash-enable="false"
          @selection="setData"
          ref="apiScenarioList"/>
      </ms-main-container>
    </ms-container>

    <template v-slot:footer>
      <el-button type="primary" @click="copy" @keydown.enter.native.prevent>复制</el-button>
      <el-button type="primary" @click="reference" @keydown.enter.native.prevent>引用</el-button>
    </template>

  </el-dialog>
</template>

<script>
    import ScenarioRelevanceCaseList from "./ScenarioRelevanceCaseList";
    import MsApiModule from "../../../definition/components/module/ApiModule";
    import MsContainer from "../../../../common/components/MsContainer";
    import MsAsideContainer from "../../../../common/components/MsAsideContainer";
    import MsMainContainer from "../../../../common/components/MsMainContainer";
    import ScenarioRelevanceApiList from "./ScenarioRelevanceApiList";
    import MsApiScenarioModule from "../ApiScenarioModule";
    import MsApiScenarioList from "../ApiScenarioList";
    import {getUUID} from "../../../../../../common/js/utils";
    export default {
      name: "ScenarioRelevance",
      components: {
        MsApiScenarioList,
        MsApiScenarioModule,
        MsMainContainer, MsAsideContainer, MsContainer},
      data() {
          return {
            dialogVisible: false,
            result: {},
            currentProtocol: null,
            selectNodeIds: [],
            moduleOptions: {},
            isApiListEnable: true,

            currentScenario: [],
            currentScenarioIds: [],
          }
      },
      methods: {
        reference() {
          let scenarios = [];
          if (!this.currentScenario || this.currentScenario.length < 1) {
            this.$emit('请选择场景');
            return;
          }
          this.currentScenario.forEach(item => {
            let obj = {id: item.id, name: item.name, type: "scenario", referenced: 'REF', resourceId: getUUID()};
            scenarios.push(obj);
          });
          this.$emit('save', scenarios);
          this.close();
        },
        copy() {
          let scenarios = [];
          if (!this.currentScenarioIds || this.currentScenarioIds.length < 1) {
            this.$emit('请选择场景');
            return;
          }
          this.result = this.$post("/api/automation/getApiScenarios/", this.currentScenarioIds, response => {
            if (response.data) {
              response.data.forEach(item => {
                let scenarioDefinition = JSON.parse(item.scenarioDefinition);
                let obj = {id: item.id, name: item.name, type: "scenario", referenced: 'Copy', resourceId: getUUID(), hashTree: scenarioDefinition.hashTree};
                scenarios.push(obj);
              });
              this.$emit('save', scenarios);
              this.close();
            }
          })
        },
        close() {
          this.refresh();
          this.dialogVisible = false;
        },
        open() {
          this.dialogVisible = true;
          if (this.$refs.apiScenarioList) {
            this.$refs.apiScenarioList.search();
          }
        },
        nodeChange(node, nodeIds, pNodes) {
          this.selectNodeIds = nodeIds;
        },
        handleProtocolChange(protocol) {
          this.currentProtocol = protocol;
        },
        setModuleOptions(data) {
          this.moduleOptions = data;
        },
        refresh() {
            this.$refs.apiScenarioList.search();
        },
        setData(data) {
          this.currentScenario = Array.from(data).map(row => row);
          this.currentScenarioIds = Array.from(data).map(row => row.id);
        },
      }
    }
</script>

<style scoped>

  .ms-aside-container {
    border: 0px;
  }

  .api-relevance >>> .el-dialog__body {
    padding: 10px 20px;
  }

</style>
