import { DirectiveBinding } from 'vue';

import { hasAnyPermission } from '@/utils/permission';

/**
 * 权限指令
 * @param el dom 节点
 * @param binding vue 绑定的数据
 */
function checkPermission(el: HTMLElement, binding: DirectiveBinding) {
  const { value } = binding;
  if (Array.isArray(value)) {
    if (value.length > 0) {
      const hasPermission = hasAnyPermission(value);
      if (!hasPermission && el.parentNode) {
        el.parentNode.removeChild(el);
      }
    }
  } else {
    throw new Error(`need roles! Like v-permission="['admin','user']"`);
  }
}

export default {
  mounted(el: HTMLElement, binding: DirectiveBinding) {
    checkPermission(el, binding);
  },
  updated(el: HTMLElement, binding: DirectiveBinding) {
    checkPermission(el, binding);
  },
};
