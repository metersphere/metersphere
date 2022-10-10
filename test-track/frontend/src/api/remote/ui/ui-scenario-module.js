import {post, get} from "@/business/utils/sdk-utils";

const BASE_URL = '/ui/scenario/module/';

export function uiScenarioModuleProjectList(projectId) {
  return get(BASE_URL + `list/${projectId}`);
}
