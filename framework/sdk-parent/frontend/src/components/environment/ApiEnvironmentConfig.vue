<template>
  <div>
    <el-dialog :close-on-click-modal="false" :title="$t('api_test.environment.environment_config')"
               :visible.sync="visible" class="environment-dialog" width="80%"
               @close="close" append-to-body destroy-on-close ref="environmentConfig" top="2vh">
      <el-container v-loading="result">
        <ms-aside-item :enable-aside-hidden="false" :title="$t('api_test.environment.environment_list')"
                       :data="environments" :item-operators="environmentOperators" :add-fuc="addEnvironment"
                       :env-add-permission="ENV_CREATE"
                       :delete-fuc="openDelEnv" @itemSelected="environmentSelected" ref="environmentItems"/>
        <environment-edit :if-create="ifCreate" :environment="currentEnvironment" v-if="visible"
                          ref="environmentEdit" :is-read-only="isReadOnly"
                          @confirm="save" :is-project="true" :key="currentEnvironment.id"
                          @close="close" @refreshAfterSave="refresh">
        </environment-edit>
      </el-container>
    </el-dialog>
  </div>
</template>

<script>
import draggable from 'vuedraggable';
import MsAsideItem from "../../components/MsAsideItem";
import EnvironmentEdit from "../../components/environment/EnvironmentEdit";
import {listenGoBack, removeGoBackListener, getUUID} from "../../utils";
import {hasPermission} from "../../utils/permission";
import {Environment, parseEnvironment} from "../../model/EnvironmentModel";
import MsDialogHeader from "../../components/MsDialogHeader";
import {delApiEnvironment, getEnvironmentByProjectId} from "../../api/environment";
import EnvironmentGlobalScript from "./EnvironmentGlobalScript";
import GlobalAssertions from "./assertion/GlobalAssertions";

export default {
  name: "ApiEnvironmentConfig",
  components: {
    EnvironmentEdit,
    MsAsideItem,
    EnvironmentGlobalScript,
    GlobalAssertions,
    draggable,
    MsDialogHeader
  },
  data() {
    return {
      result: false,
      visible: false,
      projectId: '',
      environments: [],
      currentEnvironment: new Environment(),
      environmentOperators: [
        {
          icon: 'el-icon-document-copy',
          func: this.copyEnvironment,
          permissions: this.type === 'project' ?
            ['PROJECT_ENVIRONMENT:READ+COPY'] : ['WORKSPACE_PROJECT_ENVIRONMENT:READ+COPY']
        },
        {
          icon: 'el-icon-delete',
          func: this.deleteEnvironment,
          permissions: this.type === 'project' ?
            ['PROJECT_ENVIRONMENT:READ+DELETE'] : ['WORKSPACE_PROJECT_ENVIRONMENT:READ+DELETE']
        }
      ],
      selectEnvironmentId: '',
      ifCreate: false, //是否是创建环境
      currentIndex: -1,
      isCopy: false
    }
  },
  props: {
    type: {
      type: String,
      default() {
        return "project";
      }
    }
  },
  computed: {
    ENV_CREATE() {
      return this.type === 'project' ?
        ['PROJECT_ENVIRONMENT:READ+CREATE'] : ['WORKSPACE_PROJECT_ENVIRONMENT:READ+CREATE'];
    },
    ENV_EDIT() {
      return this.type === 'project' ?
        ['PROJECT_ENVIRONMENT:READ+EDIT'] : ['WORKSPACE_PROJECT_ENVIRONMENT:READ+EDIT'];
    },
    isReadOnly() {
      // 区分 工作空间下的环境相关菜单/项目下的环境相关菜单
      return this.type === 'project' ?
        !hasPermission('PROJECT_ENVIRONMENT:READ+EDIT') : !hasPermission('WORKSPACE_PROJECT_ENVIRONMENT:READ+EDIT');
    }
  },
  methods: {
    updateGlobalScript(currentEnvironment, isPreScript, filedName, value) {
      if (isPreScript) {
        if (filedName === "connScenario") {
          currentEnvironment.config.globalScriptConfig.connScenarioPreScript = value;
        } else if (filedName === "execAfterPrivateScript") {
          currentEnvironment.config.globalScriptConfig.isPreScriptExecAfterPrivateScript = value;
        } else if (filedName === "filterRequest") {
          currentEnvironment.config.globalScriptConfig.filterRequestPreScript = value;
        }
      } else {
        if (filedName === "connScenario") {
          currentEnvironment.config.globalScriptConfig.connScenarioPostScript = value;
        } else if (filedName === "execAfterPrivateScript") {
          currentEnvironment.config.globalScriptConfig.isPostScriptExecAfterPrivateScript = value;
        } else if (filedName === "filterRequest") {
          currentEnvironment.config.globalScriptConfig.filterRequestPostScript = value;
        }
      }
    },
    open: function (projectId, envId) {
      this.visible = true;
      this.projectId = projectId;
      this.selectEnvironmentId = envId;
      this.getEnvironments();
      listenGoBack(this.close);
    },
    deleteEnvironment(environment, index) {
      this.$alert(this.$t('commons.delete') + "(" + environment.name + ")" + this.$t('project.del_env_tip'), '', {
        confirmButtonText: this.$t('commons.confirm'),
        cancelButtonText: this.$t('commons.cancel'),
        callback: (action) => {
          if (action === 'confirm') {
            if (environment.id) {
              this.result = delApiEnvironment(environment.id).then(() => {
                this.$success(this.$t('commons.delete_success'));
                this.getEnvironments();
              });
            } else {
              this.environments.splice(index, 1);
            }
          }
        }
      });

    },
    copyEnvironment(environment) {
      this.ifCreate = false;
      this.isCopy = true;
      //点击复制的时候先选择改行，否则会出现解析错误
      this.environmentSelected(environment);
      this.currentEnvironment = environment;
      if (!environment.id) {
        this.$warning(this.$t('commons.please_save'));
        return;
      }
      let newEnvironment = {};
      newEnvironment = new Environment(environment);
      newEnvironment.id = null;
      newEnvironment.config.databaseConfigs.forEach(dataSource => {
        if (dataSource.id) {
          dataSource.id = getUUID();
        }
      })
      newEnvironment.name = this.getNoRepeatName(newEnvironment.name);
      if (!this.validateEnvironment(newEnvironment)) {
        return;
      }
      newEnvironment.config.databaseConfigs.forEach(dataSource => {
        if (dataSource.id) {
          dataSource.id = getUUID();
        }
      })
      this.$refs.environmentEdit._save(newEnvironment);
      this.getEnvironments();
    },
    validateEnvironment(environment) {
      if (!this.$refs.environmentEdit.validate()) {
        this.$error(this.$t('commons.formatErr'));
        return false;
      }
      return true;
    },
    getNoRepeatName(name) {
      for (let i in this.environments) {
        if (this.environments[i].name === name) {
          return this.getNoRepeatName(name + ' copy');
        }
      }
      return name;
    },
    addEnvironment() {
      this.ifCreate = true;
      let newEnvironment = new Environment({
        projectId: this.projectId
      });
      this.environments.push(newEnvironment);
      this.$refs.environmentItems.itemSelected(this.environments.length - 1, newEnvironment);
    },
    environmentSelected(environment) {
      if (this.$refs.environmentEdit) {
        this.$refs.environmentEdit.clearValidate();
      }
      this.getEnvironment(environment);
    },
    getEnvironments() {
      if (this.projectId) {
        this.result = getEnvironmentByProjectId(this.projectId).then(response => {
          this.environments = response.data;
          if (this.environments.length > 0) {
            if (this.selectEnvironmentId) {
              const index = this.environments.findIndex(e => e.id === this.selectEnvironmentId);
              if (index !== -1) {
                this.$refs.environmentItems.itemSelected(index, this.environments[index]);
              } else {
                this.$refs.environmentItems.itemSelected(0, this.environments[0]);
              }
            } else {
              this.$refs.environmentItems.itemSelected(0, this.environments[0]);
            }
          } else {
            let item = new Environment({
              projectId: this.projectId
            });
            this.environments.push(item);
            this.$refs.environmentItems.itemSelected(0, item);
          }
        });
      }
    },
    getEnvironment(environment) {
      parseEnvironment(environment);
      this.currentEnvironment = environment;
      if (this.currentEnvironment.name) {
        this.ifCreate = false;
      }
    },
    save() {
      this.$refs.environmentEdit.save();
      this.currentEnvironment = null;
    },
    close() {
      this.$emit('close');
      if (!this.isCopy) {
        this.visible = false;
      }
      this.$refs.environmentEdit.clearValidate();
      this.currentEnvironment = null;
      removeGoBackListener(this.close);
      this.isCopy = false;
    },
    openDelEnv(environment, index) {
      this.currentEnvironment = environment;
      this.currentIndex = index;
    },
    delEnvironment() {
      this.deleteEnvironment(this.currentEnvironment, this.currentIndex)
    },
    refresh() {
      this.getEnvironments();
      this.$emit('saveRefresh');
    },
  }
}
</script>

<style scoped>

.environment-dialog :deep(.el-dialog__body) {
  padding-top: 20px;
}

.el-container {
  position: relative;
}

.ms-aside-container {
  height: 100%;
  position: absolute;
}

</style>
