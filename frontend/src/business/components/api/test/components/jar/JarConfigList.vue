<template>
  <div class="jar-config-list">

    <el-table border :data="tableData"
              class="adjust-table table-content"
              highlight-current-row
              @row-click="handleView">

      <el-table-column prop="name" :label="$t('commons.name')" show-overflow-tooltip/>
      <el-table-column prop="fileName" :label="$t('api_test.jar_config.jar_file')"  show-overflow-tooltip/>
      <el-table-column prop="description" :label="$t('commons.description')" show-overflow-tooltip/>
      <!--<el-table-column prop="createTime" :label="$t('创建时间')"  show-overflow-tooltip/>-->
      <el-table-column prop="updateTime" :label="$t('commons.update_time')" show-overflow-tooltip/>
      <el-table-column prop="owner" :label="$t('report.user_name')"  show-overflow-tooltip/>

      <el-table-column :label="$t('commons.operating')" min-width="100">
        <template v-slot:default="scope">
          <ms-table-operator-button :is-tester-permission="true" :tip="$t('api_test.scenario.reference')" icon="el-icon-connection" type="success" @exec="handleCopy(scope.$index, scope.row)"/>
          <ms-table-operator-button :isTesterPermission="true" :tip="$t('commons.delete')" icon="el-icon-delete" type="danger" @exec="handleDelete(scope.row.id)"/>
        </template>
      </el-table-column>

    </el-table>

  </div>
</template>

<script>

    import MsTableOperator from "../../../../common/components/MsTableOperator";
    import MsTableOperatorButton from "../../../../common/components/MsTableOperatorButton";

    export default {
      name: "MsJarConfigList",
      components: {MsTableOperatorButton, MsTableOperator},
      props: {
        tableData: Array,
        isReadOnly: {
          type: Boolean,
          default: false
        }
      },
      data() {
        return {
          result: {},
        }
      },
      methods: {
        handleView(row) {
          this.$emit('rowSelect', row);
        },
        handleDelete(id) {
          this.result = this.$get("/api/jar/delete/" + id, () => {
            this.$success(this.$t('commons.delete_success'));
            this.$emit('refresh');
          });
        },
        handleCopy(index, config) {

        }
      }
    }
</script>

<style scoped>

</style>
