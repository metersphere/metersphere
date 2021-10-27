<template>
  <el-dialog width="50%" :close-on-click-modal="false" top="5vh" :title="$t('api_test.jar_config.title')" :visible.sync="visible" class="jar-import" @close="close">
    <div v-loading="result.loading">
      <ms-jar-config-from :config="currentConfig" :callback="saveConfig" ref="jarConfigFrom" :read-only="isReadOnly"/>
<!--      <ms-jar-search-bar v-if="(!isSearchBarQuery && configs.length > 0) || isSearchBarQuery" :condition="condition"-->
<!--                         @search="getJarConfigs" :table-data="configs" ref="jarSearchBar"/>-->
      <ms-jar-config-list :show-upload-btn="false" :table-data="configs" ref="jarConfigList"/>
    </div>
  </el-dialog>
</template>

<script>
import MsDialogFooter from "@/business/components/common/components/MsDialogFooter";
import {listenGoBack, removeGoBackListener} from "@/common/js/utils";
import MsJarConfigFrom from "@/business/components/api/test/components/jar/JarConfigFrom";
import MsJarSearchBar from "@/business/components/api/test/components/jar/JarSearchBar";
import MsJarConfigList from "@/business/components/settings/workspace/JarConfigList";

export default {
  name: "MsJarConfig",
  components: {MsJarConfigFrom, MsJarSearchBar, MsDialogFooter, MsJarConfigList},
  data() {
    return {
      visible: false,
      result: {},
      currentConfig: {},
      configs: [],
      condition: {},
      isSearchBarQuery: false
    }
      },
      props: {
        isReadOnly: {
          type: Boolean,
          default: false
        },
      },
      methods: {
        open() {
          this.visible = true;
          this.condition = {};
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
          let url = config.id ? "/jar/update" : "/jar/add";
          this.result = this.$fileUpload(url, file, null, config, () => {
            this.$success(this.$t('commons.save_success'));
            this.$refs.jarConfigList.getJarConfigs();
            this.$refs.jarConfigFrom.clear();
          });
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

   .jar-config-list {
     max-height: 600px;
     overflow: scroll;
   }

</style>
