import {get} from "@/common/js/ajax";

export function getIntegrationService(success) {
  return get("/service/integration/all", response => {
    if (success) {
      success(response.data);
    }
  });
}
