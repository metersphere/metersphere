<template>
  <div>
    <vue-lazy-component :viewport="root" v-if="type === 'IfController'">
      <ms-if-controller :isMax="isMax" :show-btn="showBtn" :expandedNode="expandedNode"
                        :scenario="scenario" :controller="scenario" :timer="scenario" :assertions="scenario" :extract="scenario" :jsr223-processor="scenario" :request="scenario" :currentScenario="currentScenario" :currentEnvironmentId="currentEnvironmentId" :node="node"
                        :draggable="draggable" :title="title" :color="titleColor" :background-color="backgroundColor" @suggestClick="suggestClick(node)" :response="response"
                        @remove="remove" @copyRow="copyRow" @refReload="refReload" @openScenario="openScenario" :project-list="projectList" :env-map="envMap"/>
      <component-skeleton slot="skeleton" :name="scenario.name"/>
    </vue-lazy-component>

    <vue-lazy-component :viewport="root" v-else-if="type === 'JmeterElement'">
      <jmeter-element-component :isMax="isMax" :show-btn="showBtn" :expandedNode="expandedNode"
                                :scenario="scenario" :controller="scenario" :timer="scenario" :assertions="scenario" :extract="scenario" :jsr223-processor="scenario" :request="scenario" :currentScenario="currentScenario" :currentEnvironmentId="currentEnvironmentId" :node="node"
                                :draggable="draggable" :title="title" :color="titleColor" :background-color="backgroundColor" @suggestClick="suggestClick(node)" :response="response"
                                @remove="remove" @copyRow="copyRow" @refReload="refReload" @openScenario="openScenario" :project-list="projectList" :env-map="envMap"/>
      <component-skeleton slot="skeleton"/>
    </vue-lazy-component>

    <vue-lazy-component :viewport="root" v-else-if="type === 'TransactionController'">
      <ms-transaction-controller :isMax="isMax" :show-btn="showBtn" :expandedNode="expandedNode"
                                 :scenario="scenario" :controller="scenario" :timer="scenario" :assertions="scenario" :extract="scenario" :jsr223-processor="scenario" :request="scenario" :currentScenario="currentScenario" :currentEnvironmentId="currentEnvironmentId"
                                 :node="node"
                                 :draggable="draggable" :title="title" :color="titleColor" :background-color="backgroundColor" @suggestClick="suggestClick(node)" :response="response"
                                 @remove="remove" @copyRow="copyRow" @refReload="refReload" @openScenario="openScenario" :project-list="projectList" :env-map="envMap"/>
      <component-skeleton slot="skeleton" :name="scenario.name"/>
    </vue-lazy-component>

    <vue-lazy-component :viewport="root" v-else-if="type === 'ConstantTimer'">
      <ms-constant-timer :isMax="isMax" :show-btn="showBtn" :expandedNode="expandedNode"
                         :scenario="scenario" :controller="scenario" :timer="scenario" :assertions="scenario" :extract="scenario" :jsr223-processor="scenario" :request="scenario" :currentScenario="currentScenario" :currentEnvironmentId="currentEnvironmentId" :node="node"
                         :draggable="draggable" :title="title" :color="titleColor" :background-color="backgroundColor" @suggestClick="suggestClick(node)" :response="response"
                         @remove="remove" @copyRow="copyRow" @refReload="refReload" @openScenario="openScenario" :project-list="projectList" :env-map="envMap"/>
      <component-skeleton slot="skeleton" :name="scenario.name"/>
    </vue-lazy-component>


    <vue-lazy-component :viewport="root" v-else-if="getComponent(type)">
      <ms-jsr233-processor :isMax="isMax" :show-btn="showBtn" :expandedNode="expandedNode"
                           :scenario="scenario" :controller="scenario" :timer="scenario" :assertions="scenario" :extract="scenario" :jsr223-processor="scenario" :request="scenario" :currentScenario="currentScenario" :currentEnvironmentId="currentEnvironmentId" :node="node"
                           :draggable="draggable" :title="title" :color="titleColor" :background-color="backgroundColor" @suggestClick="suggestClick(node)" :response="response"
                           @remove="remove" @copyRow="copyRow" @refReload="refReload" @openScenario="openScenario" :project-list="projectList" :env-map="envMap"/>
      <component-skeleton slot="skeleton" :name="scenario.name"/>
    </vue-lazy-component>

    <vue-lazy-component :viewport="root" v-else-if="getComponent(type)">
      <ms-jsr233-processor :isMax="isMax" :show-btn="showBtn" :expandedNode="expandedNode"
                           :scenario="scenario" :controller="scenario" :timer="scenario" :assertions="scenario" :extract="scenario" :jsr223-processor="scenario" :request="scenario" :currentScenario="currentScenario" :currentEnvironmentId="currentEnvironmentId"
                           :node="node"
                           :draggable="draggable" :title="title" :color="titleColor" :background-color="backgroundColor" @suggestClick="suggestClick(node)" :response="response"
                           @remove="remove" @copyRow="copyRow" @refReload="refReload" @openScenario="openScenario" :project-list="projectList" :env-map="envMap"/>
      <component-skeleton slot="skeleton" :name="scenario.name"/>
    </vue-lazy-component>

    <vue-lazy-component :viewport="root" v-else-if="getComponent(type)">
      <ms-jsr233-processor :isMax="isMax" :show-btn="showBtn" :expandedNode="expandedNode"
                           :scenario="scenario" :controller="scenario" :timer="scenario" :assertions="scenario" :extract="scenario" :jsr223-processor="scenario" :request="scenario" :currentScenario="currentScenario" :currentEnvironmentId="currentEnvironmentId"
                           :node="node"
                           :draggable="draggable" :title="title" :color="titleColor" :background-color="backgroundColor" @suggestClick="suggestClick(node)" :response="response"
                           @remove="remove" @copyRow="copyRow" @refReload="refReload" @openScenario="openScenario" :project-list="projectList" :env-map="envMap"/>
      <component-skeleton slot="skeleton" :name="scenario.name"/>
    </vue-lazy-component>

    <vue-lazy-component :viewport="root" v-else-if="type === 'Assertions'">
      <ms-api-assertions :isMax="isMax" :show-btn="showBtn" :expandedNode="expandedNode"
                         :scenario="scenario" :controller="scenario" :timer="scenario" :assertions="scenario" :extract="scenario" :jsr223-processor="scenario" :request="scenario" :currentScenario="currentScenario" :currentEnvironmentId="currentEnvironmentId"
                         :node="node"
                         :draggable="draggable" :title="title" :color="titleColor" :background-color="backgroundColor" @suggestClick="suggestClick(node)" :response="response"
                         @remove="remove" @copyRow="copyRow" @refReload="refReload" @openScenario="openScenario" :project-list="projectList" :env-map="envMap"/>
      <component-skeleton slot="skeleton" :name="scenario.name"/>
    </vue-lazy-component>

    <vue-lazy-component :viewport="root" v-else-if="type === 'Extract'">
      <ms-api-extract :isMax="isMax" :show-btn="showBtn" :expandedNode="expandedNode"
                      :scenario="scenario" :controller="scenario" :timer="scenario" :assertions="scenario" :extract="scenario" :jsr223-processor="scenario" :request="scenario" :currentScenario="currentScenario" :currentEnvironmentId="currentEnvironmentId"
                      :node="node"
                      :draggable="draggable" :title="title" :color="titleColor" :background-color="backgroundColor" @suggestClick="suggestClick(node)" :response="response"
                      @remove="remove" @copyRow="copyRow" @refReload="refReload" @openScenario="openScenario" :project-list="projectList" :env-map="envMap"/>
      <component-skeleton slot="skeleton" :name="scenario.name"/>
    </vue-lazy-component>


    <vue-lazy-component :viewport="root" v-else-if="type === 'CustomizeReq'">
      <ms-api-component :isMax="isMax" :show-btn="showBtn" :expandedNode="expandedNode"
                        :scenario="scenario" :controller="scenario" :timer="scenario" :assertions="scenario" :extract="scenario" :jsr223-processor="scenario" :request="scenario" :currentScenario="currentScenario" :currentEnvironmentId="currentEnvironmentId"
                        :node="node"
                        :draggable="draggable" :title="title" :color="titleColor" :background-color="backgroundColor" @suggestClick="suggestClick(node)" :response="response"
                        @remove="remove" @copyRow="copyRow" @refReload="refReload" @openScenario="openScenario" :project-list="projectList" :env-map="envMap"/>
      <component-skeleton slot="skeleton" :name="scenario.name"/>
    </vue-lazy-component>


    <vue-lazy-component :viewport="root" v-else-if="type === 'LoopController'">
      <ms-loop-controller :isMax="isMax" :show-btn="showBtn" :expandedNode="expandedNode"
                          :scenario="scenario" :controller="scenario" :timer="scenario" :assertions="scenario" :extract="scenario" :jsr223-processor="scenario" :request="scenario" :currentScenario="currentScenario" :currentEnvironmentId="currentEnvironmentId"
                          :node="node"
                          :draggable="draggable" :title="title" :color="titleColor" :background-color="backgroundColor" @suggestClick="suggestClick(node)" :response="response"
                          @remove="remove" @copyRow="copyRow" @refReload="refReload" @openScenario="openScenario" :project-list="projectList" :env-map="envMap"/>
      <component-skeleton slot="skeleton" :name="scenario.name"/>
    </vue-lazy-component>

    <vue-lazy-component :viewport="root" v-else-if="type === 'scenario'">
      <ms-api-scenario-component :isMax="isMax" :show-btn="showBtn" :expandedNode="expandedNode"
                                 :scenario="scenario" :controller="scenario" :timer="scenario" :assertions="scenario" :extract="scenario" :jsr223-processor="scenario" :request="scenario" :currentScenario="currentScenario" :currentEnvironmentId="currentEnvironmentId"
                                 :node="node"
                                 :draggable="draggable" :title="title" :color="titleColor" :background-color="backgroundColor" @suggestClick="suggestClick(node)" :response="response"
                                 @remove="remove" @copyRow="copyRow" @refReload="refReload" @openScenario="openScenario" :project-list="projectList" :env-map="envMap"/>
      <component-skeleton slot="skeleton" :name="scenario.name"/>
    </vue-lazy-component>

    <vue-lazy-component :viewport="root" v-else>
      <ms-api-component :isMax="isMax" :show-btn="showBtn" :expandedNode="expandedNode"
                        :scenario="scenario" :controller="scenario" :timer="scenario" :assertions="scenario" :extract="scenario" :jsr223-processor="scenario" :request="scenario" :currentScenario="currentScenario" :currentEnvironmentId="currentEnvironmentId"
                        :node="node"
                        :draggable="draggable" :title="title" :color="titleColor" :background-color="backgroundColor" @suggestClick="suggestClick(node)" :response="response"
                        @remove="remove" @copyRow="copyRow" @refReload="refReload" @openScenario="openScenario" :project-list="projectList" :env-map="envMap"/>
      <component-skeleton slot="skeleton" :name="scenario.name"/>
    </vue-lazy-component>

  </div>
</template>

<script>
import MsConstantTimer from "./ConstantTimer";
import MsIfController from "./IfController";
import MsTransactionController from "./TransactionController";
import {ELEMENT_TYPE} from "../Setting";
import MsJsr233Processor from "./Jsr233Processor";
import MsApiAssertions from "../../../definition/components/assertion/ApiAssertions";
import MsApiExtract from "../../../definition/components/extract/ApiExtract";
import MsApiComponent from "./ApiComponent";
import MsLoopController from "./LoopController";
import MsApiScenarioComponent from "./ApiScenarioComponent";
import JmeterElementComponent from "./JmeterElementComponent";
import ComponentSkeleton from "./ComponentSkeleton";
import {component as VueLazyComponent} from '@xunlei/vue-lazy-component'

export default {
  name: "ComponentConfig",
  components: {MsConstantTimer, MsIfController, MsTransactionController, MsJsr233Processor, MsApiAssertions, MsApiExtract, MsApiComponent, MsLoopController, MsApiScenarioComponent, JmeterElementComponent, VueLazyComponent, ComponentSkeleton},
  props: {
    type: String,
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
    currentScenario: {},
    expandedNode: Array,
    currentEnvironmentId: String,
    response: {},
    node: {},
    projectList: Array,
    envMap: Map
  },
  data() {
    return {
      title: "",
      titleColor: "",
      backgroundColor: "",
      root: null
    }
  },
  mounted () {
    this.root = this.$parent.$el
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
        case ELEMENT_TYPE.Assertions:
          name = "MsApiAssertions";
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
        default:
          name = "MsApiComponent";
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
        return true;
      } else if (type === ELEMENT_TYPE.JSR223PostProcessor) {
        this.title = this.$t('api_test.definition.request.post_script');
        this.titleColor = "#783887";
        this.backgroundColor = "#F2ECF3";
        return true;
      } else if (type === ELEMENT_TYPE.JSR223Processor) {
        this.title = this.$t('api_test.automation.customize_script');
        this.titleColor = "#7B4D12";
        this.backgroundColor = "#F1EEE9";
        return true;
      }
      return false;
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
    }
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
