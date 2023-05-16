<template>
  <div v-if="detail && detail.details && detail.details.columns" style="margin-left: 20px">
    <el-table :data="detail.details.columns">
      <el-table-column prop="columnTitle" :label="$t('operating_log.param_name')" width="150px" show-overflow-tooltip/>
      <el-table-column prop="newValue" :label="$t('operating_log.param_value')">
        <template v-slot:default="scope">
          <div v-if="isObject(scope.row.newValue)" class="code-edit-container">
            <ms-code-edit
              v-if="visible"
              mode="json"
              :read-only="true"
              :data="getJsonValue(scope.row.newValue)"
              :modes="['json']"
              theme="eclipse" />
          </div>
          <div v-else>
            {{ scope.row.newValue }}
          </div>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script>
import MsCodeEdit from "metersphere-frontend/src/components/MsCodeEdit";
  export default {
    name: "RequestLogDetail",
    components: {MsCodeEdit},
    props: {
      title: String,
      detail: {
        Object,
      }
    },
    data() {
      return {
        visible: true
      }
    },
    watch: {
      detail() {
        this.reload();
      }
    },
    methods: {
      getJsonValue(value) {
        return JSON.stringify(value);
      },
      reload() {
        this.visible = false;
        this.$nextTick(() => {
          this.visible = true;
        });
      },
      isObject(value) {
        if (value instanceof Object) {
          return true;
        }
        return false;
      }
    }
  }
</script>

<style scoped>
.code-edit-container {
  height: 300px;
}
</style>
