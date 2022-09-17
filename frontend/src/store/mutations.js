const mutations = {
  setProjectId: (state, projectId) => state.projectId = projectId,
  setTest: (state, test) => state.test = test,
  setScenarioJmxs: (state, scenarioJmxs) => state.scenarioJmxs = scenarioJmxs,
  clearTest: state => state.test = {},
  clearScenarioJmxs: state => state.scenarioJmxs = {},
  setVersionSwitch: (state, value) => state.versionSwitch = value,
  setTheme: (state, value) => state.theme = value,

  setTestCaseSelectNode: (state, value) => state.testCaseSelectNode = value,
  setTestCaseSelectNodeIds: (state, value) => state.testCaseSelectNodeIds = value,
  setTestCaseModuleOptions: (state, value) => state.testCaseModuleOptions = value,

  setTestReviewSelectNode: (state, value) => state.testReviewSelectNode = value,
  setTestReviewSelectNodeIds: (state, value) => state.testReviewSelectNodeIds = value,
  setTestPlanViewSelectNode: (state, value) => state.testPlanViewSelectNode = value,
  setIsTestCaseMinderChanged: (state, value) => state.isTestCaseMinderChanged = value,
  setCurrentProjectIsCustomNum: (state, value) => state.currentProjectIsCustomNum = value,
  setTestCaseTemplate: (state, value) => state.testCaseTemplate = value,
  setApiTemplate: (state, value) => state.apiTemplate = value,
  setCurTabId: (state, value) => state.curTabId = value,
  setTestCaseDefaultValue: (state, value) => state.testCaseDefaultValue = value,
  setSelectCommand: (state, value) => state.selectCommand = value,
  setSelectStep: (state, value) => state.selectStep = value,
  setLibrarySelectElement: (state, value) => state.librarySelectElement = value,
  setCurrentScenario: (state, value) => state.currentScenario = value,
  setScenarioDefinition: (state, value) => state.scenarioDefinition = value,
  setGroupedCmd: (state, value) => state.groupedCmd = value,
  setUiElementLibraryModuleIds: (state, value) => state.uiElementLibraryModuleIds = value,
  setUiElementLibraryElements: (state, value) => state.uiElementLibraryElements = value,
  setRefreshUiScenario: (state, value) => state.refreshUiScenario = value,
  setSelectUiGroup: (state, value) => state.selectUiGroup = value,
  setShowLicenseCountWarning: (state, value) => state.showLicenseCountWarning = value,
  setTemWorkspaceId: (state, value) => state.temWorkspaceId = value,
  setDownloadFile: (state, downloadFile) => {
    // 实时更新下载进度条
    let loadingFile = state.fileDownloadList.find(item => item.id === downloadFile.id);
    if (loadingFile) {
      loadingFile.progress = downloadFile.progress;
    } else {
      state.fileDownloadList.push(downloadFile);
    }
  },
  deleteDownloadFile: (state, props) => {
    state.fileDownloadList.splice(state.fileDownloadList.findIndex(item => item.id === props), 1);
  },
  setAppFixed: (state, value) => state.appFixed = value,
}

export default mutations;
