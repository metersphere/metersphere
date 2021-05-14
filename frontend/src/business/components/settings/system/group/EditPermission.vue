<template>
  <el-dialog :close-on-click-modal="false" :visible.sync="dialogVisible" width="50%"
             title="设置权限"
             :destroy-on-close="true">
    <el-table
      :data="tableData"
      style="width: 100%">
      <el-table-column
        type="selection"
        width="55">
      </el-table-column>
      <el-table-column
        prop="func"
        label="功能菜单"
        width="180">
      </el-table-column>
<!--      <el-table-column-->
<!--        prop="operators"-->
<!--        label="操作对象"-->
<!--        width="180">-->
<!--        <template v-slot:default="scope">-->
<!--          <group-operator :operators="scope.row.operators"/>-->
<!--        </template>-->
<!--      </el-table-column>-->
<!--      <el-table-column-->
<!--        prop="permission"-->
<!--        label="权限">-->
<!--        <template v-slot:default="scope">-->
<!--          {{scope.row.permissions}}-->
<!--        </template>-->
<!--      </el-table-column>-->
    </el-table>
  </el-dialog>
</template>

<script>
import GroupOperator from "@/business/components/settings/system/group/GroupOperator";

export default {
  name: "GroupPermission",
  data() {
    return {
      dialogVisible: false,
      tableData: []
    }
  },
  components: {
    GroupOperator
  },
  methods: {
    open() {
      this.dialogVisible = true;
      this.getGroupJson();
    },
    getGroupJson() {
      this.$get("/user/group/permission", result => {
        let data = result.data;
        let arr = [];
        this._getData("系统", data.system, arr);
        this._getData("组织", data.organization, arr);
        this._getData("工作空间", data.workspace, arr);
        this._getData("项目", data.project, arr);
        this.tableData = arr;
      })
    },
    _getData(type, data, arr) {
      let obj = {};
      obj.func = type;
      obj.operators = data.map(s => s.resource);
      obj.permissions = data.map(s => s.permissions);
      arr.push(obj);
    }
  }
}
</script>

<style scoped>

</style>
