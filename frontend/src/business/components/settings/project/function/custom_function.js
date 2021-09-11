export function splicingCustomFunc(funcLanguage, funcObjScript, funcName, funcParams) {
  let funcFirstLine = generateFuncFirstLine(funcLanguage, funcName, funcParams);
  if (!funcObjScript) {
    funcObjScript = funcFirstLine + "\n\n\n}";
  }
  funcObjScript = funcObjScript.replace(regex[funcLanguage], funcFirstLine);
  return funcObjScript;
}

export function generateFuncFirstLine(funcLanguage, funcName, funcParams) {
  let funcFirstLine = "";
  switch (funcLanguage) {
    case "beanshell":
      funcFirstLine = "function " + funcName + "(" + funcParams + ") " + "{";
      break;
    case "python":
      break;
    case "groovy":
      break;
    case "nashornScript":
      break;
    case "rhinoScript":
      break;
    default:
  }
  return funcFirstLine;
}

const regex = {
  beanshell: /^function\s.*\(.*\)\s\{/,
  python: /^function\s.*\(.*\)\s\{/,
  groovy: /^function\s.*\(.*\)\s\{/,
  nashornScript: /^function\s.*\(.*\)\s\{/,
  rhinoScript: /^function\s.*\(.*\)\s\{/,
}

