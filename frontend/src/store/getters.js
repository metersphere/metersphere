const getters = {
  isNewVersion: state => state.versionSwitch === 'new',
  isOldVersion: state => state.versionSwitch === 'old',
  // getTestCaseNodePath(state, nodeId) {
  //   for (const index in state.moduleOptions) {
  //     let item = state.moduleOptions[index];
  //     if (nodeId === item.id) {
  //       return item.path;
  //     }
  //   }
  // }
}

export default getters
