import {ELEMENT_TYPE} from "@/business/components/api/automation/scenario/Setting";
import {
  Assertions,
  ConstantTimer,
  Extract,
  IfController,
  JDBCProcessor,
  JSR223Processor,
  LoopController,
  PluginController,
  TransactionController
} from "@/business/components/api/definition/model/ApiTestModel";
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
      title: this_.$t('api_test.automation.transaction_controller'),
      show: this_.showButton("TransactionController"),
      titleColor: "#783887",
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
      title: this_.$t('api_test.definition.request.scenario_assertions'),
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

export function scenarioAssertion(data, node) {
  node.active = false;
  node.scenarioAss = true;
  let needAdd = true;
  data.forEach(data => {
    if (data.type === "Assertions") {
      data.active = true;
      data.scenarioAss = true;
      needAdd = false;
      return;
    }
  })
  if (needAdd) {
    data.splice(0, 0, node);
  }
}

export function setNode(_this, node) {
  if (_this.selectedTreeNode !== undefined) {
    if (_this.stepFilter.get("SpecialSteps").indexOf(_this.selectedTreeNode.type) !== -1) {
      if (_this.selectedNode.parent.data.hashTree) {
        //同级请求添加断言，添加到父级
        if (node.type === 'Assertions') {
          scenarioAssertion(_this.selectedNode.parent.data.hashTree, node);
        } else {
          _this.selectedNode.parent.data.hashTree.splice(_this.selectedTreeNode.index, 0, node);
        }
      } else {
        //没有父级的直接加到当前的置顶
        if (node.type === 'Assertions') {
          scenarioAssertion(_this.scenarioDefinition, node);
        } else {
          _this.scenarioDefinition.splice(_this.selectedTreeNode.index, 0, node);
        }
      }
      _this.$store.state.forceRerenderIndex = getUUID();
    } else {
      //选择场景时，直接添加到当前场景
      if (node.type === 'Assertions' && _this.selectedTreeNode.type === 'scenario' && _this.selectedTreeNode.referenced === 'Copy') {
        scenarioAssertion(_this.selectedTreeNode.hashTree, node);
      } else {
        _this.selectedTreeNode.hashTree.push(node);
      }
    }
  } else {
    if (node.type === 'Assertions') {
      scenarioAssertion(_this.scenarioDefinition, node);
    } else {
      _this.scenarioDefinition.push(node);
    }
  }
}

export function setComponent(type, _this, plugin) {
  switch (type) {
    case ELEMENT_TYPE.IfController:
      setNode(_this, new IfController());
      break;
    case ELEMENT_TYPE.ConstantTimer:
      setNode(_this, new ConstantTimer({label: "SCENARIO-REF-STEP"}));
      break;
    case ELEMENT_TYPE.JSR223Processor:
      setNode(_this, new JSR223Processor({label: "SCENARIO-REF-STEP"}));
      break;
    case ELEMENT_TYPE.JSR223PreProcessor:
      setNode(_this, new JSR223Processor({type: "JSR223PreProcessor", label: "SCENARIO-REF-STEP"}));
      break;
    case ELEMENT_TYPE.JSR223PostProcessor:
      setNode(_this, new JSR223Processor({type: "JSR223PostProcessor", label: "SCENARIO-REF-STEP"}));
      break;
    case ELEMENT_TYPE.JDBCPreProcessor:
      setNode(_this, new JDBCProcessor({type: "JDBCPreProcessor", label: "SCENARIO-REF-STEP"}));
      break;
    case ELEMENT_TYPE.JDBCPostProcessor:
      setNode(_this, new JDBCProcessor({type: "JDBCPostProcessor", label: "SCENARIO-REF-STEP"}));
      break;
    case ELEMENT_TYPE.Assertions:
      setNode(_this, new Assertions({label: "SCENARIO-REF-STEP", id: getUUID()}));
      break;
    case ELEMENT_TYPE.Extract:
      setNode(_this, new Extract({label: "SCENARIO-REF-STEP", id: getUUID()}));
      break;
    case ELEMENT_TYPE.CustomizeReq:
      _this.customizeRequest = {protocol: "HTTP", type: "API", hashTree: [], referenced: 'Created', active: false};
      _this.customizeVisible = true;
      break;
    case  ELEMENT_TYPE.LoopController:
      setNode(_this, new LoopController());
      break;
    case ELEMENT_TYPE.TransactionController:
      setNode(_this, new TransactionController());
      break;
    case ELEMENT_TYPE.scenario:
      _this.isBtnHide = true;
      _this.$refs.scenarioRelevance.open();
      break;
    default:
      setNode(_this, new PluginController({
        type: plugin.jmeterClazz,
        stepName: plugin.name,
        pluginId: plugin.scriptId
      }));
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
