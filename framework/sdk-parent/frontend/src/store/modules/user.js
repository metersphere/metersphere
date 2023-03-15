import {isLogin, login, logout, updateInfo} from '../../api/user'
import {getLanguage, setLanguage} from "@/i18n";
import {PROJECT_ID, SUPER_GROUP, TokenKey, WORKSPACE_ID} from "../../utils/constants";

function saveSessionStorage(response) {
  // 登录信息保存 cookie
  let user = response.data;
  // 校验权限
  user.userGroups.forEach(ug => {
    user.groupPermissions.forEach(gp => {
      if (gp.group.id === ug.groupId) {
        ug.userGroupPermissions = gp.userGroupPermissions;
        ug.group = gp.group;
      }
    });
  });
  // 检查当前项目有没有权限
  let currentProjectId = sessionStorage.getItem(PROJECT_ID);
  if (!currentProjectId) {
    sessionStorage.setItem(PROJECT_ID, user.lastProjectId);
  } else {
    let v = user.userGroups.filter(ug => ug.group && ug.group.type === 'PROJECT')
      .filter(ug => ug.sourceId === currentProjectId);
    let index = user.groups.findIndex(g => g.id === SUPER_GROUP);
    if (v.length === 0 && index === -1) {
      sessionStorage.setItem(PROJECT_ID, user.lastProjectId);
    }
  }
  if (!sessionStorage.getItem(WORKSPACE_ID)) {
    sessionStorage.setItem(WORKSPACE_ID, user.lastWorkspaceId);
  }
}

function clearSessionStorage() {
  localStorage.removeItem(TokenKey);
  sessionStorage.removeItem(WORKSPACE_ID);
  sessionStorage.removeItem(PROJECT_ID);
}

export default {
  id: TokenKey,
  state: () => ({
    language: getLanguage(),
    showLicenseCountWarning: false
  }),
  persist: true,
  getters: {
    currentUser(store) {
      return store.$state
    },
  },
  actions: {
    userLogin(userInfo) {
      let loginUrl = "/signin";
      switch (userInfo.authenticate) {
        case "LOCAL":
          loginUrl = "/signin";
          break;
        case "LDAP":
          loginUrl = "/ldap/signin";
          break;
        default:
          loginUrl = "/signin";
      }
      return new Promise((resolve, reject) => {
        login(loginUrl, userInfo)
          .then(response => {
            this.$patch(response.data)
            saveSessionStorage(response)
            resolve(response)
          })
          .catch(error => {
            reject(error);
          });
      });
    },

    getIsLogin() {
      return new Promise((resolve, reject) => {
        isLogin()
          .then((res) => {
            this.$patch(res.data)
            setLanguage(res.data.language)
            saveSessionStorage(res)
            resolve(res)
          })
          .catch((res) => {
            // 防止快速刷新页面时，请求中止导致的掉线问题
            if (res && res.code === 'ECONNABORTED') {
              return;
            }
            // 后台直接删除redis中的token，前端也需要删除
            clearSessionStorage()
            reject(res)
          })
      });
    },

    userSetLanguage(data) {
      this.language = data.language
      setLanguage(data.language)
      return new Promise((resolve, reject) => {
        updateInfo(data)
          .then(response => {
            resolve(response)
          })
          .catch(error => {
            reject(error)
          })
      })
    },

    userLogout() {
      return new Promise((resolve, reject) => {
        logout()
          .then(() => {
            clearSessionStorage();
            location.href = '/#/login';
            location.reload();
            resolve();
          })
          .catch(error => {
            clearSessionStorage();
            location.href = '/#/login';
            location.reload();
            reject(error);
          });
      })
    },
    switchWorkspace(response) {
      this.$patch(response.data);
      sessionStorage.setItem(WORKSPACE_ID, response.data.lastWorkspaceId);
      sessionStorage.setItem(PROJECT_ID, response.data.lastProjectId);
    },
    switchProject(response) {
      this.$patch(response.data);
      sessionStorage.setItem(PROJECT_ID, response.data.lastProjectId);
    }
  }
}
