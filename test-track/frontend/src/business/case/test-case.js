import {getAdvSearchCustomField, getCurrentProjectID, OPERATORS} from "@/business/utils/sdk-utils";
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

/**
 * 跳转到用例编辑页面
 * @param query
 * {
 *   projectId: 项目ID
 *   caseId: 用例ID
 *   type: 特殊跳转，copy 表示复制操作
 * }
 */
export function openCaseEdit(query, v) {
  if (!query.caseId) {
    return;
  }
  if (!query.type) {
    delete query.type;
  }
  if (query.type !== 'copy') {
    // 编辑不带项目id，会检查用例的权限
    // 复制需要带项目id，复制到当前项目，包括用例库的复制
    delete query.projectId;
  }
  let path = '/track/case/edit/' + query.caseId;
  delete query.caseId;
  let TestCaseData = v.$router.resolve({
    path,
    query,
  });
  window.open(TestCaseData.href, '_blank');
}

/**
 * 跳转到用例创建页面
 * @param query
 * {
 *   projectId: 项目ID
 * }
 */
export function openCaseCreate(query, v) {
  if (!query.projectId) {
    query.projectId = getCurrentProjectID();
  }
  let path = '/track/case/create';
  let TestCaseData = v.$router.resolve({
    path,
    query,
  });
  window.open(TestCaseData.href, '_blank');
}

export function getTagToolTips(tags) {
  try {
    let showTips = '';
    tags.forEach((item) => {
      showTips += item + ',';
    });
    return showTips.substr(0, showTips.length - 1);
  } catch (e) {
    return '';
  }
}


export function parseColumnTag(tags) {
  if (tags.length > 1) {
    let parseTags = [];
    parseTags.push(tags[0]);
    parseTags.push("+" + (tags.length - 1));
    return parseTags;
  } else {
    return tags;
  }
}
