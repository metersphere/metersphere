<template>
  <el-row type="flex" align="middle" class="current-user">
    <el-avatar shape="square" size="small" :src="squareUrl"/>
    <span class="username">kun@fit2cloud.com</span>
    <el-button class="edit" type="primary" icon="el-icon-edit" size="mini"
               circle @click="editVisible = true"/>
    <el-dialog :title="title" :visible.sync="editVisible" width="30%">
      <el-form :model="form" label-position="top" size="small">
        <el-form-item label="姓名">
          <el-input v-model="form.name" autocomplete="off"/>
        </el-form-item>
        <el-form-item label="手机号码">
          <el-input v-model="form.mobile" autocomplete="off"/>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
                <el-button type="primary" @click="editVisible = false" size="medium">更新</el-button>
            </span>
    </el-dialog>
  </el-row>
</template>

<script>
  export default {
    name: "MsCurrentUser",
    data() {
      return {
        editVisible: false,
        id: "123456",
        squareUrl: "https://cube.elemecdn.com/9/c2/f0ee8a3c7c9638a54940382568c9dpng.png",
        form: {
          name: "kun@fit2cloud.com",
          mobile: ""
        }
      }
    }, computed: {
      title: function () {
        return "编辑账号(id: " + this.id + ")";
      }
    }
  }
</script>

<style scoped>
  .current-user .username {
    display: inline-block;
    font-size: 16px;
    font-weight: 500;
    margin: 0 5px;
    overflow-x: hidden;
    padding-bottom: 0;
    text-overflow: ellipsis;
    vertical-align: middle;
    white-space: nowrap;
    width: 180px;
  }

  .current-user .edit {
    opacity: 0;
  }

  .current-user:hover .edit {
    opacity: 1;
  }
</style>
