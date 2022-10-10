import JSEncrypt from 'jsencrypt';
import {LicenseKey, PROJECT_ID, WORKSPACE_ID} from "./constants";
import {useUserStore} from "@/store";

export function getCurrentWorkspaceId() {
  let workspaceId = sessionStorage.getItem(WORKSPACE_ID);
  if (workspaceId) {
    return workspaceId;
  }
  return getCurrentUser().lastWorkspaceId;
}

export function getCurrentProjectID() {
  let projectId = sessionStorage.getItem(PROJECT_ID);
  if (projectId) {
    return projectId;
  }
  return getCurrentUser().lastProjectId;
}

export function getCurrentUser() {
  try {
    const store = useUserStore();
    return store.currentUser;
  } catch (e) {
    console.log(e)
    return {};
  }
}

export function getCurrentUserId() {
  return getCurrentUser().id;
}

export function publicKeyEncrypt(input, publicKey) {

  let jsencrypt = new JSEncrypt({default_key_size: 1024});
  jsencrypt.setPublicKey(publicKey);

  return jsencrypt.encrypt(input);
}
