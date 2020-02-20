<template>
  <el-row type="flex" align="middle" class="current-user">
    <el-avatar shape="square" size="small" :src="squareUrl"/>
    <span class="username">{{currentUser.name}}</span>
    <el-button class="edit" type="primary" icon="el-icon-edit" size="mini"
               circle @click="edit"/>
    <el-dialog :title="currentUser.name" :visible.sync="editVisible" width="30%">
      <el-form :model="form" label-position="top" size="small">
        <el-form-item label="姓名">
          <el-input v-model="form.name" autocomplete="off"/>
        </el-form-item>
        <el-form-item label="手机号码">
          <el-input v-model="form.mobile" autocomplete="off"/>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submit" size="medium">更新</el-button>
      </span>
    </el-dialog>
  </el-row>
</template>

<script>
  import Cookies from 'js-cookie';
  import {TokenKey} from "../../../common/constants";

  export default {
    name: "MsCurrentUser",

    data() {
      return {
        editVisible: false,
        id: "123456",
        squareUrl: "https://cube.elemecdn.com/9/c2/f0ee8a3c7c9638a54940382568c9dpng.png",
        form: {}
      }
    },
    methods: {
      edit() {
        this.editVisible = true;
        this.form = Object.assign({}, this.currentUser);
      },
      submit() {
        this.editVisible = false;
      }
    },
    computed: {
      currentUser: () => {
        let user = Cookies.get(TokenKey);
        return JSON.parse(user);
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
