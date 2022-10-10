import {hasLicense, hasPermissions} from "../../utils/permission"

function checkPermissions(el, binding) {
  const {value} = binding;
  if (value && value instanceof Array && value.length > 0) {
    let hasPermission = hasPermissions(...(value));
    if (!hasPermission) {
      el.parentNode && el.parentNode.removeChild(el);
    }
  }
}

function checkLicense(el, binding, type) {
  let v = hasLicense();

  if (!v) {
    el.parentNode && el.parentNode.removeChild(el);
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
  },
  hasLicense: {
    inserted(el, binding) {
      checkLicense(el, binding);
    }
  }
}
