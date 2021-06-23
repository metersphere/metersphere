const mutations = {
  setProjectId: (state, projectId) => state.projectId = projectId,
  setTest: (state, test) => state.test = test,
  setScenarioJmxs: (state, scenarioJmxs) => state.scenarioJmxs = scenarioJmxs,
  clearTest: state => state.test = {},
  clearScenarioJmxs:state => state.scenarioJmxs = {},
  setVersionSwitch: (state, value) => state.versionSwitch = value,
  setTheme: (state, value) => state.theme = value,

  setTestCaseSelectNode: (state, value) => state.testCaseSelectNode = value,
  setTestCaseSelectNodeIds: (state, value) => state.testCaseSelectNodeIds = value,
  setTestCaseModuleOptions: (state, value) => state.testCaseModuleOptions = value,

  setTestReviewSelectNode: (state, value) => state.testReviewSelectNode = value,
  setTestReviewSelectNodeIds: (state, value) => state.testReviewSelectNodeIds = value,
  setTestReviewModuleOptions: (state, value) => state.testReviewModuleOptions = value,
  setTestPlanViewSelectNode: (state, value) => state.testPlanViewSelectNode = value,
}

export default mutations;
