import {getCurrentOrganizationId} from "@/common/js/utils";
import {get} from "@/common/js/ajax";

export function getIntegrationService(success) {
  return get("/service/integration/all/" + getCurrentOrganizationId(), response => {
    let data = response.data;
    if (success) {
      success(response.data);
    }
  });
}
