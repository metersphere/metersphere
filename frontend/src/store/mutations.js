const mutations = {
  setProjectId: (state, projectId) => state.projectId = projectId,
  setTest: (state, test) => state.test = test,
  clearTest: state => state.test = {},
  setVersionSwitch: (state, value) => state.versionSwitch = value,
  setTheme: (state, value) => state.theme = value
}

export default mutations;
