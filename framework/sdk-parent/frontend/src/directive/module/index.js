function enableModules(...modules) {
  for (let module of modules) {
    let modules = JSON.parse(localStorage.getItem("modules"));
    if (module === "project" || module === "setting") {
      return modules && modules[module] === "ENABLE";
    }
    let projectModules = JSON.parse(sessionStorage.getItem("project_modules"));
    if (projectModules && projectModules.length > 0) {
      return (
        modules[module] === "ENABLE" && projectModules.indexOf(modules) > -1
      );
    }
    let workModules = JSON.parse(sessionStorage.getItem("workspace_modules"));
    if (workModules && workModules.length > 0) {
      return modules[module] === "ENABLE" && workModules.indexOf(modules) > -1;
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
