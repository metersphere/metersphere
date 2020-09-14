<template>
  <ms-main-container>

    <el-table border :data="tableData" class="adjust-table table-content"
              @row-click="handleView">

      <el-table-column prop="name" :label="'连接池名称'" show-overflow-tooltip/>
      <el-table-column prop="driver" :label="'数据库驱动'"  show-overflow-tooltip/>
      <el-table-column prop="dbUrl" :label="'数据库连接URL'" show-overflow-tooltip/>
      <el-table-column prop="username" :label="'用户名'"  show-overflow-tooltip/>
      <el-table-column prop="poolMax" :label="'最大连接数'" show-overflow-tooltip/>
      <el-table-column prop="timeout" :label="'最大等待时间'"  show-overflow-tooltip/>

      <el-table-column
        :label="$t('commons.operating')" min-width="100">
        <template v-slot:default="scope">
          <ms-table-operator :is-tester-permission="true" @editClick="handleEdit(scope.row)"
                             @deleteClick="handleDelete(scope.$index)">
            <template v-slot:middle>
              <ms-table-operator-button :is-tester-permission="true" :tip="$t('commons.copy')"
                                        icon="el-icon-document-copy"
                                        type="success" @exec="handleCopy(scope.row)"/>
            </template>
          </ms-table-operator>
        </template>
      </el-table-column>

    </el-table>

    <ms-database-config-dialog :configs="tableData" ref="databaseConfigEdit"/>

  </ms-main-container>
</template>

<script>

    import {DatabaseConfig} from "../../../model/ScenarioModel";
    import MsMainContainer from "../../../../../common/components/MsMainContainer";
    import MsTableOperator from "../../../../../common/components/MsTableOperator";
    import MsTableOperatorButton from "../../../../../common/components/MsTableOperatorButton";
    import MsDatabaseConfigDialog from "./DatabaseConfigDialog";
    import {getUUID} from "../../../../../../../common/js/utils";

    export default {
      name: "MsDatabaseConfigList",
      components: {MsDatabaseConfigDialog, MsTableOperatorButton, MsTableOperator, MsMainContainer},
      props: {
        tableData: Array,
        isReadOnly: {
          type: Boolean,
          default: false
        }
      },
      data() {
        return {
          drivers: DatabaseConfig.DRIVER_CLASS,
          result: {},
        }
      },
      methods: {
        handleView() {
        },
        handleEdit(config) {
          this.$refs.databaseConfigEdit.open(config);
        },
        handleDelete(index) {
          this.tableData.splice(index, 1);
        },
        handleCopy(config) {
          let copy = {};
          Object.assign(copy, config);
          copy.id = getUUID();
          this.$refs.databaseConfigEdit.open(copy);
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
