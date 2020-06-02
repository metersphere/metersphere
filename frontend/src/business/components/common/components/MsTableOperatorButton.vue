<template>
  <ms-tip-button
    :disabled="disabled"
    @click="exec"
    @clickStop="clickStop"
    :type="type"
    :tip="tip"
    :icon="icon" size="mini" circle/>
</template>

<script>
  import MsTableButton from "./MsTableButton";
  import MsTipButton from "./MsTipButton";
  import {hasRoles} from "../../../../common/js/utils";
  import {ROLE_TEST_MANAGER, ROLE_TEST_USER} from "../../../../common/js/constants";
  export default {
    name: "MsTableOperatorButton",
    components: {MsTipButton, MsTableButton},
    props: {
      icon: {
        type: String,
        default: 'el-icon-question'
      },
      type: {
        type: String,
        default: 'primary'
      },
      tip: {
        type: String
      },
      disabled: {
        type: Boolean,
        default: false
      },
      isTesterPermission: {
        type: Boolean,
        default: false
      }
    },
    mounted() {
      if (this.isTesterPermission && !hasRoles(ROLE_TEST_USER, ROLE_TEST_MANAGER)) {
        this.disabled = true;
      }
    },
    methods: {
      exec() {
        this.$emit('exec');
      },
      clickStop() {
        this.$emit('clickStop');
      }
    }
  }
</script>

<style scoped>
</style>
