<template>
  <div v-loading="result.loading">

    <el-card>
      <div slot="header">
        <el-row type="flex" justify="space-between" align="middle">
          <span class="title">{{$t('commons.organization')}}
            <ms-create-box :tips="btnTips" :exec="create"/>
          </span>
          <span class="search">
            <el-input type="text" size="small" :placeholder="$t('organization.search_by_name')" prefix-icon="el-icon-search"
                              maxlength="60" v-model="condition" clearable/>
          </span>
        </el-row>
      </div>
      <!-- system menu organization table-->
      <el-table :data="tableData" style="width: 100%">
        <el-table-column type="selection" width="55"/>
        <el-table-column prop="name" :label="$t('commons.name')"/>
        <el-table-column prop="description" :label="$t('commons.description')"/>
        <el-table-column :label="$t('commons.member')">
          <template slot-scope="scope">
            <el-button type="text" class="member-size" @click="cellClick(scope.row)">{{scope.row.memberSize}}</el-button>
          </template>
        </el-table-column>
        <el-table-column :label="$t('commons.operating')">
          <template slot-scope="scope">
            <el-button @click="edit(scope.row)" type="primary" icon="el-icon-edit" size="mini" circle/>
            <el-button @click="del(scope.row)" type="danger" icon="el-icon-delete" size="mini" circle/>
          </template>
        </el-table-column>
      </el-table>
      <div>
        <el-row>
          <el-col :span="22" :offset="1">
            <div class="table-page">
              <el-pagination
                @size-change="handleSizeChange"
                @current-change="handleCurrentChange"
                :current-page.sync="currentPage"
                :page-sizes="[5, 10, 20, 50, 100]"
                :page-size="pageSize"
                layout="total, sizes, prev, pager, next, jumper"
                :total="total">
              </el-pagination>
            </div>
          </el-col>
        </el-row>
      </div>
    </el-card>
    <!-- dialog of organization member -->
    <el-dialog :visible.sync="memberVisible" width="70%" :destroy-on-close="true" @close="closeMemberFunc">
      <el-row type="flex" justify="space-between" align="middle">
        <span class="member-title">{{$t('commons.member')}}
          <ms-create-box :tips="btnTips" :exec="addMember"/>
        </span>
        <span class="search">
          <el-input type="text" size="small"
                    :placeholder="$t('organization.search_by_name')"
                    prefix-icon="el-icon-search"
                    maxlength="60" v-model="condition" clearable/>
        </span>
      </el-row>
      <!-- organization member table -->
      <el-table :data="memberLineData" style="width: 100%">
        <el-table-column prop="name" :label="$t('commons.username')"/>
        <el-table-column prop="email" :label="$t('commons.email')"/>
        <el-table-column prop="phone" :label="$t('commons.phone')"/>
        <el-table-column :label="$t('commons.role')" width="140">
          <template slot-scope="scope">
            <el-tag v-for="(role, index) in scope.row.roles" :key="index" size="mini" effect="dark">
              {{ role.name }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="$t('commons.operating')">
          <template slot-scope="scope">
            <el-button @click="editMember(scope.row)" type="primary" icon="el-icon-edit" size="mini" circle/>
            <el-button @click="delMember(scope.row)" type="danger" icon="el-icon-delete" size="mini" circle/>
          </template>
        </el-table-column>
      </el-table>
      <div>
        <el-row>
          <el-col :span="22" :offset="1">
            <div class="table-page">
              <el-pagination
                @size-change="handleMemberSizeChange"
                @current-change="handleMemberCurrentChange"
                :current-page.sync="currentMemberPage"
                :page-sizes="[5, 10, 20, 50, 100]"
                :page-size="pageMemberSize"
                layout="total, sizes, prev, pager, next, jumper"
                :total="memberTotal">
              </el-pagination>
            </div>
          </el-col>
        </el-row>
      </div>
    </el-dialog>

    <!-- add organization form -->
    <el-dialog :title="$t('organization.create')" :visible.sync="createVisible" width="30%" @closed="closeFunc" :destroy-on-close="true">
      <el-form :model="form" label-position="left" label-width="100px" size="small" :rules="rule" ref="createOrganization">
        <el-form-item :label="$t('commons.name')" prop="name">
          <el-input v-model="form.name" autocomplete="off"/>
        </el-form-item>
        <el-form-item :label="$t('commons.description')" prop="description">
          <el-input v-model="form.description" autocomplete="off"/>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button type="primary" @click="createOrganization('createOrganization')" size="medium">{{$t('commons.save')}}</el-button>
      </span>
    </el-dialog>

    <!-- update organization form -->
    <el-dialog :title="$t('organization.modify')" :visible.sync="updateVisible" width="30%" :destroy-on-close="true" @close="closeFunc">
      <el-form :model="form" label-position="left" label-width="100px" size="small" :rules="rule" ref="updateOrganizationForm">
        <el-form-item :label="$t('commons.name')" prop="name">
          <el-input v-model="form.name" autocomplete="off"/>
        </el-form-item>
        <el-form-item :label="$t('commons.description')" prop="description">
          <el-input v-model="form.description" autocomplete="off"/>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button type="primary" @click="updateOrganization('updateOrganizationForm')" size="medium">{{$t('organization.modify')}}</el-button>
      </span>
    </el-dialog>

    <!-- add organization member form -->
    <el-dialog :title="$t('member.create')" :visible.sync="addMemberVisible" width="30%" :destroy-on-close="true" @close="closeFunc">
      <el-form :model="memberForm" ref="form" :rules="orgMemberRule" label-position="left" label-width="100px" size="small">
        <el-form-item :label="$t('commons.member')" prop="userIds">
          <el-select v-model="memberForm.userIds" multiple :placeholder="$t('member.please_choose_member')" class="select-width">
            <el-option
              v-for="item in memberForm.userList"
              :key="item.id"
              :label="item.name"
              :value="item.id">
              <span class="org-member-name">{{ item.name }}</span>
              <span class="org-member-email">{{ item.email }}</span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('commons.role')" prop="roleIds">
          <el-select v-model="memberForm.roleIds" multiple :placeholder="$t('role.please_choose_role')" class="select-width">
            <el-option
              v-for="item in memberForm.roles"
              :key="item.id"
              :label="item.name"
              :value="item.id">
            </el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm('form')" size="medium">{{$t('commons.save')}}</el-button>
      </span>
    </el-dialog>

    <!-- update organization member form -->
    <el-dialog :title="$t('member.modify')" :visible.sync="updateMemberVisible" width="30%" :destroy-on-close="true" @close="closeFunc">
      <el-form :model="memberForm" label-position="left" label-width="100px" size="small" ref="updateUserForm">
        <el-form-item label="ID" prop="id">
          <el-input v-model="memberForm.id" autocomplete="off" :disabled="true"/>
        </el-form-item>
        <el-form-item :label="$t('commons.username')" prop="name">
          <el-input v-model="memberForm.name" autocomplete="off"/>
        </el-form-item>
        <el-form-item :label="$t('commons.email')" prop="email">
          <el-input v-model="memberForm.email" autocomplete="off"/>
        </el-form-item>
        <el-form-item :label="$t('commons.phone')" prop="phone">
          <el-input v-model="memberForm.phone" autocomplete="off"/>
        </el-form-item>
        <el-form-item :label="$t('commons.role')" prop="roleIds">
          <el-select v-model="memberForm.roleIds" multiple :placeholder="$t('role.please_choose_role')" class="select-width">
            <el-option
              v-for="item in memberForm.allroles"
              :key="item.id"
              :label="item.name"
              :value="item.id">
            </el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button type="primary" @click="updateOrgMember('updateUserForm')" size="medium">{{$t('commons.save')}}</el-button>
      </span>
    </el-dialog>

  </div>
</template>

<script>
  import MsCreateBox from "../CreateBox";

  export default {
    name: "MsOrganization",
    components: {MsCreateBox},
    data() {
      return {
        queryPath: '/organization/list',
        deletePath: '/organization/delete/',
        createPath: '/organization/add',
        updatePath: '/organization/update',
        result: {},
        createVisible: false,
        updateVisible: false,
        memberVisible: false,
        addMemberVisible: false,
        updateMemberVisible: false,
        multipleSelection: [],
        currentPage: 1,
        pageSize: 5,
        total: 0,
        currentMemberPage: 1,
        pageMemberSize: 5,
        memberTotal: 0,
        currentRow: {},
        btnTips: this.$t('member.create'),
        condition: "",
        tableData: [],
        memberLineData: [],
        form: {},
        memberForm: {},
        rule: {
          name: [
            {required: true, message: this.$t('organization.input_name'), trigger: 'blur'},
            { min: 2, max: 10, message: this.$t('commons.input_limit', [2, 50]), trigger: 'blur' },
            {
              required: true,
              pattern: /^[\u4e00-\u9fa5_a-zA-Z0-9.·-]+$/,
              message: this.$t('organization.special_characters_are_not_supported'),
              trigger: 'blur'
            }
          ],
          description: [
            { max: 50, message: this.$t('commons.input_limit', [0, 50]), trigger: 'blur'}
          ]
        },
        orgMemberRule: {
          userIds: [
            {required: true, message: this.$t('member.please_choose_member'), trigger: ['blur']}
          ],
          roleIds: [
            {required: true, message: this.$t('role.please_choose_role'), trigger: ['blur']}
          ]
        }
      }
    },
    created() {
      this.initTableData();
    },
    methods: {
      create() {
        this.createVisible = true;
      },
      addMember() {
        this.addMemberVisible = true;
        this.memberForm = {};
        this.result = this.$get('/user/list/', response => {
          this.$set(this.memberForm, "userList", response.data);
        });
        this.result = this.$get('/role/list/org', response => {
          this.$set(this.memberForm, "roles", response.data);
        })
      },
      edit(row) {
        this.updateVisible = true;
        this.form = row;
      },
      editMember(row) {
        this.updateMemberVisible = true;
        this.memberForm = row;
        let roleIds = this.memberForm.roles.map(r => r.id);
        this.result = this.$get('/role/list/org', response => {
          this.$set(this.memberForm, "allroles", response.data);
        })
        // 编辑时填充角色信息
        this.$set(this.memberForm, 'roleIds', roleIds);
      },
      cellClick(row){
        // 保存当前点击的组织信息到currentRow
        this.currentRow = row;
        this.memberVisible = true;
        let param = {
          name: '',
          organizationId: row.id
        };
        let path = "/user/special/org/member/list";
        this.result = this.$post(this.buildPagePath(path), param, res => {
          let data = res.data;
          this.memberLineData = data.listObject;
          let url = "/userrole/list/org/" + row.id;
          for (let i = 0; i < this.memberLineData.length; i++) {
            this.$get(url + "/" + this.memberLineData[i].id, response => {
              let roles = response.data;
              this.$set(this.memberLineData[i], "roles", roles);
            })
          }
          this.memberTotal = data.itemCount;
        });
      },
      del(row) {
        this.$confirm(this.$t('organization.delete_confirm'), '', {
          confirmButtonText: this.$t('commons.confirm'),
          cancelButtonText: this.$t('commons.cancel'),
          type: 'warning'
        }).then(() => {
          this.result = this.$get(this.deletePath + row.id,() => {
            this.$message({
              type: 'success',
              message: this.$t('commons.delete_success')
            });
            this.initTableData()
          });
        }).catch(() => {
          this.$message({
            type: 'info',
            message: this.$t('commons.delete_cancel')
          });
        });
      },
      delMember(row) {
        this.$confirm(this.$t('member.delete_confirm'), '', {
          confirmButtonText: this.$t('commons.confirm'),
          cancelButtonText: this.$t('commons.cancel'),
          type: 'warning'
        }).then(() => {
          this.result = this.$get('/user/special/org/member/delete/' + this.currentRow.id + '/' + row.id, () => {
            this.$message({
              type: 'success',
              message: this.$t('commons.delete_success')
            });
            this.cellClick(this.currentRow);
          });
        }).catch(() => {
          this.$message({
            type: 'info',
            message: this.$t('commons.delete_cancel')
          });
        });
      },
      createOrganization(createOrganizationForm) {
        this.$refs[createOrganizationForm].validate( valide => {
          if (valide) {
            this.result = this.$post(this.createPath, this.form,() => {
              this.$message({
                type: 'success',
                message: this.$t('commons.save_success')
              });
              this.initTableData();
              this.createVisible = false;
              });
          } else {
            return false;
          }
        })
      },
      updateOrganization(udpateOrganizationForm) {
        this.$refs[udpateOrganizationForm].validate(valide => {
          if (valide) {
            this.result = this.$post(this.updatePath, this.form,() => {
              this.$message({
                type: 'success',
                message: this.$t('commons.modify_success')
              });
              this.updateVisible = false;
              this.initTableData();
              });
          } else {
            return false;
          }
        })
      },
      initTableData() {
        this.result = this.$post(this.buildPagePath(this.queryPath),{},response => {
          let data = response.data;
          this.tableData = data.listObject;
          for (let i = 0; i < this.tableData.length; i++) {
            let param = {
              name: '',
              organizationId: this.tableData[i].id
            }
            let path = "user/special/org/member/list/all";
            this.$post(path, param, res => {
              let member = res.data;
              this.$set(this.tableData[i], "memberSize", member.length);
            })
          }
          this.total = data.itemCount;
        })
      },
      closeFunc() {
        this.form = {};
      },
      closeMemberFunc() {
        this.memberLineData = [];
        this.initTableData();
      },
      buildPagePath(path) {
        return path + "/" + this.currentPage + "/" + this.pageSize;
      },
      handleSizeChange(size) {
        this.pageSize = size;
        this.initTableData();
      },
      handleCurrentChange(current) {
        this.currentPage = current;
        this.initTableData();
      },
      handleMemberSizeChange(size) {
        this.pageMemberSize = size;
        this.cellClick(this.currentRow);
      },
      handleMemberCurrentChange(current) {
        this.currentMemberPage = current;
        this.cellClick(this.currentRow);
      },
      handleSelectionChange(val) {
        this.multipleSelection = val;
      },
      submitForm(formName) {
        this.$refs[formName].validate((valid) => {
          if (valid) {
            let param = {
              userIds: this.memberForm.userIds,
              roleIds: this.memberForm.roleIds,
              organizationId: this.currentRow.id
            };
            this.result = this.$post("user/special/org/member/add", param,() => {
              this.cellClick(this.currentRow);
              this.addMemberVisible = false;
            })
          } else {
            return false;
          }
        });
      },
      updateOrgMember() {
        let param = {
          id: this.memberForm.id,
          name: this.memberForm.name,
          email: this.memberForm.email,
          phone: this.memberForm.phone,
          roleIds: this.memberForm.roleIds,
          organizationId: this.currentRow.id
        }
        this.result = this.$post("/organization/member/update", param,() => {
          this.$message({
            type: 'success',
            message: this.$t('commons.modify_success')
          });
          this.updateMemberVisible = false;
          this.cellClick(this.currentRow);
        });
      },
    }

  }
</script>

<style scoped>
  .search {
    width: 240px;
  }

  .table-page {
    padding-top: 20px;
    margin-right: -9px;
    float: right;
  }

  .member-size {
    text-decoration:underline;
    cursor:pointer;
  }

  .org-member-name {
    float: left;
  }

  .org-member-email {
    float: right;
    color: #8492a6;
    font-size: 13px;
  }

  .select-width {
    width: 100%;
  }

  .member-title {
    margin-bottom: 30px;
  }
</style>
