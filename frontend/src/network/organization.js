import {getCurrentOrganizationId} from "@/common/js/utils";
import {get} from "@/common/js/ajax";

export function getIntegrationService(success) {
  return get("/service/integration/all/" + getCurrentOrganizationId(), response => {
    if (success) {
      success(response.data);
    }
  });
}
