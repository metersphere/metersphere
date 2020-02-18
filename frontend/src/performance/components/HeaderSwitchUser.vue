<template>
  <el-row>
    <el-button type="text" @click="open">切换角色-{{ currentUserRole }}</el-button>
    <el-dialog title="角色列表" :visible.sync="createVisible" width="30%">
      <el-tree :data="userRoleList"
               @node-click="handleNodeClick"
               :props="defaultProps"
               ></el-tree>
      <div style="text-align: center; margin-top: 20px;">
        <el-button type="info" size="mini" class="ms-button" @click="closeDialog">取消</el-button>
        <el-button type="primary" size="mini" style="margin-left: 50px;" @click="changeSubmit">保存</el-button>
      </div>
    </el-dialog>
  </el-row>
</template>

<script>
  import Cookies from "js-cookie"
  import {TokenKey} from "../../common/constants"

  export default {
    name: "MsSwitchUser",
    computed: {
      currentUserRole() {
        return this.userInfo.lastSourceId;
      }
    },
    created() {
      this.getUserRoleList();
      this.getUserInfo();
    },
    data() {
      return {
        createVisible: false,
        defaultProps: {
          children: 'children',
          label: 'switchInfo'
        },
        switchInfo: '',
        userRoleList: [],
        selectNode:[],
        userInfo: {},
        userId: JSON.parse(Cookies.get(TokenKey)).id
      }
    },
    methods: {
      open() {
        this.createVisible = true;
      },
      getUserRoleList() {
        this.$get('user/rolelist/' + this.userId).then(response => {
          let roleList = response.data.data;
          let newRoleList = [];
          roleList.forEach(item => {
            // item.current = item.id === this.userInfo.lastSourceId;
            item.current = item.roleId;
            if (item.current) {
              if (item.name) {
                item.switchInfo = item.name + " [" + item.desc + "]";
              } else {
                item.switchInfo = "MeterSphere[系统管理员]";
              }
            }
            if (!item.parentId) {
              item.hasChild = false;
              item.children = [];
              newRoleList.push(item);
            } else {
              newRoleList.forEach(userRole => {
                if (userRole.id === item.parentId) {
                  userRole.children.push(item);
                  userRole.hasChild = true;
                }
              })
            }
          })
          this.userRoleList = newRoleList;
        })
      },
      closeDialog() {
        this.createVisible = false;
      },
      switchRole(selectNode) {
        if (!selectNode.switchable) {
          return;
        }
        this.$post("user/switch/source/" + selectNode.roleId).then(() => {
          this.getUserInfo();
          // localStorage.setItem("lastSourceId", "bbbbb");
          this.closeDialog();
        });
        localStorage.setItem("lastSourceId", selectNode.roleId);
        window.location.reload();
      },
      changeSubmit() {
        this.switchRole(this.selectNode);
      },
      handleNodeClick(data) {
        this.selectNode = data;
        window.console.log(data)
      },
      getUserInfo() {
        this.$get("/user/info/" + this.userId).then(response => {
          this.userInfo = response.data.data;
        })
      }

    }

  }
</script>

<style scoped>
</style>
