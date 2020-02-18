<template>
  <div v-loading="loading">
    <el-card>
      <div slot="header">
        <el-row type="flex" justify="space-between" align="middle">
          <span class="title">成员
            <ms-create-box :tips="btnTips" :exec="create"/>
          </span>
          <span class="search">
                    <el-input type="text" size="small" placeholder="根据名称搜索" prefix-icon="el-icon-search"
                              maxlength="60" v-model="condition" @change="search" clearable/>
          </span>
        </el-row>
      </div>
      <el-table :data="tableData" style="width: 100%">
        <el-table-column prop="name" label="用户名"/>
        <el-table-column prop="email" label="邮箱"/>
        <el-table-column prop="phone" label="电话"/>
        <el-table-column>
          <template slot-scope="scope">
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

    <el-dialog title="添加成员" :visible.sync="createVisible" width="30%">
      <el-form :model="form" :rules="rules" ref="form" label-position="left" label-width="100px" size="small">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" autocomplete="off"/>
        </el-form-item>
        <el-form-item label="描述">
          <el-input type="textarea" v-model="form.description"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submit('form')" size="medium">创建</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
  import MsCreateBox from "./CreateBox";
  // import {Message} from "element-ui";

  export default {
    name: "Member",
    components: {MsCreateBox},
    data() {
      return {
        loading: false,
        form: {},
        btnTips: "添加成员",
        createVisible: false,
        queryPath: "/user/member/list",
        condition: "",
        tableData: [],
        multipleSelection: [],
        currentWorkspaceId: "0a2430b1-a818-4b9b-bc04-c1229c472896",
        currentPage: 1,
        pageSize: 5,
        total: 0,
      }
    },
    created: function () {
      this.initTableData();
    },
    methods: {
      initTableData() {
        let param = {
          name: this.condition,
          workspaceId: this.currentWorkspaceId
        };

        this.$post(this.buildPagePath(this.queryPath), param).then(response => {
          if (response.data.success) {
            let data = response.data.data;
            this.total = data.itemCount;
            this.tableData = data.listObject;
          } else {
            this.$message.error(response.message);
          }
        })
      },
      buildPagePath(path) {
        return path + "/" + this.currentPage + "/" + this.pageSize;
      },
      search() {

      },
      handleSizeChange(size) {
        this.pageSize = size;
      },
      handleCurrentChange(current) {
        this.currentPage = current;
      },
      del(row) {
        this.$confirm('移除该成员, 是否继续?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          this.$get('/user/member/delete/' + this.currentWorkspaceId + '/' + row.id).then(() => {
            this.initTableData();
          });
          this.$message({
            type: 'success',
            message: '删除成功!'
          });
        }).catch(() => {
          this.$message({
            type: 'info',
            message: '已取消删除'
          });
        });
      },
      create() {
        this.createVisible = true;
        this.form = {};
      }
    }
  }
</script>

<style scoped>
  .search {
    width: 240px;
  }

  .edit {
    opacity: 0;
  }

  .el-table__row:hover .edit {
    opacity: 1;
  }

  .table-page {
    padding-top: 20px;
    margin-right: -9px;
    float: right;
  }
</style>
