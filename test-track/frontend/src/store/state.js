export default {
  id: "TRACK",
  state: () => ({
    theme: undefined,
    testCaseSelectNode: {},
    testCaseSelectNodeIds: [],
    testCaseModuleOptions: [],
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
  }),
  persist: false,
  getters: {
    currentApi(store) {
      return store.$state
    },
  },
}
