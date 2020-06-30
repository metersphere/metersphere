<template>
  <el-dialog :title="'环境配置'" :visible.sync="visible" class="environment-dialog">
    <el-container >
      <ms-aside-item :title="'环境列表'" :data="environments" :add-fuc="addEnvironment" :delete-fuc="deleteEnvironment" @itemSelected="environmentSelected"/>
      <el-main>

      </el-main>
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

    export default {
      name: "ApiEnvironmentConfig",
      components: {
        MsAsideItem,
        MsMainContainer, MsAsideContainer, MsContainer, MsApiCollapseItem, MsApiCollapse, draggable},
      data() {
        return {
          visible:false,
          environments: [{name: 'tesddd'}]
        }
      },
      methods: {
        open(project) {
          this.visible = true
        },
        deleteEnvironment(environment) {
          console.log(environment);
          for (let i = 0; i < this.environments.length; i++) {
            if (this.environments[i].name === environment.name) {
              this.environments.splice(i, 1);
              break;
            }
          }
        },
        addEnvironment() {
          this.environments.push({name: '新建'});
          console.log('add');
        },
        environmentSelected() {
          console.log('select');
        }
      }
    }
</script>

<style scoped>

  .environment-dialog >>> .el-dialog__body {
    padding-top: 20px;
  }

  .ms-aside-container {
    height: calc(100vh - 500px);
  }

</style>
