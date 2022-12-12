import {getAdvSearchCustomField, OPERATORS} from "@/business/utils/sdk-utils";
import i18n from "@/i18n";

export function initTestCaseConditionComponents(condition, customFields, trashEnable) {
  let conditionComponents = condition.components;
  conditionComponents = conditionComponents.filter(item => item.custom !== true);
  let comp = getAdvSearchCustomField(condition, customFields);
  let statusOption = null;
  let priorityOption = null;
  // 系统字段国际化处理
  comp = comp.filter(element => {
    if (element.label === '责任人') {
      element.label = i18n.t('custom_field.case_maintainer');
    }
    if (element.label === '用例等级') {
      element.label = i18n.t('custom_field.case_priority');
      priorityOption = element.options;
      return false;
    }
    if (element.label === '用例状态') {
      element.label = i18n.t('custom_field.case_status');
      // 回收站TAB页处理高级搜索用例状态字段
      if (trashEnable) {
        element.operator.options = [OPERATORS.IN];
        element.options = [{text: i18n.t('test_track.plan.plan_status_trash'), value: 'Trash'}];
      } else {
        element.options.forEach(option => option.text = i18n.t(option.text));
      }
      statusOption = element.options;
      // 用例状态不走自定义字段的搜索，查询status字段
      return false;
    }
    return true;
  });

  conditionComponents.forEach((item) => {
    if ((item.key === 'status' || item.label === '用例状态') && statusOption) {
      item.options = statusOption;
    } else if (item.key === 'priority') {
      item.options = priorityOption;
    }
  });
  if (!statusOption) {
    // statusOption 为空，则去掉状态选项
    conditionComponents = conditionComponents.filter(item => item.key !== 'status' && item.label !== '用例状态');
  }
  conditionComponents.push(...comp);
  return conditionComponents;
}


export function openCaseEdit(caseId, type, v) {
  if (!caseId) {
    return;
  }
  let query = {};
  if (type) {
    query.type = type;
  }
  let TestCaseData = v.$router.resolve({
    path: "/track/case/edit/" + caseId,
    query,
  });
  window.open(TestCaseData.href, "_blank");
}
