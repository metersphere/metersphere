import SvgIcon from './SvgIcon';

const requireAll = requireContext => requireContext.keys().map(requireContext)
const req = require.context("../../assets/module", false, /\.svg$/)
requireAll(req)

export default {
  install(Vue) {
    Vue.component('svg-icon', SvgIcon)
  }
}
