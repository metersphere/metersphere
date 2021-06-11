<template>
  <el-dialog :close-on-click-modal="false" :title="$t('member.create')" :visible.sync="dialogVisible" width="40%" :destroy-on-close="true"
             @close="handleClose">
    <el-form :model="form" ref="form" :rules="rules" label-position="right" label-width="100px" size="small">
      <el-form-item :label="$t('commons.member')" prop="userIds" :rules="{required: true, message: $t('member.please_choose_member'), trigger: 'blur'}">
        <el-select
          v-model="form.userIds"
          multiple
          filterable
          :popper-append-to-body="false"
          class="select-width"
          :placeholder="$t('member.please_choose_member')">
          <el-option
            v-for="item in userList"
            :key="item.id"
            :label="item.id"
            :value="item.id">
            <template>
              <span class="workspace-member-name">{{item.name}} ({{item.id}})</span>
              <span class="workspace-member-email">{{item.email}}</span>
            </template>
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item :label="$t('commons.group')" prop="groupIds">
        <el-select v-model="form.groupIds" multiple :placeholder="$t('group.please_select_group')" class="select-width">
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
import {getCurrentOrganizationId, getCurrentProjectID} from "@/common/js/utils";

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
          {required: true, message: this.$t('group.please_select_group'), trigger: ['blur']}
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
          let param = {
            userIds: this.form.userIds,
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
      this.result = this.$post('/user/group/list', {type: GROUP_PROJECT, resourceId: getCurrentOrganizationId()}, response => {
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
