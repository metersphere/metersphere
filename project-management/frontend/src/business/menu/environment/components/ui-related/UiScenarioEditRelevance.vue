<template>
  <test-case-relevance-base
    :dialog-title="$t('ui.import_by_list_label')"
    @setProject="setProject"
    ref="baseRelevance"
  >
    <template v-slot:aside>
      <ui-scenario-module
        v-if="currentType === 'scenario'"
        currentType="scenario"
        style="margin-top: 5px"
        @nodeSelectEvent="nodeChange"
        @refreshTable="refresh"
        @setModuleOptions="setModuleOptions"
        @enableTrash="false"
        :is-read-only="true"
        ref="nodeTree"
      />
      <ui-custom-command-module
        v-if="currentType === 'customCommand'"
        currentType="customCommand"
        style="margin-top: 5px"
        @customNodeChange="customNodeChange"
        @refreshTable="refresh"
        @setCustomModuleOptions="setCustomModuleOptions"
        @enableTrash="false"
        :is-read-only="true"
        ref="customNodeTree"
      />
    </template>

    <ui-scenario-list
      v-if="currentType === 'scenario'"
      currentType="scenario"
      :module-options="moduleOptions"
      :select-node-ids="selectNodeIds"
      :select-project-id="projectId"
      :trash-enable="false"
      :batch-operators="[]"
      :is-reference-table="true"
      @selection="setData"
      :is-relate="true"
      :init-ui-table-opretion="'init'"
      :custom-num="customNum"
      :showDrag="false"
      :mode="'import'"
      ref="uiScenarioList"
    >
      <toggle-tabs
        slot="tabChange"
        :activeDom.sync="currentType"
        :tabList="tabList"
      ></toggle-tabs>
    </ui-scenario-list>

    <ui-custom-command-list
      v-if="currentType === 'customCommand'"
      currentType="customCommand"
      :module-options="customModuleOptions"
      :select-node-ids="selectCustomNodeIds"
      :select-project-id="projectId"
      :trash-enable="false"
      :batch-operators="[]"
      :is-reference-table="true"
      @selection="setData"
      :is-relate="true"
      :init-ui-table-opretion="'init'"
      :custom-num="customNum"
      :showDrag="false"
      :mode="'import'"
      ref="uiCustomScenarioList"
    >
      <toggle-tabs
        slot="tabChange"
        :activeDom.sync="currentType"
        :tabList="tabList"
      ></toggle-tabs>
    </ui-custom-command-list>

    <template v-slot:headerBtn>
      <!-- todo 场景引用     -->
      <el-button
        type="primary"
        @click="reference"
        :loading="buttonIsWorking"
        @keydown.enter.native.prevent
        size="mini"
      >
        {{ $t("api_test.scenario.reference") }}
      </el-button>
    </template>
  </test-case-relevance-base>
</template>

<script>
import MsContainer from "metersphere-frontend/src/components/MsContainer";
import MsAsideContainer from "metersphere-frontend/src/components/MsAsideContainer";
import MsMainContainer from "metersphere-frontend/src/components/MsMainContainer";
import {hasLicense} from "metersphere-frontend/src/utils/permission.js";
import {getUUID} from 'metersphere-frontend/src/utils';
import RelevanceDialog from "metersphere-frontend/src/components/environment/snippet/ext/RelevanceDialog";
import TestCaseRelevanceBase from "metersphere-frontend/src/components/environment/snippet/ext/TestCaseRelevanceBase";
import UiScenarioModule from "./UiScenarioModule";
import UiScenarioList from "./UiScenarioList";
import ToggleTabs from "./ToggleTabs";
import UiCustomCommandList from "./UiCustomCommandList";
import UiCustomCommandModule from "./UiCustomCommandModule";
import {TYPE_TO_C} from "metersphere-frontend/src/model/Setting";

const VersionSelect = {};

export default {
  name: "UiScenarioEditRelevance",
  components: {
    UiScenarioModule,
    VersionSelect: VersionSelect.default,
    TestCaseRelevanceBase,
    RelevanceDialog,
    UiScenarioList,
    MsMainContainer,
    MsAsideContainer,
    MsContainer,
    ToggleTabs,
    UiCustomCommandList,
    UiCustomCommandModule,
  },
  props: {
    scenarioType: String,
  },
  data() {
    return {
      currentType: "scenario",
      buttonIsWorking: false,
      result: {},
      currentProtocol: null,
      selectNodeIds: [],
      selectCustomNodeIds: [],
      moduleOptions: [],
      customModuleOptions: [],
      isApiListEnable: true,
      currentScenario: [],
      currentScenarioIds: [],
      projectId: "",
      customNum: false,
      versionOptions: [],
      currentVersion: "",
      versionEnable: true,
      tabList: [
        {
          domKey: "scenario",
          tip: this.$t("ui.scenario_title"),
          content: this.$t("ui.scenario_title"),
          placement: "left",
          enable: true,
        },
        {
          domKey: "customCommand",
          tip: this.$t("ui.custom_command_title"),
          content: this.$t("ui.custom_command_title"),
          placement: "right",
          enable: true,
        },
      ],
    };
  },
  watch: {
    projectId(val) {
      this.listByType(val);
    },
    scenarioType(val) {
      this.currentType = this.scenarioType;
    },
    currentType(val) {
      this.listByType(this.projectId);
      this.searchByType();
    },
  },
  computed: {
    isScenario() {
      return this.currentType === "scenario";
    },
  },
  methods: {
    listByType(projectId) {
      this.$nextTick(() => {
        this.isScenario
          ? this.$refs.nodeTree.list(this.projectId)
          : this.$refs.customNodeTree.list(this.projectId);
      });
    },
    changeButtonLoadingType() {
      this.buttonIsWorking = false;
    },
    createScenarioDefinition(scenarios, data, referenced) {
      let errArr = [];
      for (let item of data) {
        let scenarioDefinition = JSON.parse(item.scenarioDefinition);
        if (!scenarioDefinition) {
          continue;
        }
        if (
          !scenarioDefinition.hashTree ||
          scenarioDefinition.hashTree.length <= 0
        ) {
          errArr.push(scenarioDefinition.name);
          continue;
        }
        if (scenarioDefinition && scenarioDefinition.hashTree) {
          //处理scenarioDefinition
          this.handleScenarioDefinition(scenarioDefinition);

          let variables = scenarioDefinition.variables
            ? scenarioDefinition.variables.filter((v) => {
              return v.name;
            })
            : [];

          if (item.scenarioType === "customCommand" && item.commandViewStruct) {
            let innerVariables = this.handleInnerVariables(
              item.commandViewStruct
            );
            if (innerVariables) {
              variables = innerVariables;
            }
          }
          let outputVariables = this.handleOutputVariables(
            item.commandViewStruct
          );
          let obj = {
            //场景的id 存入resourceId中 避免el-tree key重复
            id: getUUID(),
            name: item.name,
            type: this.currentType,
            variables: this.resetInnerVariable(variables),
            outputVariables: this.resetOutVariable(outputVariables),
            environmentMap: scenarioDefinition.environmentMap,
            referenced: referenced,
            resourceId: item.id,
            hashTree: scenarioDefinition.hashTree,
            projectId: item.projectId,
            num: item.num,
            versionName: item.versionName,
            versionEnable: item.versionEnable,
            description: item.description,
            clazzName: this.isScenario
              ? TYPE_TO_C.get("UiScenario")
              : TYPE_TO_C.get("customCommand"),
          };
          scenarios.push(obj);
        }
      }

      if (errArr.length > 0) {
        let msg = errArr.join("、");
        if (msg && msg.length > 30) {
          msg = msg.substring(0, 30) + "...";
        }
        let str = this.isScenario
          ? this.$t("ui.scenario")
          : this.$t("ui.instruction");
        this.$error(msg + " " + str + "为空，导入失败");
      }

      if (referenced === "REF") {
        //
        if (this.isScenario) {
          //场景
          this.$EventBus.$emit("handleScenarioREFEvent")
        }
        //指令
        else {
          this.$EventBus.$emit("handleCustomCommandREFEvent")
        }
      }
    },
    resetInnerVariable(vars) {
      if (vars && Array.isArray(vars)) {
        vars.forEach((v) => {
          v.id = getUUID();
        });
      }
      return vars;
    },
    resetOutVariable(vars) {
      if (vars && Array.isArray(vars)) {
        vars.forEach((v) => {
          v.id = getUUID();
        });
      }
      return vars;
    },
    handleInnerVariables(commandViewStruct) {
      if (!commandViewStruct) {
        return [];
      }
      let struct = JSON.parse(commandViewStruct);
      if (struct && struct.length > 1) {
        return struct[1].variables;
      }
      return [];
    },
    handleOutputVariables(commandViewStruct) {
      if (!commandViewStruct) {
        return [];
      }

      let struct = JSON.parse(commandViewStruct);
      if (struct && struct.length > 1) {
        let cur = struct[1].outputVariables;
        if (cur) {
          return cur;
        }
      }

      return [];
    },
    handleScenarioDefinition(scenarioDefinition) {
      if (!scenarioDefinition || !scenarioDefinition.hashTree || scenarioDefinition.hashTree.length <= 0) {
        return;
      }
      scenarioDefinition.resourceId = scenarioDefinition.id || getUUID();
      scenarioDefinition.id = getUUID();
      //重置 出参
      if (scenarioDefinition.outputVariables) {
        scenarioDefinition.outputVariables = "";
      }

      for (let item of scenarioDefinition.hashTree) {
        item.id = getUUID();
        //重置 出参
        if (item.outputVariables) {
          item.outputVariables = "";
        }
        if (item.hashTree) {
          this.handleScenarioDefinition(item);
        }
      }
    },
    reference() {
      this.buttonIsWorking = true;
      let selectIds = this.scenarioType == "scenario" ? this.$refs.uiScenarioList.getSelectRows() : this.$refs.uiCustomScenarioList.getSelectRows();
      if (!selectIds || selectIds.size == 0 || selectIds.size > 1) {
        this.$warning('请(只)选择一个场景/指令作为获取 cookie 的流程!');
        return;
      }
      this.$emit("reference", selectIds);
      this.buttonIsWorking = false;
    },
    close() {
      this.$refs.baseRelevance.close();
    },
    open() {
      this.buttonIsWorking = false;
      this.$refs.baseRelevance.open();
      this.searchByType();
    },

    /**
     * common opt
     */
    searchByType() {
      this.$nextTick(() => {
        this.isScenario
          ? this.$refs.uiScenarioList.search(this.projectId)
          : this.$refs.uiCustomScenarioList.search(this.projectId);
      });
    },
    nodeChange(node, nodeIds, pNodes) {
      this.selectNodeIds = nodeIds;
    },
    customNodeChange(node, nodeIds, pNodes) {
      this.selectCustomNodeIds = nodeIds;
    },
    handleProtocolChange(protocol) {
      this.currentProtocol = protocol;
    },
    setModuleOptions(data) {
      this.moduleOptions = data;
    },
    setCustomModuleOptions(data) {
      this.customModuleOptions = data;
    },
    refresh() {
      this.searchByType();
    },
    setData(data) {
      this.currentScenario = Array.from(data).map((row) => row);
      this.currentScenarioIds = Array.from(data).map((row) => row.id);
    },
    setProject(projectId) {
      this.projectId = projectId;
      this.selectNodeIds = [];
      this.selectCustomNodeIds = [];
    },
    getConditions() {
      return this.getConditionsByType();
    },
    getConditionsByType() {
      try {
        if (this.isScenario) {
          return this.$refs.uiScenarioList.getConditions();
        }
        return this.$refs.uiCustomScenarioList.getConditions();
      } catch (e) {
        return {};
      }
    },
    getVersionOptionList(projectId) {
      if (hasLicense()) {
        this.$get("/project/version/get-project-versions/" + projectId).then(
          (response) => {
            this.versionOptions = response.data;
          }
        );
      }
    },
    changeVersion(currentVersion) {
      if (this.$refs.uiScenarioList) {
        this.$refs.uiScenarioList.condition.versionId = currentVersion || null;
      }
      this.refresh();
    },
    checkVersionEnable(projectId) {
      if (!projectId) {
        return;
      }
      if (hasLicense()) {
        this.$get("/project/version/enable/" + projectId).then((response) => {
          this.versionEnable = false;
          this.$nextTick(() => {
            this.versionEnable = true;
          });
        });
      }
    },
  },
};
</script>

<style scoped></style>
