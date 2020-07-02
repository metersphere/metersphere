<template>
  <el-dialog :title="'环境配置'" :visible.sync="visible" class="environment-dialog">
    <el-container v-loading="result.loading">
      <ms-aside-item :title="'环境列表'" :data="environments" :item-operators="environmentOperators" :add-fuc="addEnvironment"
                     :delete-fuc="deleteEnvironment" @itemSelected="environmentSelected" ref="environmentItems"/>
      <environment-edit :environment="currentEnvironment" ref="environmentEdit"/>
    </el-container>
  </el-dialog>
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

    export default {
      name: "ApiEnvironmentConfig",
      components: {
        EnvironmentEdit,
        MsAsideItem,
        MsMainContainer, MsAsideContainer, MsContainer, MsApiCollapseItem, MsApiCollapse, draggable},
      data() {
        return {
          result: {},
          visible:false,
          projectId: '',
          environments: [],
          currentEnvironment: {variables: [{}], headers: [{}], protocol: 'https', projectId: this.projectId},
          environmentOperators: [
            {
              icon: 'el-icon-document-copy',
              func: this.copyEnvironment
            },
            {
              icon: 'el-icon-delete',
              func: this.deleteEnvironment
            }
          ]
        }
      },
      methods: {
        open(projectId) {
          this.visible = true;
          this.projectId = projectId;
          this.getEnvironments();
        },
        deleteEnvironment(environment) {
          this.result = this.$get('/api/environment/delete/' + environment.id, response => {
            this.$success('删除成功');
            this.getEnvironments();
          });
        },
        copyEnvironment(environment) {
          let newEnvironment = {};
          Object.assign(newEnvironment, environment);
          newEnvironment.id = null;
          this.environments.push(newEnvironment);
        },
        addEnvironment() {
          this.environments.push(this.getDefaultEnvironment());
        },
        environmentSelected(environment) {
          this.getEnvironment(environment);
        },
        getEnvironments() {
          if (this.projectId) {
            this.result = this.$get('/api/environment/list/' + this.projectId, response => {
              this.environments = response.data;
              if (this.environments.length > 0) {
                this.$refs.environmentItems.itemSelected(0, this.environments[0]);
              } else {
                let item = this.getDefaultEnvironment();
                this.environments.push(item);
                this.$refs.environmentItems.itemSelected(0, item);
              }
            });
          }
        },
        getEnvironment(environment) {
          let item = environment;
          if (!(environment.variables instanceof Array)) {
            item.variables = JSON.parse(environment.variables);
          }
          if (!(environment.headers instanceof Array)) {
            item.headers = JSON.parse(environment.headers);
          }
          this.currentEnvironment =  item;
        },
        getDefaultEnvironment() {
          return {variables: [{}], headers: [{}], protocol: 'https', projectId: this.projectId};
        }
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
