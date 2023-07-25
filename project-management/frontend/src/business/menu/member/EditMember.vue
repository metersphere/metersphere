<template>
  <add-member :group-type="GROUP_PROJECT"
              :group-scope-id="workspaceId"
              ref="addMember"
              v-loading="loading"
              @submit="submit"
              :user-resource-url="userResourceUrl"/>
</template>

<script>
import {GROUP_PROJECT} from "metersphere-frontend/src/utils/constants";
import {getCurrentProjectID, getCurrentWorkspaceId} from "metersphere-frontend/src/utils/token";
import AddMember from "./AddMember";
import {addProjectUser} from "@/api/project";

export default {
  name: "EditMember",
  components: {AddMember},
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
      loading: false,
      userResourceUrl: 'user/add/project/member/option/' + getCurrentProjectID()
    };
  },
  computed: {
    projectId() {
      return getCurrentProjectID();
    },
    workspaceId() {
      return getCurrentWorkspaceId();
    },
    GROUP_PROJECT() {
      return GROUP_PROJECT;
    }
  },
  methods: {
    submit(param) {
      param['projectId'] = this.projectId;
      this.loading = addProjectUser(param).then(() => {
        this.$success(this.$t('commons.save_success'));
        this.$emit("refresh");
        this.$refs.addMember.close();
      })
    },
    open() {
      this.$refs.addMember.open();
    },
    handleClose() {
      this.dialogVisible = false;
      this.form = {};
    }
  }
};
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
