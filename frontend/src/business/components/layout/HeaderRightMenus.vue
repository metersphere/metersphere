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
import MsUser from "@/business/components/common/head/HeaderUser";
import MsHeaderWs from "@/business/components/common/head/HeaderWs";
import MsLanguageSwitch from "@/business/components/common/head/LanguageSwitch";
import MsTaskCenter from "@/business/components/task/TaskCenter";
import MsNotification from "@/business/components/notice/Notification";


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
