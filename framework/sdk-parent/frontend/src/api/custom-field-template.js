import {get, post} from "../plugins/request";
import {getCurrentProjectID} from "../utils/token";

export function getTemplate(baseUrl) {
  return new Promise((resolve) => {
    let template = {};
    return get(baseUrl + getCurrentProjectID())
      .then((response) => {
        template = response.data;
        if (template.customFields) {
          template.customFields.forEach(item => {
            if (item.options) {
              item.options = JSON.parse(item.options);
            }
          });
        }
        resolve(template);
      });
  });
}

export function getIssueTemplate() {
  return getTemplate('field/template/issue/get/relate/');
}

export function getTestTemplate() {
  return getTemplate('field/template/case/get/relate/');
}

export function updateCustomFieldTemplate(request) {
  return post('/custom/field/template/update', request);
}

