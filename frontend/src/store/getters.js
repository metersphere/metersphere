const getters = {
  isNewVersion: state => state.versionSwitch === 'new',
  isOldVersion: state => state.versionSwitch === 'old'
}

export default getters
