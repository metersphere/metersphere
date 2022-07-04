import Vue from 'vue'
import SvgIcon from '@/business/components/common/svg/SvgIcon';

// 注册到全局
Vue.component('svg-icon', SvgIcon)

const requireAll = requireContext => requireContext.keys().map(requireContext)
const req = require.context('@/assets/module', false, /\.svg$/)
requireAll(req)
