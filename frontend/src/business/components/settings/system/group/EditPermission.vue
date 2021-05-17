<template>
  <el-dialog :close-on-click-modal="false" :visible.sync="dialogVisible" width="65%"
             title="设置权限"
             :destroy-on-close="true"
             v-loading="result.loading"
  >
    <div style="height: 500px;overflow: auto">
      <el-table
        :data="tableData"
        style="width: 100%">
<!--        <el-table-column-->
<!--          type="selection"-->
<!--          width="55">-->
<!--        </el-table-column>-->
        <el-table-column
          prop="resource"
          label="操作对象"
          width="180">
          <template v-slot:default="scope">
            {{scope.row.resource.name}}
          </template>
        </el-table-column>
        <el-table-column
          prop="permissions"
          label="权限">
          <template v-slot:default="scope">
            <group-permission :permissions="scope.row.permissions" :selected.sync="tableData"/>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <el-button type="primary" @click="onSubmit" size="small" style="margin-top: 10px;margin-left: 5px;">确定</el-button>
    <el-button @click="cancel" size="small">取消</el-button>
  </el-dialog>
</template>

<script>
import GroupOperator from "@/business/components/settings/system/group/GroupOperator";
import GroupPermission from "@/business/components/settings/system/group/GroupPermission";

export default {
  name: "EditPermission",
  component: {
    GroupPermission,
  },
  data() {
    return {
      dialogVisible: false,
      tableData: [],
      selected: [],
      group: {},
      result: {}
    }
  },
  components: {
    GroupPermission,
    GroupOperator
  },
  methods: {
    open(row) {
      this.tableData = [];
      this.dialogVisible = true;
      this.group = Object.assign({}, row);
      this.getGroupJson();
    },
    getGroupJson() {
      this.result = this.$post("/user/group/permission", this.group, result => {
        let data = result.data;
        if (data) {
          this.tableData = data.permissions;
        }
      })
    },
    onSubmit() {
      let param = {};
      let permissions = [];
      this.tableData.forEach(td => {
        permissions.push(...td.permissions);
      })
      param.userGroupId = this.group.id;
      param.permissions = permissions;
      this.result = this.$post('/user/group/permission/edit', param, () => {
        this.$success(this.$t('commons.save_success'));
        this.dialogVisible = false;
      })
    },
    cancel() {
      this.dialogVisible = false;
    }
  }
}
</script>

<style scoped>

</style>
