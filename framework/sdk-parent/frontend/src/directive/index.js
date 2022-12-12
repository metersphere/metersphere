import ClickOutside from "element-ui/src/utils/clickoutside";
import Permission from "./permission";
import Drag from "./drag";
import directive from './loading/directive';
import service from './loading/index';
import modules from "./module";
import NoPermissionDisable from "./permission/permission-disable";


export const onceLinkClick = {
  inserted(el, binding) {
    el.addEventListener('click', () => {
      if (el.style.pointerEvents === '') {
        el.style.pointerEvents = 'none'; // 防止a标签
        setTimeout(() => {
          el.style.pointerEvents = '';
        }, binding.value || 3000);
      }
    })
  }
}

export const onceClick = {
  inserted(el, binding) {
    el.addEventListener('click', () => {
      if (!el.disabled) {
        el.disabled = true
        setTimeout(() => {
          el.disabled = false
        }, binding.value || 3000)
      }
    })
  }
}

export default {
  install(Vue) {
    Vue.directive('click-outside', ClickOutside);
    Vue.directive('permission', Permission.hasPermissions);
    Vue.directive('permission-disable', NoPermissionDisable.hasPermissions);
    Vue.directive('xpack', Permission.hasLicense);
    Vue.directive('preventLinkReClick', onceLinkClick);
    Vue.directive('preventReClick', onceClick);
    Vue.directive('modules', modules);
    //支持左右拖拽
    Vue.directive('left-to-right-drag', Drag.left2RightDrag);
    Vue.directive('right-to-left-drag', Drag.right2LeftDrag);
    Vue.directive('bottom-to-top-drag', Drag.bottom2TopDrag);
    Vue.directive('top-bottom-to-drag', Drag.top2BottomDrag);
    Vue.directive('vertical-drag', Drag.verticalDrag);
    //
    Vue.use(directive);
    Vue.prototype.$loading = service;
  },
  directive,
  service
}


