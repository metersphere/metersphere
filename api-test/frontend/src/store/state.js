export default {
  id: "API",
  state: () => ({
    useEnvironment: "",
    selectStep: {},
    currentApiCase: {},
    pluginFiles: [],
    scenarioEnvMap: new Map(),
    apiMap: new Map(),
    apiStatus: new Map(),
    apiCaseMap: new Map(),
    forceRerenderIndex: ""
  }),
  getters: {
    currentApi(store) {
      return store.$state
    },
  },
}
