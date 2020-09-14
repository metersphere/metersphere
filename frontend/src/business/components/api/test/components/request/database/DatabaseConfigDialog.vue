<template>
  <el-dialog :title="'数据库配置'" :visible.sync="visible">
    <ms-database-from :config="config" @save="editConfig"/>
  </el-dialog>
</template>

<script>
    import MsDatabaseConfigList from "./DatabaseConfigList";
    import MsDatabaseFrom from "./DatabaseFrom";
    import {DatabaseConfig} from "../../../model/ScenarioModel";

    export default {
      name: "MsDatabaseConfigDialog",
      components: {MsDatabaseFrom, MsDatabaseConfigList},
      props: {
        configs: Array,
        isReadOnly: {
          type: Boolean,
          default: false
        },
      },
      data() {
        return {
          visible: false,
          config: new DatabaseConfig(),
        }
      },
      methods: {
        open(config) {
          this.visible = true;
          Object.assign(this.config, config);
        },
        editConfig(config) {
          let currentConfig = undefined;
          for (let item of this.configs) {
            if (item.name === config.name && item.id != config.id) {
              this.$warning("名称重复");
              return;
            }
            if (item.id === config.id) {
              currentConfig = item;
            }
          }
          if (currentConfig) {
            Object.assign(currentConfig, config)
          } else {
            //copy
            this.configs.push(config);
          }
          this.visible = false;
        }
      }
    }
</script>

<style scoped>

</style>
