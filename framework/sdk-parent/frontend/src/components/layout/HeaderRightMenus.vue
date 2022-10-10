<template>
  <div class="ms-align-right">
    <ms-user/>
    <ms-language-switch/>
    <ms-header-ws/>
    <ms-task-center/>
    <ms-notification/>
  </div>
</template>

<script>
import MsUser from "../../components/head/HeaderUser";
import MsHeaderWs from "../../components/head/HeaderWs";
import MsLanguageSwitch from "../../components/head/LanguageSwitch";
import MsTaskCenter from "../../components/task/TaskCenter";
import MsNotification from "../../components/notice/Notification";


export default {
  name: 'MsHeaderRightMenus',
  props: {},
  components: {
    MsNotification,
    MsTaskCenter,
    MsLanguageSwitch,
    MsUser,
    MsHeaderWs
  },
  data() {
    return {}
  }
};
</script>


<style scoped>
.ms-align-right {
  width: 100%;
}
</style>
