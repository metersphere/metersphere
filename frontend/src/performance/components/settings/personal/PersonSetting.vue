<template>
  <div v-loading="result.loading">
    <el-card>

      <div slot="header">
        <el-row type="flex" justify="space-between" align="middle">
          <span class="title">个人信息</span>
        </el-row>
      </div>

      <el-table :data="tableData" style="width: 100%">
        <el-table-column prop="id" label="ID"/>
        <el-table-column prop="name" label="用户名" width="120"/>
        <el-table-column prop="email" label="邮箱"/>
        <el-table-column prop="phone" label="电话"/>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template slot-scope="scope">
            <span>{{ scope.row.createTime | timestampFormatDate }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作">
          <template slot-scope="scope">
            <el-button @click="edit(scope.row)" type="primary" icon="el-icon-edit" size="mini" circle/>
          </template>
        </el-table-column>
      </el-table>

      <el-dialog title="修改个人信息" :visible.sync="updateVisible" width="30%" :destroy-on-close="true" @close="closeFunc">
        <el-form :model="form" label-position="left" label-width="100px" size="small" :rules="rule" ref="updateUserForm">
          <el-form-item label="ID" prop="id">
            <el-input v-model="form.id" autocomplete="off" :disabled="true"/>
          </el-form-item>
          <el-form-item label="用户名" prop="name">
            <el-input v-model="form.name" autocomplete="off"/>
          </el-form-item>
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="form.email" autocomplete="off"/>
          </el-form-item>
          <el-form-item label="电话" prop="phone">
            <el-input v-model="form.phone" autocomplete="off"/>
          </el-form-item>
        </el-form>
        <span slot="footer" class="dialog-footer">
          <el-button type="primary" @click="updateUser('updateUserForm')" size="medium">修改</el-button>
        </span>
      </el-dialog>

    </el-card>
  </div>
</template>

<script>
  import {TokenKey} from "../../../../common/constants";

  export default {
    data() {
      return {
        result: {},
        updateVisible: false,
        tableData: [],
        updatePath: '/user/update/currentuser',
        form: {},
        rule: {
          name: [
            {required: true, message: '请输入姓名', trigger: 'blur'},
            { min: 2, max: 10, message: '长度在 2 到 10 个字符', trigger: 'blur' },
            {
              required: true,
              pattern: /^[\u4e00-\u9fa5_a-zA-Z0-9.·-]+$/,
              message: '姓名不支持特殊字符',
              trigger: 'blur'
            }
          ],
          phone: [
            {
              required: false,
              pattern: '^1(3|4|5|7|8)\\d{9}$',
              message: '手机号码格式不正确！',
              trigger: 'blur'
            }
          ],
          email: [
            { required: true, message: '请输入邮箱', trigger: 'blur' },
            {
              required: true,
              pattern: /^([A-Za-z0-9_\-.])+@([A-Za-z0-9]+\.)+[A-Za-z]{2,6}$/,
              message: '邮箱格式不正确！',
              trigger: 'blur'
            }
          ]
        }
      }
    },
    name: "MsPersonSetting",
    created() {
      this.initTableData();
    },
    methods: {
      currentUser: () => {
        let user = localStorage.getItem(TokenKey);
        return JSON.parse(user);
      },
      edit(row) {
        this.updateVisible = true;
        this.form = row;
      },
      updateUser(updateUserForm) {
        this.$refs[updateUserForm].validate(valide => {
          if (valide) {
            this.result = this.$post(this.updatePath, this.form,response => {
              this.$message({
                type: 'success',
                message: '修改成功!'
              });
              localStorage.setItem(TokenKey, JSON.stringify(response.data));
              this.updateVisible = false;
              this.initTableData();
              window.location.reload();
            });
          } else {
            return false;
          }
        })
      },
      initTableData() {
        this.result = this.$get("/user/info/" + this.currentUser().id, response => {
          let data = response.data;
          let dataList = [];
          dataList[0] = data;
          this.tableData = dataList;
        })
      },
      closeFunc() {
        this.form = {};
      }
    }
  }
</script>

<style scoped>

</style>
