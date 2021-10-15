<template>
  <el-card class="table-card">
    <template v-slot:header>
      <ms-table-header :show-create="false" :condition.sync="condition"
                       @search="search" @create="create"
                       :create-tip="$t('project.create')">
        <template v-slot:button>
          <ms-table-button icon="el-icon-box"
                           v-permission="['PROJECT_MANAGER:READ+EDIT']"
                           :content="$t('api_test.jar_config.title')" @click="openJarConfig"/>
        </template>
      </ms-table-header>
    </template>

    <ms-jar-config-list @refresh="getJarConfigs" v-if="configs.length > 0" @rowSelect="rowSelect" :table-data="configs" ref="jarConfigList"/>
    <ms-jar-config ref="jarConfig"/>
  </el-card>
</template>

<script>
import MsContainer from "@/business/components/common/components/MsContainer";
import MsMainContainer from "@/business/components/common/components/MsMainContainer";
import MsTableHeader from "@/business/components/common/components/MsTableHeader";
import MsTableButton from "@/business/components/common/components/MsTableButton";
import MsJarConfig from "@/business/components/api/test/components/jar/JarConfig";
import MsJarConfigList from "@/business/components/api/test/components/jar/JarConfigList";
export default {
  name: "JarManage",
  components: {
    MsMainContainer,
    MsContainer,
    MsTableHeader,
    MsTableButton,
    MsJarConfigList,
    MsJarConfig
  },
  data() {
    return {
      condition: {},
      configs: [],
      currentConfig: {},
    }
  },
  created() {
    this.getJarConfigs();
  },
  methods: {
    openJarConfig() {
      this.$refs.jarConfig.open();
    },
    getJarConfigs(isSearchBarQuery) {
      if (isSearchBarQuery) {
        this.isSearchBarQuery = isSearchBarQuery;
      }
      this.result = this.$post("/jar/list", this.condition, response => {
        this.configs = response.data;
        this.currentConfig = {};
      });
    },
    rowSelect(config) {
      this.currentConfig = config;
    },
  }
}
</script>

<style scoped>

</style>
