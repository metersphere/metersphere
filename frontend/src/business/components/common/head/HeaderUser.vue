<template>
  <el-dropdown size="medium" @command="handleCommand" class="align-right">
    <span class="dropdown-link">
        {{currentUser.name}}<i class="el-icon-caret-bottom el-icon--right"/>
    </span>
    <template v-slot:dropdown>
      <el-dropdown-menu>
        <el-dropdown-item command="personal">{{$t('commons.personal_information')}}</el-dropdown-item>
        <el-dropdown-item command="logout">{{$t('commons.exit_system')}}</el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<script>
  import {TokenKey} from '../../../../common/js/constants';
  import {getCurrentUser} from "../../../../common/js/utils";

  export default {
    name: "MsUser",
    computed: {
      currentUser: () => {
        return getCurrentUser();
      }
    },
    methods: {
      handleCommand(command) {
        switch (command) {
          case "personal":
            // TODO 优化路由跳转，避免重复添加路由
            this.$router.push('/setting/personsetting').catch(error => error);
            break;
          case "logout":
            this.$get("/signout", function () {
              localStorage.removeItem(TokenKey);
              window.location.href = "/login";
            });
            break;
          default:
            break;
        }
      }
    }
  }
</script>

<style scoped>
  .dropdown-link {
    cursor: pointer;
    font-size: 12px;
    color: rgb(245, 245, 245);
    line-height: 40px;
  }

  .align-right {
    float: right;
  }

</style>


