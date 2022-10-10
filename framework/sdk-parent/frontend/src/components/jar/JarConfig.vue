<template>
  <el-dialog width="50%" :close-on-click-modal="false" :title="$t('api_test.jar_config.title')" :visible.sync="visible" class="jar-import" @close="close">
    <div v-loading="result.loading">
      <ms-jar-config-from :config="currentConfig" :callback="saveConfig" ref="jarConfigFrom" :read-only="isReadOnly"/>
    </div>
  </el-dialog>
</template>

<script>
import MsDialogFooter from "../MsDialogFooter";
import {getCurrentProjectID} from "../../utils/token";
import {listenGoBack, removeGoBackListener} from "../../utils";
import MsJarConfigFrom from "./JarConfigFrom";
import MsJarSearchBar from "./JarSearchBar";

export default {
  name: "MsJarConfig",
  components: {MsJarConfigFrom, MsJarSearchBar, MsDialogFooter},
  data() {
    return {
      visible: false,
      result: {},
      currentConfig: {},
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
          config.resourceType = 'PROJECT';
          config.resourceId = getCurrentProjectID();
          let url = config.id ? "/jar/update" : "/jar/add";
          this.result = this.$fileUpload(url, file, null, config, () => {
            this.$success(this.$t('commons.save_success'));
            this.$emit("refresh")
            this.visible = false;
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
