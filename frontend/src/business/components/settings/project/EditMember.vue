<template>
  <add-member :group-type="GROUP_PROJECT" :group-scope-id="organizationId" ref="addMember" @submit="submit"/>
</template>

<script>
import {GROUP_PROJECT} from "@/common/js/constants";
import {getCurrentOrganizationId, getCurrentProjectID} from "@/common/js/utils";
import AddMember from "@/business/components/settings/common/AddMember";

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
    }
  },
  computed: {
    projectId() {
      return getCurrentProjectID();
    },
    organizationId() {
      return getCurrentOrganizationId();
    },
    GROUP_PROJECT() {
      return GROUP_PROJECT;
    }
  },
  methods: {
    submit(param) {
      param['projectId'] = this.projectId
      this.result = this.$post("user/project/member/add", param, () => {
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
