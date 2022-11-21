import { get } from "metersphere-frontend/src/plugins/request";

export function getApiTemplate(projectId) {
  return new Promise((resolve) => {
    let template = {};
    let baseUrl = "/field/template/api/get-template/relate/";
    get(baseUrl + projectId).then((response) => {
      template = response.data;
      if (template.customFields) {
        template.customFields.forEach((item) => {
          if (item.options) {
            item.options = JSON.parse(item.options);
          }
        });
      }
      resolve(template);
    });
  });
}
