import Vue from 'vue'
import Vuex from 'vuex'
import getters from './getters'
import actions from './actions'
import mutations from './mutations'

Vue.use(Vuex)

const state = {
  projectId: "",
  test: {},
  scenarioJmxs:{},
  versionSwitch: "new",
  isReadOnly: true,
  theme: undefined,

  testCaseSelectNode: {},
  testCaseSelectNodeIds: [],
  testCaseModuleOptions: [],

  testReviewSelectNode: {},
  testReviewSelectNodeIds: [],
  testReviewModuleOptions: [],

  testPlanViewSelectNode: {}

}

const store = new Vuex.Store({
  state,
  getters,
  actions,
  mutations
})

export default store
