import {ELEMENT_TYPE} from "@/business/components/api/automation/scenario/Setting";
import {Assertions, ConstantTimer, Extract, IfController, JSR223Processor, JDBCProcessor,LoopController, TransactionController} from "@/business/components/api/definition/model/ApiTestModel";

export function buttons() {
  let buttons = [
    {
      title: this.$t('api_test.definition.request.extract_param'),
      show: this.showButton("Extract"),
      titleColor: "#015478",
      titleBgColor: "#E6EEF2",
      icon: "colorize",
      click: () => {
        this.addComponent('Extract')
      }
    },
    {
      title: this.$t('api_test.definition.request.post_script'),
      show: this.showButton("JSR223PostProcessor"),
      titleColor: "#783887",
      titleBgColor: "#F2ECF3",
      icon: "skip_next",
      click: () => {
        this.addComponent('JSR223PostProcessor')
      }
    },
    {
      title: this.$t('api_test.definition.request.pre_script'),
      show: this.showButton("JSR223PreProcessor"),
      titleColor: "#B8741A",
      titleBgColor: "#F9F1EA",
      icon: "skip_previous",
      click: () => {
        this.addComponent('JSR223PreProcessor')
      }
    },
    {
      title: this.$t('api_test.definition.request.post_sql'),
      show: this.showButton("JDBCPostProcessor"),
      titleColor: "#1483F6",
      titleBgColor: "#F2ECF3",
      icon: "skip_next",
      click: () => {
        this.addComponent('JDBCPostProcessor')
      }
    },
    {
      title: this.$t('api_test.definition.request.pre_sql'),
      show: this.showButton("JDBCPreProcessor"),
      titleColor: "#FE6F71",
      titleBgColor: "#F9F1EA",
      icon: "skip_previous",
      click: () => {
        this.addComponent('JDBCPreProcessor')
      }
    },
    {
      title: this.$t('api_test.automation.customize_script'),
      show: this.showButton("JSR223Processor"),
      titleColor: "#7B4D12",
      titleBgColor: "#F1EEE9",
      icon: "code",
      click: () => {
        this.addComponent('JSR223Processor')
      }
    },
    {
      title: this.$t('api_test.automation.if_controller'),
      show: this.showButton("IfController"),
      titleColor: "#E6A23C",
      titleBgColor: "#FCF6EE",
      icon: "alt_route",
      click: () => {
        this.addComponent('IfController')
      }
    },
    {
      title: this.$t('api_test.automation.loop_controller'),
      show: this.showButton("LoopController"),
      titleColor: "#02A7F0",
      titleBgColor: "#F4F4F5",
      icon: "next_plan",
      click: () => {
        this.addComponent('LoopController')
      }
    },
    {
      title: this.$t('api_test.automation.transcation_controller'),
      show: this.showButton("TransactionController"),
      titleColor: "#6D317C",
      titleBgColor: "#F4F4F5",
      icon: "alt_route",
      click: () => {
        this.addComponent('TransactionController')
      }
    },
    {
      title: this.$t('api_test.automation.wait_controller'),
      show: this.showButton("ConstantTimer"),
      titleColor: "#67C23A",
      titleBgColor: "#F2F9EE",
      icon: "access_time",
      click: () => {
        this.addComponent('ConstantTimer')
      }
    },
    {
      title: this.$t('api_test.definition.request.assertions_rule'),
      show: this.showButton("Assertions"),
      titleColor: "#A30014",
      titleBgColor: "#F7E6E9",
      icon: "next_plan",
      click: () => {
        this.addComponent('Assertions')
      }
    },
    {
      title: this.$t('api_test.automation.customize_req'),
      show: this.showButton("CustomizeReq"),
      titleColor: "#008080",
      titleBgColor: "#EBF2F2",
      icon: "tune",
      click: () => {
        this.addComponent('CustomizeReq')
      }
    },
    {
      title: this.$t('api_test.automation.scenario_import'),
      show: this.showButton("scenario"),
      titleColor: "#606266",
      titleBgColor: "#F4F4F5",
      icon: "movie",
      click: () => {
        this.addComponent('scenario')
      }
    },
    {
      title: this.$t('api_test.automation.api_list_import'),
      show: this.showButton("HTTPSamplerProxy", "DubboSampler", "JDBCSampler", "TCPSampler"),
      titleColor: "#F56C6C",
      titleBgColor: "#FCF1F1",
      icon: "api",
      click: this.apiListImport
    }
  ];
  return buttons.filter(btn => btn.show);
}

export function setComponent(type, _this) {
  switch (type) {
    case ELEMENT_TYPE.IfController:
      _this.selectedTreeNode !== undefined ? _this.selectedTreeNode.hashTree.push(new IfController()) :
        _this.scenarioDefinition.push(new IfController());
      break;
    case ELEMENT_TYPE.ConstantTimer:
      _this.selectedTreeNode !== undefined ? _this.selectedTreeNode.hashTree.push(new ConstantTimer()) :
        _this.scenarioDefinition.push(new ConstantTimer());
      break;
    case ELEMENT_TYPE.JSR223Processor:
      _this.selectedTreeNode !== undefined ? _this.selectedTreeNode.hashTree.push(new JSR223Processor()) :
        _this.scenarioDefinition.push(new JSR223Processor());
      break;
    case ELEMENT_TYPE.JSR223PreProcessor:
      _this.selectedTreeNode !== undefined ? _this.selectedTreeNode.hashTree.push(new JSR223Processor({type: "JSR223PreProcessor"})) :
        _this.scenarioDefinition.push(new JSR223Processor({type: "JSR223PreProcessor"}));
      break;
    case ELEMENT_TYPE.JSR223PostProcessor:
      _this.selectedTreeNode !== undefined ? _this.selectedTreeNode.hashTree.push(new JSR223Processor({type: "JSR223PostProcessor"})) :
        _this.scenarioDefinition.push(new JSR223Processor({type: "JSR223PostProcessor"}));
      break;
    case ELEMENT_TYPE.JDBCPreProcessor:
      _this.selectedTreeNode !== undefined ? _this.selectedTreeNode.hashTree.push(new JDBCProcessor({type: "JDBCPreProcessor"})) :
        _this.scenarioDefinition.push(new JDBCProcessor({type: "JDBCPreProcessor"}));
      break;
    case ELEMENT_TYPE.JDBCPostProcessor:
      _this.selectedTreeNode !== undefined ? _this.selectedTreeNode.hashTree.push(new JDBCProcessor({type: "JDBCPostProcessor"})) :
        _this.scenarioDefinition.push(new JDBCProcessor({type: "JDBCPostProcessor"}));
      break;
    case ELEMENT_TYPE.Assertions:
      _this.selectedTreeNode !== undefined ? _this.selectedTreeNode.hashTree.push(new Assertions()) :
        _this.scenarioDefinition.push(new Assertions());
      break;
    case ELEMENT_TYPE.Extract:
      _this.selectedTreeNode !== undefined ? _this.selectedTreeNode.hashTree.push(new Extract()) :
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
      _this.$refs.apiImport.open();
      break;
  }
  if (_this.selectedNode) {
    _this.selectedNode.expanded = true;
  }
  _this.sort();
}
