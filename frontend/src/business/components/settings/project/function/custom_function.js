export const FUNC_TEMPLATE = {
  beanshell: "public static void test() {\n\n\n}",
  groovy: "public static void test() {\n\n\n}",
  python: "def test():\n",
  nashornScript: "function test() {\n\n\n}",
  rhinoScript: "function test() {\n\n\n}"
}


// 拼接函数
export function splicingCustomFunc(funcObj, funcParams) {
  let funcLanguage = funcObj.type || "beanshell";
  let funcObjScript = funcObj.script;
  let funcName = funcObj.name;
  let funcFirstLine = generateFuncFirstLine(funcLanguage, funcName, funcParams);
  if (!funcObjScript && funcName) {
    funcObjScript = funcLanguage === "python" ? funcFirstLine : funcFirstLine + "\n\n\n}";
  }
  if (funcObjScript) {
    funcObjScript = funcObjScript.replace(regex[funcLanguage], funcFirstLine);
  }
  return funcObjScript;
}

export function generateFuncFirstLine(funcLanguage, funcName, funcParams) {
  let funcEnd = funcLanguage === "python" ? ":" : "{";
  return scriptFuncDefinition[funcLanguage] + " " + funcName + "(" + funcParams + ") " + funcEnd;
}

const scriptFuncDefinition = {
  beanshell: "public static void",
  python: "def",
  groovy: "public static void",
  // nashornScript: "",
  // rhinoScript: "",
}

const firstFuncRegex = RegExp(".*\(.*\)\\s\{\\r?");
const regex = {
  beanshell: calcRegex(scriptFuncDefinition.beanshell),
  python: RegExp("^def\\s.*\(.*\)\\s\:"),
  groovy: calcRegex(scriptFuncDefinition.groovy),
  // nashornScript: calcRegex(scriptFuncDefinition.nashornScript),
  // rhinoScript: calcRegex(scriptFuncDefinition.rhinoScript),
}

function calcRegex(str) {
  return RegExp("^" + str + "\\s.*\(.*\)\\s\{");
}
