import {post, get} from "@/common/js/ajax";
import {getCurrentProjectID} from "@/common/js/utils";

export function getTemplate(baseUrl) {
  return new Promise((resolve) => {
    let template = {};
    get(baseUrl + getCurrentProjectID(), (response) => {
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
  post('/custom/field/template/update', request);
}
