<template>
  <el-dialog width="50%" :close-on-click-modal="false" :title="$t('api_test.jar_config.title')" :visible.sync="visible" class="jar-import" @close="close">
    <div v-loading="result.loading">
      <ms-jar-config-from :config="currentConfig" :callback="saveConfig" ref="jarConfigFrom" :read-only="isReadOnly"/>
      <ms-jar-config-list @refresh="getJarConfigs" v-if="configs.length > 0" @rowSelect="rowSelect" :table-data="configs"/>
    </div>
  </el-dialog>
</template>

<script>
    import MsDialogFooter from "../../../../common/components/MsDialogFooter";
    import ApiEnvironmentConfig from "../ApiEnvironmentConfig";
    import {listenGoBack, removeGoBackListener} from "../../../../../../common/js/utils";
    import MsJarConfigList from "./JarConfigList";
    import MsJarConfigFrom from "./JarConfigFrom";
    export default {
      name: "MsJarConfig",
      components: {MsJarConfigFrom, MsJarConfigList, ApiEnvironmentConfig, MsDialogFooter},
      data() {
        return {
          visible: false,
          result: {},
          projectId: "",
          currentConfig: {},
          configs: []
        }
      },
      props: {
        isReadOnly: {
          type: Boolean,
          default: false
        },
      },
      methods: {
        open(projectId) {
          this.visible = true;
          this.projectId = projectId;
          this.getJarConfigs();
          listenGoBack(this.close);
        },
        saveConfig(config, file) {
          for (let item of this.configs) {
            if (item.name === config.name && item.id !== config.id) {
              this.$warning(this.$t('commons.already_exists'));
              return;
            }
            if (item.fileName === file.name && item.id !== config.id) {
              this.$warning(this.$t('api_test.jar_config.file_exist'));
              return;
            }
          }
          let url = config.id ? "/api/jar/update" : "/api/jar/add";
          config.projectId = this.projectId;
          this.result = this.$fileUpload(url, file, null, config, () => {
            this.$success(this.$t('commons.save_success'));
            this.getJarConfigs();
          });
        },
        getJarConfigs() {
          this.result = this.$get("/api/jar/list/" + this.projectId, response => {
            this.configs = response.data;
            this.currentConfig = {};
          })
        },
        rowSelect(config) {
          this.currentConfig = config;
        },
        close() {
          this.$refs.jarConfigFrom.clear();
          removeGoBackListener(this.close);
          this.visible = false;
        }
      }
    }
</script>

<style scoped>

</style>
