<template>
  <div class="project-container">
    <div class="main-content">
      <el-card>
        <div slot="header">
          <el-row type="flex" justify="space-between" align="middle">
            <span class="title">
              项目
              <ms-create-box :tips="btnTips" :exec="create"/>
            </span>
            <span class="search">
                    <el-input type="text" size="small" placeholder="根据名称搜索" prefix-icon="el-icon-search"
                              maxlength="60" v-model="condition" clearable/>
                </span>
          </el-row>
        </div>
        <el-table :data="items" style="width: 100%">
          <el-table-column prop="name" label="名称"/>
          <el-table-column prop="description" label="描述"/>
          <el-table-column>
            <template slot-scope="scope">
              <el-button @click="edit(scope.row)" type="primary" icon="el-icon-edit" size="mini" circle/>
              <el-button @click="del(scope.row)" type="danger" icon="el-icon-delete" size="mini" circle/>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <el-dialog title="创建项目" :visible.sync="createVisible">
        <el-form :model="form" :rules="rules" ref="form" label-position="left" label-width="100px" size="small">
          <el-form-item label="名称">
            <el-input v-model="form.name" autocomplete="off"></el-input>
          </el-form-item>
          <el-form-item label="描述">
            <el-input type="textarea" v-model="form.description"></el-input>
          </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">
          <el-button type="primary" @click="submit('form')" size="medium">创建</el-button>
        </div>
      </el-dialog>
    </div>
  </div>
</template>

<script>
  import MsCreateBox from "../settings/CreateBox";
  import {Message} from "element-ui";

  export default {
    name: "MsProject",
    components: {MsCreateBox},
    data() {
      return {
        createVisible: false,
        loading: false,
        btnTips: "添加项目",
        condition: "",
        items: [],
        form: {},
        rules: {
          name: [
            {required: true, message: '请输入项目名称', trigger: 'blur'},
            {min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur'}
          ]
        },
      }
    },
    mounted() {
      // this.list();
    },
    destroyed() {
      this.createVisible = false;
    },
    methods: {
      create() {
        this.createVisible = true;
        this.form = {};
      },
      edit(row) {
        this.createVisible = true;
        this.form = row;
      },
      submit(formName) {
        this.$refs[formName].validate((valid) => {
          if (valid) {
            this.loading = true;
            let saveType = "add";
            if (this.form.id) {
              saveType = "update"
            }
            this.$post("/project/" + saveType, this.form, () => {
              this.createVisible = false;
              this.loading = false;
              this.list();
              Message.success('保存成功');
            });
          } else {
            return false;
          }
        });
      },
      del(row) {
        this.$confirm('这个项目确定要删除吗?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          this.$get('/project/delete/' + row.id, () => {
            Message.success('删除成功');
            this.list();
          });
        }).catch(() => {

        });
      },
      list() {
        this.$post("/project/list/1/10", {}, (response) => {
          this.items = response.data;
        })
      }
    }
  }
</script>

<style scoped>
  .project-container {
    padding: 15px;
    width: 100%;
    height: 100%;
    box-sizing: border-box;
  }

  .main-content {
    margin: 0 auto;
    width: 100%;
    max-width: 1200px;
  }

  .table-page {
    padding-top: 20px;
    margin-right: -9px;
    float: right;
  }
</style>
