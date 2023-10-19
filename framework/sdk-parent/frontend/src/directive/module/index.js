function enableModules(...modules) {
  for (let module of modules) {
    let modules = JSON.parse(localStorage.getItem("modules"));
    if (modules && modules[module] === "DISABLE") {
      return false;
    }
  }
  return true;
}

function checkModule(el, binding) {
  const { value } = binding;
  if (value && value instanceof Array && value.length > 0) {
    let v = enableModules(...value);
    if (!v) {
      el.parentNode && el.parentNode.removeChild(el);
    }
  }
}

export default {
  inserted(el, binding) {
    checkModule(el, binding);
  },
};
