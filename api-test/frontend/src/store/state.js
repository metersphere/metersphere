export default {
  id: 'API',
  state: () => ({
    useEnvironment: '',
    selectStep: {},
    currentApiCase: {},
    pluginFiles: [],
    scenarioEnvMap: new Map(),
    apiMap: new Map(),
    apiStatus: new Map(),
    apiCaseMap: new Map(),
    forceRerenderIndex: '',
    // 存储保存状态的map，只针对自定义属性change。
    saveMap: new Map(),
  }),
  getters: {
    currentApi(store) {
      return store.$state;
    },
  },
};
