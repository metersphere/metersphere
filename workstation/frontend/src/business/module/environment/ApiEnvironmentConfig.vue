<template>
  <div>
    <el-dialog :close-on-click-modal="false" :title="$t('api_test.environment.environment_config')"
               :visible.sync="visible" class="environment-dialog" width="80%"
               @close="close" append-to-body destroy-on-close ref="environmentConfig">
      <el-container v-loading="result.loading">
        <ms-aside-item :enable-aside-hidden="false" :title="$t('api_test.environment.environment_list')"
                       :data="environments" :item-operators="environmentOperators"
                       :env-add-permission="ENV_CREATE"
                       :delete-fuc="openDelEnv" @itemSelected="environmentSelected" ref="environmentItems"/>
      </el-container>
    </el-dialog>
  </div>
</template>

<script>
import draggable from 'vuedraggable';
import MsContainer from "metersphere-frontend/src/components/MsContainer";
import MsAsideContainer from "metersphere-frontend/src/components/MsAsideContainer";
import MsDialogHeader from "metersphere-frontend/src/components/MsDialogHeader";
import MsMainContainer from "metersphere-frontend/src/components/MsMainContainer";
import {listenGoBack, removeGoBackListener} from "metersphere-frontend/src/utils";
import {hasPermission} from "metersphere-frontend/src/utils/permission";
import MsAsideItem from "metersphere-frontend/src/components/MsAsideItem";
import {Environment, parseEnvironment} from "metersphere-frontend/src/model/EnvironmentModel";
import {delApiEnvironment, getEnvironmentByProjectId} from "metersphere-frontend/src/api/environment";

export default {
  name: "ApiEnvironmentConfig",
  components: {
    MsAsideItem,
    MsMainContainer, MsAsideContainer, MsContainer, draggable, MsDialogHeader
  },
  data() {
    return {
      result: {},
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
      newEnvironment.name = this.getNoRepeatName(newEnvironment.name);
      if (!this.validateEnvironment(newEnvironment)) {
        return;
      }
      this.$refs.environmentEdit._save(newEnvironment);
      this.environments.unshift(newEnvironment);
      this.$refs.environmentItems.itemSelected(this.environments.length - 1, newEnvironment);
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

    environmentSelected(environment) {
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
    },
    save() {
      this.$refs.environmentEdit.save();
    },
    close() {
      this.$emit('close');
      if (!this.isCopy) {
        this.visible = false;
      }
      this.$refs.environmentEdit.clearValidate();
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
