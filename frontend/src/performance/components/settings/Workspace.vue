<template>
  <div v-loading="loading">
    <el-card>
      <div slot="header">
        <el-row type="flex" justify="space-between" align="middle">
          <span class="title">工作空间</span>
          <span class="search">
                    <el-input type="text" size="small" placeholder="根据名称搜索" prefix-icon="el-icon-search"
                              maxlength="60" v-model="condition" clearable/>
                </span>
        </el-row>
      </div>
      <el-table :data="items" style="width: 100%">
        <el-table-column prop="name" label="名称"/>
        <el-table-column prop="description" label="描述"/>
        <el-table-column width="100">
          <template slot-scope="scope">
            <el-button @click="edit(scope.row)" type="primary" icon="el-icon-edit" size="mini" circle
                       class="edit"/>
            <el-popconfirm title="这个工作空间确定要删除吗？" @onConfirm="del(scope.row)">
              <el-button slot="reference" type="primary" icon="el-icon-delete" size="mini"
                         circle
                         class="edit"/>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    <ms-create-box :tips="btnTips" :exec="create"/>
    <el-dialog title="创建工作空间" :visible.sync="createVisible" width="30%">
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
  import {Message} from "element-ui";

  export default {
    name: "MsWorkspace",
    components: {MsCreateBox},
    mounted() {
      this.list();
    },
    methods: {
      create() {
        this.createVisible = true;
        this.form = {};
      },
      submit(formName) {
        this.$refs[formName].validate((valid) => {
          if (valid) {
            this.loading = true;
            this.$post("/workspace/save", this.form, () => {
              this.createVisible = false;
              this.loading = false;
              this.list();
              Message.success('保存成功');
            })
          } else {
            return false;
          }
        });
      },
      edit(row) {
        this.createVisible = true;
        window.console.log(row);
        this.loading = true;
        this.form = row;

        // let self = this;
        // let getUser1 = this.$get("/test/user");
        // let getUser2 = this.$get("/test/sleep");
        // this.$all([getUser1, getUser2], function (r1, r2) {
        //   window.console.log(r1.data.data, r2.data.data);
        //   self.loading = false;
        // });
      },
      del(row) {
        this.$get('/workspace/delete/' + row.id, () => {
          Message.success('删除成功');
          this.list();
        });
        window.console.log(row);
      },
      list() {
        this.$post('/workspace/list', {}, response => {
          this.items = response.data;
        })
      }
    },
    data() {
      return {
        loading: false,
        createVisible: false,
        btnTips: "添加工作空间",
        condition: "",
        items: [],
        form: {
          // name: "",
          // description: ""
        },
        rules: {
          name: [
            {required: true, message: '请输入工作空间名称', trigger: 'blur'},
            {min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur'}
          ]
        },
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
</style>
