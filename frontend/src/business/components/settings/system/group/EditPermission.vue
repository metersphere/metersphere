<template>
  <el-dialog :close-on-click-modal="false" :visible.sync="dialogVisible" width="65%"
             :title="title"
             :destroy-on-close="true"
             v-loading="result.loading"
             top="5%"
  >
    <div style="height: 60vh;overflow: auto">
      <el-table
        :span-method="objectSpanMethod"
        border
        :data="tableData"
        class="permission-table"
        style="width: 100%">
        <el-table-column
          prop="type"
          :label="$t('group.functional_menu')"
          width="180">
          <template v-slot:default="scope">
            <span v-if="scope.row.type !== 'PROJECT'">
              {{ userGroupType[scope.row.type] ? $t(userGroupType[scope.row.type]) : $t(scope.row.type) }}
            </span>
            <span v-else>{{ _computedMenuName(scope.row.resource) }}</span>
          </template>
        </el-table-column>
        <el-table-column
          prop="resource"
          :label="$t('group.operation_object')"
          width="180">
          <template v-slot:default="scope">
            {{ $t(scope.row.resource.name) }}
          </template>
        </el-table-column>
        <el-table-column
          prop="permissions"
          :label="$t('group.permission')">
          <template v-slot:default="scope">
            <group-permission :permissions="scope.row.permissions" :selected.sync="tableData" :read-only="readOnly" :group="group"/>
          </template>
        </el-table-column>
        <el-table-column
          width="50px"
          :label="$t('group.check_all')">
          <template v-slot:default="scope">
            <div style="text-align: center;">
              <el-checkbox @change="handleSelectAll($event, scope.row.permissions)" :disabled="isReadOnly(scope.row)"/>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <template v-slot:footer>
      <el-button @click="cancel" size="medium">{{ $t('commons.cancel') }}</el-button>
      <el-button type="primary" @click="onSubmit" size="medium" style="margin-top: 10px;margin-left: 5px;" :disabled="readOnly">
        {{ $t('commons.confirm') }}
      </el-button>
    </template>
  </el-dialog>
</template>

<script>
import GroupPermission from "@/business/components/settings/system/group/GroupPermission";
import {PROJECT_GROUP_SCOPE, USER_GROUP_SCOPE} from "@/common/js/table-constants";
import {hasLicense} from "@/common/js/utils";

export default {
  name: "EditPermission",
  data() {
    return {
      dialogVisible: false,
      tableData: [],
      selected: [],
      group: {},
      result: {},
      spanArr: [],
      readOnly: false,
      title: this.$t('group.set_permission')
    }
  },
  components: {
    GroupPermission,
  },
  computed: {
    userGroupType() {
      return USER_GROUP_SCOPE;
    },
    isReadOnly() {
      return function (data) {
        const isDefaultSystemGroup = this.group.id === 'admin' && data.resource.id === 'SYSTEM_GROUP';
        return this.readOnly || isDefaultSystemGroup;
      }
    }
  },
  methods: {
    open(row, readOnly, title) {
      this.readOnly = readOnly ? readOnly : false;
      this.title = title ? title : this.$t('group.set_permission');
      this.tableData = [];
      this.spanArr = [];
      this.dialogVisible = true;
      this.group = Object.assign({}, row);
      this.getGroupJson();
    },
    getGroupJson() {
      this.result = this.$post("/user/group/permission", this.group, result => {
        let data = result.data;
        if (data) {
          this.tableData = data.permissions;
          this.handleNoLicensePermissions();
          this._getUniteMenu();
        }
      })
    },
    handleNoLicensePermissions() {
      let license = hasLicense();
      if (license) {
        return;
      }
      for (let i = this.tableData.length - 1; i >= 0; i--) {
        if (this.tableData[i].resource.license) {
          this.tableData.splice(i, 1);
        } else {
          for (let j = this.tableData[i].permissions.length - 1; j >= 0; j--) {
            if (this.tableData[i].permissions[j].license) {
              this.tableData[i].permissions.splice(j, 1);
            }
          }
        }
      }
    },
    _getUniteMenu() {
      let menu = ['TRACK', 'API', 'PERFORMANCE', 'REPORT'];
      for (let i = 0; i < this.tableData.length; i++) {
        if (i === 0) {
          this.spanArr.push(1);
          this.pos = 0
        } else {
          // 判断当前元素与上一个元素是否相同
          let sign = false;
          if (this.tableData[i].type !== 'PROJECT') {
            sign = this.tableData[i].type === this.tableData[i - 1].type;
          } else {
            sign = !menu.includes(this.tableData[i].resource.id.split('_')[1]) ?
              true : this.tableData[i].resource.id.split('_')[1] === this.tableData[i - 1].resource.id.split('_')[1]
          }
          if (sign) {
            this.spanArr[this.pos] += 1;
            this.spanArr.push(0);
          } else {
            this.spanArr.push(1);
            this.pos = i;
          }
        }
      }
    },
    _computedMenuName(resource) {
      return this.$t(PROJECT_GROUP_SCOPE[resource.id.split('_')[1]]) ?
        this.$t(PROJECT_GROUP_SCOPE[resource.id.split('_')[1]]) : this.$t('permission.other.project');
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
    },
    objectSpanMethod({rowIndex, columnIndex}) {
      if (columnIndex === 0) {
        const _row = this.spanArr[rowIndex];
        const _col = _row > 0 ? 1 : 0;
        return {
          rowspan: _row,
          colspan: _col
        }
      }
    },
    handleSelectAll(check, permissions) {
      permissions.map(p => p.checked = check);
    }
  }
}
</script>

<style scoped>

</style>
