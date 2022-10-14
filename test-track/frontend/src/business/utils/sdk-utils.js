export {operationConfirm, removeGoBackListener, handleCtrlSEvent, byteToSize, getTypeByFileName, strMapToObj, getUUID, windowPrint} from "metersphere-frontend/src/utils";
export {parseCustomFilesForList, getCustomFieldFilter, buildBatchParam} from "metersphere-frontend/src/utils/tableUtils";
export {getCurrentProjectID, getCurrentWorkspaceId, getCurrentUser} from "metersphere-frontend/src/utils/token";
export {hasLicense, hasPermissions, hasPermission} from "metersphere-frontend/src/utils/permission";
export {get, post, downloadFile, fileDownloadGet, fileDownloadPost, fileUpload} from "metersphere-frontend/src/plugins/request";
export {CURRENT_LANGUAGE} from "metersphere-frontend/src/utils/constants";


export {generateColumnKey, getAdvSearchCustomField} from "metersphere-frontend/src/components/search/custom-component";
export {TEST_CASE_RELEVANCE_ISSUE_LIST} from "metersphere-frontend/src/components/search/search-components";


export {getProjectMemberOption} from "metersphere-frontend/src/api/user";
export {deleteMarkDownImgByName} from "metersphere-frontend/src/api/img";
export {getApiDefinitionById, getApiTestCasePages} from "metersphere-frontend/src/api/environment";
export {getOwnerProjects, getProjectListAll} from "metersphere-frontend/src/api/project";
export {deleteRelationshipEdge} from "metersphere-frontend/src/api/relationship-edge";
export {isProjectVersionEnable, getProjectVersions, getVersionFilters} from "metersphere-frontend/src/api/version";


import {
  getCustomFieldValue,
} from "metersphere-frontend/src/utils/tableUtils";
import i18n from "@/i18n";

export function getCustomFieldValueForTrack(row, field, members) {
  if (field.name === '用例状态' && field.system) {
    return parseStatus(row, field.options);
  }
  return getCustomFieldValue(row, field, members);
}

function parseStatus(row, options) {
  if (options) {
    for (let option of options) {
      if (option.value === row.status) {
        return option.system ? i18n.t(option.text) : option.text;
      }
    }
  }
  return row.status;
}
