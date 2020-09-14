<template>
  <div>
    <ms-database-from :config="currentConfig" @save="addConfig" ref="databaseFrom"/>
    <ms-database-config-list v-if="configs.length > 0" :table-data="configs"/>
  </div>
</template>

<script>
    import MsDatabaseConfigList from "./DatabaseConfigList";
    import {DatabaseConfig} from "../../../model/ScenarioModel";
    import MsDatabaseFrom from "./DatabaseFrom";
    import {getUUID} from "../../../../../../../common/js/utils";

    export default {
      name: "MsDatabaseConfig",
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
          drivers: DatabaseConfig.DRIVER_CLASS,
          currentConfig: new DatabaseConfig()
        }
      },
      methods: {
        addConfig(config) {
          for (let item of this.configs) {
            if (item.name === config.name) {
              this.$warning("名称重复");
              return;
            }
          }
          config.id = getUUID();
          this.configs.push(config);
          this.currentConfig =  new DatabaseConfig();
        }
      }
    }
</script>

<style scoped>

  .addButton {
    float: right;
  }

  .database-from {
    padding: 10px;
    border: #DCDFE6 solid 1px;
    margin: 5px 0;
    border-radius: 5px;
  }

</style>
