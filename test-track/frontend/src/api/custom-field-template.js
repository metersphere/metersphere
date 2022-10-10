import {post, get} from "metersphere-frontend/src/plugins/request";
import {getCurrentProjectID} from "metersphere-frontend/src/utils/token";

export function getTemplate(baseUrl) {
  return new Promise((resolve) => {
    let template = {};
    get(baseUrl + getCurrentProjectID())
      .then(response => {
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
