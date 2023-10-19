function enableModules(...modules) {
  let sysModules = JSON.parse(sessionStorage.getItem("modules"));
  let projectModules = JSON.parse(sessionStorage.getItem("project_modules"));
  let workModules = JSON.parse(sessionStorage.getItem("workspace_modules"));
  for (let module of modules) {
    if (module === "project" || module === "setting") {
      return modules && modules[module] === "ENABLE";
    }
    if (projectModules && projectModules.length > 0) {
      return (
        sysModules[module] === "ENABLE" && projectModules.indexOf(module) > -1
      );
    }
    if (workModules && workModules.length > 0) {
      return (
        sysModules[module] === "ENABLE" && workModules.indexOf(module) > -1
      );
    }
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
