<template>
  <div>
    <el-row type="flex" align="middle" class="current-user">
      <div class="icon-title" @click="resVisible = true">
        {{ currentUser.name.substring(0, 1) }}
      </div>
      <span class="username"  @click="resVisible = true" style="cursor: pointer">{{ currentUser.name }}</span>
    </el-row>
    <el-dialog :close-on-click-modal="false" width="80%"
               :visible.sync="resVisible" class="api-import" destroy-on-close @close="closeDialog">
      <ms-person-router @closeDialog = "closeDialog"/>
    </el-dialog>
  </div>
</template>

<script>
import {getCurrentUser} from "@/common/js/utils";
import MsPersonRouter from "@/business/components/settings/components/PersonRouter";

export default {
  name: "MsCurrentUser",
  components:{MsPersonRouter},
  data() {
    return {
      editVisible: false,
      resVisible:false,
      id: "123456",
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
    },
    closeDialog(){
      this.resVisible = false;
    },
  },
  computed: {
    currentUser: () => {
      return getCurrentUser();
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

.icon-title {
  color: #fff;
  width: 30px;
  background-color: #72dc91;
  height: 30px;
  line-height: 30px;
  text-align: center;
  border-radius: 30px;
  font-size: 14px;
  cursor: pointer;
}

</style>
