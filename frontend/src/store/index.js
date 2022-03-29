import Vue from 'vue'
import Vuex from 'vuex'
import getters from './getters'
import actions from './actions'
import mutations from './mutations'

Vue.use(Vuex)

const state = {
  projectId: "",
  test: {},
  scenarioJmxs: {},
  versionSwitch: "new",
  isReadOnly: true,
  theme: undefined,

  testCaseSelectNode: {},
  testCaseSelectNodeIds: [],
  testCaseModuleOptions: [],

  testReviewSelectNode: {},
  testReviewSelectNodeIds: [],
  useEnvironment: "",
  testPlanViewSelectNode: {},
  selectStep: {},
  currentApiCase: {},
  pluginFiles: [],
  isTestCaseMinderChanged: false,
  // 当前项目是否勾选自定义ID
  currentProjectIsCustomNum: false,
  testCaseTemplate: {},
  scenarioEnvMap: new Map(),
  apiMap: new Map(),
  apiStatus: new Map(),
  apiCaseMap: new Map(),
  testCaseMap: new Map(),
  curTabId: null,
  testCaseDefaultValue: {},
  forceRerenderIndex: "",
  currentScenario: {},
  scenarioDefinition: {},
  selectCommand: {},
  //按照type分组的指令
  groupedCmd: {},
  selectUiGroup: '',
  librarySelectElement: {},
  uiElementLibraryModuleIds: null,
  uiElementLibraryElements: null,
  refreshUiScenario: false,
  showLicenseCountWarning: false
}

const store = new Vuex.Store({
  state,
  getters,
  actions,
  mutations
})

export default store
