export default {
  id: "TRACK",
  state: () => ({
    theme: undefined,
    testCaseSelectNode: {},
    testCaseSelectNodeIds: [],
    testCasePublicSelectNodeIds: [],
    testCaseModuleOptions: [],
    testCaseReviewModuleOptions: [],
    testCaseReviewCaseModuleOptions: [],
    testPlanModuleOptions: [],
    testReviewSelectNode: {},
    testReviewSelectNodeIds: [],
    testPlanViewSelectNode: {},
    isTestCaseMinderChanged: false,
    // 当前项目是否勾选自定义ID
    currentProjectIsCustomNum: false,
    testCaseTemplate: {},
    testCaseMap: new Map(),
    curTabId: null,
    testCaseDefaultValue: {},
    temWorkspaceId: null,
    appFixed: null,
    // 防止全量导出时重复点击
    isTestCaseExporting: false
  }),
  persist: false,
  getters: {
    currentApi(store) {
      return store.$state
    },
  },
}
