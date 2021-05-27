<template>
  <el-dialog :close-on-click-modal="false" title="添加成员" :visible.sync="dialogVisible" width="30%" :destroy-on-close="true"
             @close="handleClose">
    <el-form :model="form" ref="form" :rules="rules" label-position="right" label-width="100px" size="small">
      <el-form-item :label="$t('commons.member')" prop="memberSign" :rules="{required: true, message: $t('member.input_id_or_email'), trigger: 'change'}">
        <el-autocomplete
          class="input-with-autocomplete"
          v-model="form.memberSign"
          :placeholder="$t('member.input_id_or_email')"
          :trigger-on-focus="false"
          :fetch-suggestions="querySearch"
          size="small"
          highlight-first-item
          value-key="email"
        >
          <template v-slot:default="scope">
            <span class="workspace-member-name">{{scope.item.id}}</span>
            <span class="workspace-member-email">{{scope.item.email}}</span>
          </template>
        </el-autocomplete>
      </el-form-item>
      <el-form-item label="用户组" prop="groupIds">
        <el-select v-model="form.groupIds" multiple placeholder="请选择用户组" class="select-width">
          <el-option
            v-for="item in form.groups"
            :key="item.id"
            :label="item.name"
            :value="item.id">
          </el-option>
        </el-select>
      </el-form-item>
    </el-form>
    <template v-slot:footer>
      <div class="dialog-footer">
        <el-button @click="dialogVisible = false" size="medium">{{ $t('commons.cancel') }}</el-button>
        <el-button type="primary" @click="submitForm('form')" @keydown.enter.native.prevent size="medium">
          {{ $t('commons.confirm') }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script>
import {GROUP_PROJECT} from "@/common/js/constants";
import {getCurrentProjectID} from "@/common/js/utils";

export default {
  name: "EditMember",
  data() {
    return {
      dialogVisible: false,
      form: {},
      rules: {
        userIds: [
          {required: true, message: this.$t('member.please_choose_member'), trigger: ['blur']}
        ],
        groupIds: [
          {required: true, message: '请选择用户组', trigger: ['blur']}
        ]
      },
      userList: [],
    }
  },
  computed: {
    projectId() {
      return getCurrentProjectID();
    }
  },
  methods: {
    submitForm() {
      this.$refs['form'].validate((valid) => {
        if (valid) {
          let userIds = [];
          let userId = this.form.userId;
          let email  = this.form.memberSign;
          let member = this.userList.find(user => user.id === email || user.email === email);
          if (!member) {
            this.$warning(this.$t('member.no_such_user'));
            return false;
          } else {
            userId = member.id;
          }
          userIds.push(userId);
          let param = {
            userIds: userIds,
            groupIds: this.form.groupIds,
            projectId: this.projectId
          };
          this.result = this.$post("user/project/member/add", param, () => {
            this.$success(this.$t('commons.save_success'));
            this.$emit("refresh");
            this.dialogVisible = false;
          })
        }
      });
    },
    open() {
      this.$get('/user/list/', response => {
        this.dialogVisible = true;
        this.userList = response.data;
      })
      this.result = this.$post('/user/group/list', {type: GROUP_PROJECT, resourceId: this.projectId}, response => {
        this.$set(this.form, "groups", response.data);
      })
    },
    handleClose() {
      this.dialogVisible = false;
      this.form = {};
    },
    querySearch(queryString, cb) {
      var userList = this.userList;
      var results = queryString ? userList.filter(this.createFilter(queryString)) : userList;
      // 调用 callback 返回建议列表的数据
      cb(results);
    },
    createFilter(queryString) {
      return (user) => {
        return (user.email.indexOf(queryString.toLowerCase()) === 0 || user.id.indexOf(queryString.toLowerCase()) === 0);
      };
    },
  }
}
</script>

<style scoped>
.workspace-member-name {
  float: left;
}

.workspace-member-email {
  float: right;
  color: #8492a6;
  font-size: 13px;
}

.input-with-autocomplete {
  width: 100%;
}

.select-width {
  width: 100%;
}
</style>
