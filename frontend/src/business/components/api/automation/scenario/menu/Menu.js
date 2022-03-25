import {ELEMENT_TYPE} from "@/business/components/api/automation/scenario/Setting";
import {Assertions, ConstantTimer, Extract, IfController, JSR223Processor, JDBCProcessor, LoopController, TransactionController, PluginController} from "@/business/components/api/definition/model/ApiTestModel";
import {getUUID} from "@/common/js/utils";

export function buttons(this_) {
  let buttons = [
    {
      title: this_.$t('api_test.definition.request.extract_param'),
      show: this_.showButton("Extract"),
      titleColor: "#015478",
      titleBgColor: "#E6EEF2",
      icon: "colorize",
      click: () => {
        this_.addComponent('Extract')
      }
    },
    {
      title: this_.$t('api_test.definition.request.post_script'),
      show: this_.showButton("JSR223PostProcessor"),
      titleColor: "#783887",
      titleBgColor: "#F2ECF3",
      icon: "skip_next",
      click: () => {
        this_.addComponent('JSR223PostProcessor')
      }
    },
    {
      title: this_.$t('api_test.definition.request.pre_script'),
      show: this_.showButton("JSR223PreProcessor"),
      titleColor: "#B8741A",
      titleBgColor: "#F9F1EA",
      icon: "skip_previous",
      click: () => {
        this_.addComponent('JSR223PreProcessor')
      }
    },
    {
      title: this_.$t('api_test.definition.request.post_sql'),
      show: this_.showButton("JDBCPostProcessor"),
      titleColor: "#1483F6",
      titleBgColor: "#F2ECF3",
      icon: "skip_next",
      click: () => {
        this_.addComponent('JDBCPostProcessor')
      }
    },
    {
      title: this_.$t('api_test.definition.request.pre_sql'),
      show: this_.showButton("JDBCPreProcessor"),
      titleColor: "#FE6F71",
      titleBgColor: "#F9F1EA",
      icon: "skip_previous",
      click: () => {
        this_.addComponent('JDBCPreProcessor')
      }
    },
    {
      title: this_.$t('api_test.automation.customize_script'),
      show: this_.showButton("JSR223Processor"),
      titleColor: "#7B4D12",
      titleBgColor: "#F1EEE9",
      icon: "code",
      click: () => {
        this_.addComponent('JSR223Processor')
      }
    },
    {
      title: this_.$t('api_test.automation.if_controller'),
      show: this_.showButton("IfController"),
      titleColor: "#E6A23C",
      titleBgColor: "#FCF6EE",
      icon: "alt_route",
      click: () => {
        this_.addComponent('IfController')
      }
    },
    {
      title: this_.$t('api_test.automation.loop_controller'),
      show: this_.showButton("LoopController"),
      titleColor: "#02A7F0",
      titleBgColor: "#F4F4F5",
      icon: "next_plan",
      click: () => {
        this_.addComponent('LoopController')
      }
    },
    {
      title: this_.$t('api_test.automation.transcation_controller'),
      show: this_.showButton("TransactionController"),
      titleColor: "#6D317C",
      titleBgColor: "#F4F4F5",
      icon: "alt_route",
      click: () => {
        this_.addComponent('TransactionController')
      }
    },
    {
      title: this_.$t('api_test.automation.wait_controller'),
      show: this_.showButton("ConstantTimer"),
      titleColor: "#67C23A",
      titleBgColor: "#F2F9EE",
      icon: "access_time",
      click: () => {
        this_.addComponent('ConstantTimer')
      }
    },
    {
      title: this_.$t('api_test.definition.request.assertions_rule'),
      show: this_.showButton("Assertions"),
      titleColor: "#A30014",
      titleBgColor: "#F7E6E9",
      icon: "next_plan",
      click: () => {
        this_.addComponent('Assertions')
      }
    },
    {
      title: this_.$t('api_test.automation.customize_req'),
      show: this_.showButton("CustomizeReq"),
      titleColor: "#008080",
      titleBgColor: "#EBF2F2",
      icon: "tune",
      click: () => {
        this_.addComponent('CustomizeReq')
      }
    },
    {
      title: this_.$t('api_test.automation.scenario_import'),
      show: this_.showButton("scenario"),
      titleColor: "#606266",
      titleBgColor: "#F4F4F5",
      icon: "movie",
      click: () => {
        this_.addComponent('scenario')
      }
    },
    {
      title: this_.$t('api_test.automation.api_list_import'),
      show: this_.showButton("HTTPSamplerProxy", "DubboSampler", "JDBCSampler", "TCPSampler"),
      titleColor: "#F56C6C",
      titleBgColor: "#FCF1F1",
      icon: "api",
      click: this_.apiListImport
    }
  ];
  return buttons.filter(btn => btn.show);
}

export function setComponent(type, _this, plugin) {
  switch (type) {
    case ELEMENT_TYPE.IfController:
      _this.selectedTreeNode !== undefined ? _this.selectedTreeNode.hashTree.push(new IfController()) :
        _this.scenarioDefinition.push(new IfController());
      break;
    case ELEMENT_TYPE.ConstantTimer:
      _this.selectedTreeNode !== undefined ? _this.selectedTreeNode.hashTree.push(new ConstantTimer({label: "SCENARIO-REF-STEP"})) :
        _this.scenarioDefinition.push(new ConstantTimer());
      break;
    case ELEMENT_TYPE.JSR223Processor:
      _this.selectedTreeNode !== undefined ? _this.selectedTreeNode.hashTree.push(new JSR223Processor({label: "SCENARIO-REF-STEP"})) :
        _this.scenarioDefinition.push(new JSR223Processor());
      break;
    case ELEMENT_TYPE.JSR223PreProcessor:
      _this.selectedTreeNode !== undefined ? _this.selectedTreeNode.hashTree.push(new JSR223Processor({type: "JSR223PreProcessor", label: "SCENARIO-REF-STEP"})) :
        _this.scenarioDefinition.push(new JSR223Processor({type: "JSR223PreProcessor"}));
      break;
    case ELEMENT_TYPE.JSR223PostProcessor:
      _this.selectedTreeNode !== undefined ? _this.selectedTreeNode.hashTree.push(new JSR223Processor({type: "JSR223PostProcessor", label: "SCENARIO-REF-STEP"})) :
        _this.scenarioDefinition.push(new JSR223Processor({type: "JSR223PostProcessor"}));
      break;
    case ELEMENT_TYPE.JDBCPreProcessor:
      _this.selectedTreeNode !== undefined ? _this.selectedTreeNode.hashTree.push(new JDBCProcessor({type: "JDBCPreProcessor", label: "SCENARIO-REF-STEP"})) :
        _this.scenarioDefinition.push(new JDBCProcessor({type: "JDBCPreProcessor"}));
      break;
    case ELEMENT_TYPE.JDBCPostProcessor:
      _this.selectedTreeNode !== undefined ? _this.selectedTreeNode.hashTree.push(new JDBCProcessor({type: "JDBCPostProcessor", label: "SCENARIO-REF-STEP"})) :
        _this.scenarioDefinition.push(new JDBCProcessor({type: "JDBCPostProcessor"}));
      break;
    case ELEMENT_TYPE.Assertions:
      _this.selectedTreeNode !== undefined ? _this.selectedTreeNode.hashTree.push(new Assertions({label: "SCENARIO-REF-STEP", id: getUUID()})) :
        _this.scenarioDefinition.push(new Assertions());
      break;
    case ELEMENT_TYPE.Extract:
      _this.selectedTreeNode !== undefined ? _this.selectedTreeNode.hashTree.push(new Extract({label: "SCENARIO-REF-STEP", id: getUUID()})) :
        _this.scenarioDefinition.push(new Extract());
      break;
    case ELEMENT_TYPE.CustomizeReq:
      _this.customizeRequest = {protocol: "HTTP", type: "API", hashTree: [], referenced: 'Created', active: false};
      _this.customizeVisible = true;
      break;
    case  ELEMENT_TYPE.LoopController:
      _this.selectedTreeNode !== undefined ? _this.selectedTreeNode.hashTree.push(new LoopController()) :
        _this.scenarioDefinition.push(new LoopController());
      break;
    case ELEMENT_TYPE.TransactionController:
      _this.selectedTreeNode !== undefined ? _this.selectedTreeNode.hashTree.push(new TransactionController()) :
        _this.scenarioDefinition.push(new TransactionController());
      break;
    case ELEMENT_TYPE.scenario:
      _this.isBtnHide = true;
      _this.$refs.scenarioRelevance.open();
      break;
    default:
      _this.scenarioDefinition.push(new PluginController({type: plugin.jmeterClazz, stepName: plugin.name, pluginId: plugin.scriptId}));
      break;
  }
  if (_this.selectedNode) {
    _this.selectedNode.expanded = true;
  }
  _this.sort();
  _this.$nextTick(() => {
    _this.cancelBatchProcessing();
  });
}
