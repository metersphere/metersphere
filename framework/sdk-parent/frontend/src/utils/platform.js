import i18n from "../i18n";

export function getPlatformFormRules(config) {
  let rules = {};
  if (config && config.formItems) {
    config.formItems.forEach(item => {
      rules[item.name] = {
        required: item.required,
        message: item.i18n ? i18n.t(item.message) : item.message,
        trigger: ['change', 'blur']
      }
    });
  }
  return rules;
}
