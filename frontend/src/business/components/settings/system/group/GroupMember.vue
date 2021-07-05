<template>
  <div>
    <el-dialog :close-on-click-modal="false" :visible.sync="visible" width="65%" top="15vh"
               :destroy-on-close="true" @close="close" v-loading="result.loading"
               class="group-member"
    >
      <template v-slot:title>
        <ms-table-header :condition.sync="condition" @create="addMemberBtn" @search="search"
                         :create-tip="$t('member.create')" :title="$t('commons.member')"/>
      </template>
      <el-table :border="true" class="adjust-table" :data="memberData" style="width: 100%;margin-top:5px;">
        <el-table-column prop="id" label="ID"/>
        <el-table-column prop="name" :label="$t('commons.username')" show-overflow-tooltip/>
        <el-table-column prop="email" :label="$t('commons.email')" show-overflow-tooltip/>
        <el-table-column prop="phone" :label="$t('commons.phone')" show-overflow-tooltip>
          <template v-slot="scope">
            {{ scope.row.phone || '-' }}
          </template>
        </el-table-column>
        <el-table-column :label="typeLabel" v-if="showTypeLabel">
          <template v-slot:default="scope">
            <el-popover
              placement="top"
              width="250"
              trigger="click"
            >
              <div v-loading="sourceResult.loading" style="height: 150px;overflow: auto;">
                <el-tag
                  v-for="item in groupSource"
                  :key="item.id"
                  :type="item.name"
                  size="small"
                  style="margin-left: 5px;margin-top: 5px;"
                >
                  {{ item.name }}
                </el-tag>
              </div>
              <el-link type="primary" @click="getGroupSource(scope.row)" slot="reference">点击查看</el-link>
            </el-popover>
          </template>
        </el-table-column>
        <el-table-column :label="$t('commons.operating')">
          <template v-slot:default="scope">
            <div>
              <ms-table-operator :tip2="$t('commons.remove')"
                                 :show-edit="showTypeLabel"
                                 @editClick="editMemberBtn(scope.row)"
                                 @deleteClick="removeMember(scope.row)"/>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <ms-table-pagination :change="search" :current-page.sync="currentPage"
                           :page-size.sync="pageSize"
                           :total="total"/>
    </el-dialog>
    <el-dialog :close-on-click-modal="false" :visible.sync="memberVisible" width="45%"
               :title="title" :destroy-on-close="true" v-loading="memberResult.loading" @close="memberDialogClose">
      <el-form ref="memberFrom" label-position="right" :model="form" size="small" :rules="rules" label-width="100px"
               style="margin-right: 40px;">
        <el-form-item :label="$t('commons.member')" prop="userIds">
          <el-select
            v-model="form.userIds"
            multiple
            filterable
            :popper-append-to-body="false"
            style="width: 100%"
            :disabled="userSelectDisable"
            :placeholder="$t('member.please_choose_member')">
            <el-option
              v-for="item in users"
              :key="item.id"
              :label="item.id"
              :value="item.id">
              <template>
                <span class="user-select-left">{{ item.name }} ({{ item.id }})</span>
                <span class="user-select-right">{{ item.email }}</span>
              </template>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item :label="typeLabel" v-if="showTypeLabel" prop="sourceIds">
          <el-select v-model="form.sourceIds" :placeholder="typeLabel" style="width: 100%;" clearable multiple
                     filterable>
            <el-option v-for="item in sourceData" :key="item.id" :label="item.name" :value="item.id"/>
          </el-select>
        </el-form-item>
      </el-form>
      <template v-slot:footer>
        <div class="dialog-footer">
          <el-button @click="memberVisible = false" size="medium">{{ $t('commons.cancel') }}</el-button>
          <el-button type="primary" @click="addMember" @keydown.enter.native.prevent size="medium">
            {{ $t('commons.confirm') }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import MsTableHeader from "@/business/components/common/components/MsTableHeader";
import MsTablePagination from "@/business/components/common/pagination/TablePagination";
import {GROUP_ORGANIZATION, GROUP_PROJECT, GROUP_SYSTEM, GROUP_WORKSPACE} from "@/common/js/constants";
import MsTableOperator from "@/business/components/common/components/MsTableOperator";

export default {
  name: "GroupMember",
  components: {
    MsTableHeader,
    MsTablePagination,
    MsTableOperator
  },
  data() {
    return {
      visible: false,
      memberVisible: false,
      condition: {},
      memberData: [],
      currentPage: 1,
      pageSize: 5,
      total: 0,
      result: {},
      sourceResult: {},
      memberResult: {},
      group: {},
      groupSource: [],
      sourceData: [],
      users: [],
      form: {},
      title: '',
      submitType: '',
      userSelectDisable: false,
      rules: {
        userIds: {required: true, message: this.$t('member.please_choose_member'), trigger: 'blur'},
        sourceIds: {required: true, message: this.$t('group.select_belong_source'), trigger: 'blur'}
      }
    }
  },
  computed: {
    typeLabel() {
      let type = this.group.type;
      if (type === GROUP_ORGANIZATION) {
        return this.$t('group.belong_organization');
      }
      if (type === GROUP_WORKSPACE) {
        return this.$t('group.belong_workspace');
      }
      if (type === GROUP_PROJECT) {
        return this.$t('group.belong_project');
      }
      return '';
    },
    showTypeLabel() {
      return this.group.type !== GROUP_SYSTEM;
    }
  },
  methods: {
    init() {
      this.condition.userGroupId = this.group.id;
      this.result = this.$post("/user/group/user/" + this.currentPage + "/" + this.pageSize, this.condition, res => {
        let data = res.data;
        if (data) {
          let {itemCount, listObject} = data;
          this.total = itemCount;
          this.memberData = listObject;
        }
      })
    },
    open(group) {
      this.visible = true;
      this.group = group;
      this.init();
    },
    close() {
      this.visible = false;
      this.$emit("refresh");
    },
    addMemberBtn() {
      this.title = this.$t('member.create');
      this.memberVisible = true;
      this.submitType = 'ADD';
      this.getUser();
      this.getResource();
    },
    search() {
      this.init();
    },
    editMemberBtn(row) {
      this.title = this.$t('member.modify');
      this.userSelectDisable = true;
      this.memberVisible = true;
      this.submitType = 'EDIT';
      this.getUser();
      this.getResource();
      this.$get('/user/group/source/' + row.id + "/" + this.group.id, res => {
        let data = res.data;
        let userIds = [row.id];
        let sourceIds = data.map(d => d.id);
        this.$set(this.form, 'userIds', userIds);
        this.$set(this.form, 'sourceIds', sourceIds);
      })
    },
    editMember() {
      this.form.groupId = this.group.id;
      this.$refs['memberFrom'].validate(valid => {
        if (valid) {
          this.result = this.$post('/user/group/edit/member', this.form, () => {
            this.$success(this.$t('commons.save_success'));
            this.init();
            this.memberVisible = false;
          });
        } else {
          return false;
        }
      })
    },
    getUser() {
      this.memberResult = this.$get('/user/list/', response => {
        this.users = response.data;
      })
    },
    removeMember(row) {
      this.$confirm(this.$t('member.remove_member').toString(), '', {
        confirmButtonText: this.$t('commons.confirm'),
        cancelButtonText: this.$t('commons.cancel'),
        type: 'warning'
      }).then(() => {
        this.result = this.$get('/user/group/rm/' + row.id + '/' + this.group.id, () => {
          this.$success(this.$t('commons.remove_success'));
          this.init();
        });
      }).catch(() => {
        this.$info(this.$t('commons.remove_cancel'));
      });
    },
    getGroupSource(row) {
      this.groupSource = [];
      this.sourceResult = this.$get('/user/group/source/' + row.id + "/" + this.group.id, res => {
        this.groupSource = res.data;
      })
    },
    addMember() {
      if (this.submitType === 'ADD') {
        this._addMember();
      } else if (this.submitType === 'EDIT') {
        this.editMember();
      }
    },
    _addMember() {
      this.form.groupId = this.group.id;
      this.$refs['memberFrom'].validate(valid => {
        if (valid) {
          this.result = this.$post('/user/group/add/member', this.form, () => {
            this.$success(this.$t('commons.save_success'));
            this.init();
            this.memberVisible = false;
          });
        } else {
          return false;
        }
      })
    },
    getResource() {
      this.memberResult = this.$get('/organization/list/resource/' + this.group.id + "/" + this.group.type, res => {
        let data = res.data;
        if (data) {
          this._setResource(this.group.type, data);
        }
      })
    },
    _setResource(type, data) {
      switch (type) {
        case GROUP_ORGANIZATION:
          this.sourceData = data.organizations;
          break;
        case GROUP_WORKSPACE:
          this.sourceData = data.workspaces;
          break;
        case GROUP_PROJECT:
          this.sourceData = data.projects;
          break;
        default:
      }
    },
    memberDialogClose() {
      this.form = {};
      this.memberVisible = false;
      this.userSelectDisable = false;
    }
  }
}
</script>

<style scoped>
/*.group-member >>> .el-dialog__header {*/
/*  padding: 0;*/
/*}*/

.user-select-left {
  float: left;
}

.user-select-right {
  float: right;
  margin-right: 18px;
  color: #8492a6;
  font-size: 13px;
}
</style>
