<template>
  <div>
    <el-dialog :close-on-click-modal="false" :title="$t('api_test.environment.environment_config')"
               :visible.sync="visible" class="environment-dialog" width="80%"
               @close="close" append-to-body destroy-on-close ref="environmentConfig">
      <el-container v-loading="result.loading">
        <ms-aside-item :enable-aside-hidden="false" :title="$t('api_test.environment.environment_list')"
                       :data="environments" :item-operators="environmentOperators" :add-fuc="addEnvironment"
                       :env-add-permission="ENV_CREATE"
                       :delete-fuc="openDelEnv" @itemSelected="environmentSelected" ref="environmentItems"/>
        <environment-edit :if-create="ifCreate" :environment="currentEnvironment"
                          ref="environmentEdit" :is-read-only="isReadOnly"
                          @confirm="save" :is-project="true"
                          @close="close" @refreshAfterSave="refresh"/>
      </el-container>
    </el-dialog>
    <el-dialog
      :visible.sync="delDialogVisible"
      append-to-body
      width="30%">
      <span style="color: #de9d1c; font-size: 18px;padding-right: 5px"><i class="el-icon-warning"/></span><span
      style="font-size: 18px">{{ $t('commons.confirm_delete') + currentEnvironment.name }}</span>
      <span slot="footer" class="dialog-footer">
        <el-button @click="delDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="delEnvironment">确 定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import MsApiCollapse from "./collapse/ApiCollapse";
import MsApiCollapseItem from "./collapse/ApiCollapseItem";
import draggable from 'vuedraggable';
import MsContainer from "../../../common/components/MsContainer";
import MsAsideContainer from "../../../common/components/MsAsideContainer";
import MsMainContainer from "../../../common/components/MsMainContainer";
import MsAsideItem from "../../../common/components/MsAsideItem";
import EnvironmentEdit from "./environment/EnvironmentEdit";
import {getUUID, hasPermission, listenGoBack, removeGoBackListener} from "../../../../../common/js/utils";
import {Environment, parseEnvironment} from "../model/EnvironmentModel";
import MsDialogHeader from "@/business/components/common/components/MsDialogHeader";

export default {
  name: "ApiEnvironmentConfig",
  components: {
    EnvironmentEdit,
    MsAsideItem,
    MsMainContainer, MsAsideContainer, MsContainer, MsApiCollapseItem, MsApiCollapse, draggable, MsDialogHeader
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
      ifCreate: false, //是否是创建环境
      delDialogVisible: false,
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
      this.$alert(this.$t('commons.confirm_delete') + environment.name, '', {
        confirmButtonText: this.$t('commons.confirm'),
        cancelButtonText: this.$t('commons.cancel'),
        callback: (action) => {
          if (action === 'confirm') {
            if (environment.id) {
              this.result = this.$get('/api/environment/delete/' + environment.id, () => {
                this.$success(this.$t('commons.delete_success'));
                this.getEnvironments();
                this.delDialogVisible = false;
              });
            } else {
              this.environments.splice(index, 1);
              this.delDialogVisible = false;
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
    addEnvironment() {
      this.ifCreate = true;
      let newEnvironment = new Environment({
        projectId: this.projectId
      });
      this.environments.push(newEnvironment);
      this.$refs.environmentItems.itemSelected(this.environments.length - 1, newEnvironment);
    },
    environmentSelected(environment) {
      this.getEnvironment(environment);
    },
    getEnvironments() {
      if (this.projectId) {
        this.result = this.$get('/api/environment/list/' + this.projectId, response => {
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
      this.delDialogVisible = true
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

.environment-dialog >>> .el-dialog__body {
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
