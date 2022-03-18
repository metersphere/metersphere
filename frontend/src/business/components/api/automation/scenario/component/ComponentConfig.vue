<template>
  <div class="request-form">
    <component
      v-bind:is="component"
      :isMax="isMax"
      :show-btn="showBtn"
      :show-version="showVersion"
      :scenario="scenario"
      :controller="scenario"
      :timer="scenario"
      :assertions="scenario"
      :extract="scenario"
      :jsr223-processor="scenario"
      :request="scenario"
      :currentScenario="currentScenario"
      :currentEnvironmentId="currentEnvironmentId"
      :node="node"
      :draggable="draggable"
      :title="title"
      :color="titleColor"
      :response="response"
      :environment-type="environmentType"
      :environment-group-id="envGroupId"
      :background-color="backgroundColor"
      :project-list="projectList"
      :env-map="envMap"
      :message="message"
      :api-id="apiId"
      :scenario-definition="scenarioDefinition"
      :if-from-variable-advance="ifFromVariableAdvance"
      @suggestClick="suggestClick(node)"
      @remove="remove"
      @runScenario="runScenario"
      @stopScenario="stopScenario"
      @copyRow="copyRow"
      @refReload="refReload"
      @openScenario="openScenario"
      @setDomain="setDomain"
      @savePreParams="savePreParams"
      @editScenarioAdvance="editScenarioAdvance"
    />
  </div>
</template>

<script>
import MsIfController from "./IfController";
import MsTransactionController from "./TransactionController";
import {ELEMENT_TYPE} from "../Setting";
import MsApiComponent from "./ApiComponent";
import MsLoopController from "./LoopController";
import MsApiScenarioComponent from "./ApiScenarioComponent";
import JmeterElementComponent from "./JmeterElementComponent";
import PluginComponent from "./PluginComponent";

export default {
  name: "ComponentConfig",
  components: {
    PluginComponent,
    MsIfController,
    MsTransactionController,
    MsApiComponent,
    MsLoopController,
    MsApiScenarioComponent,
    JmeterElementComponent,
    MsConstantTimer: () => import("./ConstantTimer"),
    MsJsr233Processor: () => import("./Jsr233Processor"),
    MsApiAssertions: () => import("../../../definition/components/assertion/ApiAssertions"),
    MsApiExtract: () => import("../../../definition/components/extract/ApiExtract"),
    MsJdbcProcessor: () => import("@/business/components/api/automation/scenario/component/JDBCProcessor"),
  },
  props: {
    type: String,
    message: String,
    scenario: {},
    draggable: {
      type: Boolean,
      default: true,
    },
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
    currentScenario: {},
    currentEnvironmentId: String,
    response: {},
    node: {},
    projectList: Array,
    envMap: Map,
    environmentType: String,
    envGroupId: String,
    scenarioDefinition: Array,
    // 是否来自于接口自动化的参数设置
    ifFromVariableAdvance: {
      type: Boolean,
      default: false,
    },
  },
  data() {
    return {
      title: "",
      titleColor: "",
      backgroundColor: "",
      apiId: "",
    }
  },
  computed: {
    component({type}) {
      let name;
      switch (type) {
        case ELEMENT_TYPE.IfController:
          name = "MsIfController";
          break;
        case ELEMENT_TYPE.TransactionController:
          name = "MsTransactionController";
          break;
        case ELEMENT_TYPE.ConstantTimer:
          name = "MsConstantTimer";
          break;
        case ELEMENT_TYPE.JSR223Processor:
          name = this.getComponent(ELEMENT_TYPE.JSR223Processor);
          break;
        case ELEMENT_TYPE.JSR223PreProcessor:
          name = this.getComponent(ELEMENT_TYPE.JSR223PreProcessor);
          break;
        case ELEMENT_TYPE.JSR223PostProcessor:
          name = this.getComponent(ELEMENT_TYPE.JSR223PostProcessor);
          break;
        case ELEMENT_TYPE.JDBCPostProcessor:
          name = this.getComponent(ELEMENT_TYPE.JDBCPostProcessor);
          break;
        case ELEMENT_TYPE.JDBCPreProcessor:
          name = this.getComponent(ELEMENT_TYPE.JDBCPreProcessor);
          break;
        case ELEMENT_TYPE.Assertions:
          name = this.getComponent(ELEMENT_TYPE.Assertions);
          break;
        case ELEMENT_TYPE.Extract:
          name = "MsApiExtract";
          break;
        case ELEMENT_TYPE.CustomizeReq:
          name = "MsApiComponent";
          break;
        case  ELEMENT_TYPE.LoopController:
          name = "MsLoopController";
          break;
        case ELEMENT_TYPE.scenario:
          name = "MsApiScenarioComponent";
          break;
        case "AuthManager":
          break;
        case "JmeterElement":
          name = "JmeterElementComponent";
          break;
        case "DubboSampler":
          name = "MsApiComponent";
          break;
        case "HTTPSamplerProxy":
          name = "MsApiComponent";
          break;
        case "JDBCSampler":
          name = "MsApiComponent";
          break;
        case "TCPSampler":
          name = "MsApiComponent";
          break;
        default:
          name = this.getComponent(ELEMENT_TYPE.Plugin);
          break;
      }
      return name;
    }
  },
  methods: {
    getComponent(type) {
      if (type === ELEMENT_TYPE.JSR223PreProcessor) {
        this.title = this.$t('api_test.definition.request.pre_script');
        this.titleColor = "#b8741a";
        this.backgroundColor = "#F9F1EA";
        return "MsJsr233Processor";
      } else if (type === ELEMENT_TYPE.JSR223PostProcessor) {
        this.title = this.$t('api_test.definition.request.post_script');
        this.titleColor = "#783887";
        this.backgroundColor = "#F2ECF3";
        return "MsJsr233Processor";
      }
      if (type === ELEMENT_TYPE.JDBCPreProcessor) {
        this.title = this.$t('api_test.definition.request.pre_sql');
        this.titleColor = "#FE6F71";
        this.backgroundColor = "#F2ECF3";
        return "MsJdbcProcessor";
      } else if (type === ELEMENT_TYPE.JDBCPostProcessor) {
        this.title = this.$t('api_test.definition.request.post_sql');
        this.titleColor = "#1483F6";
        this.backgroundColor = "#F2ECF3";
        return "MsJdbcProcessor";
      } else if (type === ELEMENT_TYPE.Plugin) {
        this.titleColor = "#1483F6";
        this.backgroundColor = "#F2ECF3";
        return "PluginComponent";
      } else if (type === ELEMENT_TYPE.Assertions) {
        if (this.node && this.node.parent && this.node.parent.data && this.node.parent.data.referenced === "REF") {
          this.apiId = this.node.parent.data.id;
          this.scenario.document.nodeType = "scenario";
        } else {
          this.apiId = "none";
        }
        return "MsApiAssertions";
      } else {
        this.title = this.$t('api_test.automation.customize_script');
        this.titleColor = "#7B4D12";
        this.backgroundColor = "#F1EEE9";
        return "MsJsr233Processor";
      }
    },
    remove(row, node) {
      this.$emit('remove', row, node);
    },
    copyRow(row, node) {
      this.$emit('copyRow', row, node);

    },
    openScenario(data) {
      this.$emit('openScenario', data);
    },
    suggestClick(node) {
      this.$emit('suggestClick', node);
    },
    refReload(data, node) {
      this.$emit('refReload', data, node);
    },
    runScenario(scenario) {
      this.$emit('runScenario', scenario);
    },
    stopScenario() {
      this.$emit('stopScenario');
    },
    setDomain() {
      this.$emit("setDomain");
    },
    savePreParams(data) {
      this.$emit("savePreParams", data);
    },
    editScenarioAdvance(data) {
      this.$emit('editScenarioAdvance', data);
    },
  }
}
</script>

<style scoped>
.request-form >>> .debug-button {
  margin-left: auto;
  display: block;
  margin-right: 10px;
}

</style>
