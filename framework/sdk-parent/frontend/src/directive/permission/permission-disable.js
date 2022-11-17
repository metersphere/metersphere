import {hasPermissions} from "../../utils/permission"

function checkPermissions(el, binding) {
  const {value} = binding;
  if (value && value instanceof Array && value.length > 0) {
    let hasPermission = hasPermissions(...(value));
    if (!hasPermission) {
      if (el.nodeName === 'A') {
        el.style["pointer-events"] = "none";
      } else {
        el.disabled = true;
        el.classList.add('is-disabled');
      }
    }
  }
}

export default {
  hasPermissions: {
    inserted(el, binding) {
      checkPermissions(el, binding);
    },
    update(el, binding) {
      checkPermissions(el, binding);
    }
  }
}
