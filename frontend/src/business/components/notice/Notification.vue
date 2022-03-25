<template>
  <div>
    <el-menu :unique-opened="true" class="header-user-menu align-right header-top-menu"
             mode="horizontal"
             :background-color="color"
             text-color="#fff"
             active-text-color="#fff">
      <el-menu-item onselectstart="return false">
        <el-tooltip effect="light">
          <template v-slot:content>
            <span>{{ $t('commons.notice_center') }}</span>
          </template>
          <div @click="showNoticeCenter" v-if="noticeCount > 0 || noticeShow">
            <el-badge is-dot class="item" type="danger">
              <font-awesome-icon class="icon global focusing" :icon="['fas', 'bell']"/>
            </el-badge>
          </div>
          <font-awesome-icon @click="showNoticeCenter" class="icon global focusing" :icon="['fas', 'bell']" v-else/>
        </el-tooltip>
      </el-menu-item>
    </el-menu>

    <el-drawer :visible.sync="taskVisible" :destroy-on-close="true" direction="rtl"
               :withHeader="true" :modal="false" :title="$t('commons.notice_center')" size="550px"
               custom-class="ms-drawer-task">
      <div style="margin: 0 20px 0">
        <el-tabs :active-name="activeName">
          <el-tab-pane :label="$t('commons.mentioned_me_notice')" name="mentionedMe">
            <notification-data ref="mentionedMe" :user-list="userList" type="MENTIONED_ME"/>
          </el-tab-pane>
          <el-tab-pane :label="$t('commons.system_notice')" name="systemNotice">
            <notification-data ref="systemNotice" :user-list="userList" type="SYSTEM_NOTICE"/>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-drawer>

  </div>
</template>

<script>
import MsDrawer from "../common/components/MsDrawer";
import {getCurrentUserId} from "@/common/js/utils";
import MsRequestResultTail from "../../components/api/definition/components/response/RequestResultTail";
import MsTipButton from "@/business/components/common/components/MsTipButton";
import {getOperation, getResource} from "@/business/components/notice/util";
import NotificationData from "@/business/components/notice/components/NotificationData";

export default {
  name: "MsNotification",
  components: {
    NotificationData,
    MsTipButton,
    MsDrawer,
    MsRequestResultTail
  },
  inject: [
    'reload'
  ],
  data() {
    return {
      noticeCount: 0,
      serverTime: new Date().getTime(),
      taskVisible: false,
      result: {},
      systemNoticeData: [],
      response: {},
      initEnd: false,
      visible: false,
      showType: "",
      userList: [],
      userMap: {},
      websocket: Object,
      activeName: 'mentionedMe',
      pageSize: 20,
      goPage: 1,
      totalPage: 0,
      totalCount: 0,
      noticeShow: false,
    };
  },
  props: {
    color: String
  },
  created() {
    this.getNotifications();
    this.getUserList();
  },
  watch: {
    taskVisible(v) {
      if (!v) {
        this.close();
      }
    }
  },
  methods: {
    getUserList() {
      this.$get('/user/list', response => {
        this.userList = response.data;
        this.userMap = this.userList.reduce((r, c) => {
          r[c.id] = c;
          return r;
        }, {});
      });
    },
    initWebSocket() {
      let protocol = "ws://";
      if (window.location.protocol === 'https:') {
        protocol = "wss://";
      }
      const uri = protocol + window.location.host + "/notification/count/" + getCurrentUserId();
      this.websocket = new WebSocket(uri);
      this.websocket.onmessage = this.onMessage;
      this.websocket.onopen = this.onOpen;
      this.websocket.onerror = this.onError;
      this.websocket.onclose = this.onClose;
    },
    onOpen() {
    },
    onError(e) {
    },
    onMessage(e) {
      let m = JSON.parse(e.data);
      this.noticeCount = m.count;
      this.serverTime = m.now;
      this.initIndex++;
      if (this.noticeCount > 0) {
        this.showNotification();
      }
      if (this.taskVisible && this.noticeCount > 0 && this.initEnd) {
        this.$refs.systemNotice.init();
        this.$refs.mentionedMe.init();
      }
    },
    onClose(e) {
    },
    showNoticeCenter() {
      this.noticeCount = 0;
      this.readAll();
      this.taskVisible = true;
    },
    close() {
      this.visible = false;
      this.taskVisible = false;
      this.showType = "";
    },
    open() {
      this.showNoticeCenter();
      this.initIndex = 0;
    },
    readAll() {
      this.$get('/notification/read/all');
      this.noticeShow = false;
    },
    getNotifications() {
      this.initWebSocket();
    },
    showNotification() {
      this.result = this.$post('/notification/list/all/' + 1 + '/' + 10, {}, response => {
        let data = response.data.listObject;
        let now = this.serverTime;
        data.filter(d => d.status === 'UNREAD').forEach(d => {
          if (now - d.createTime > 10 * 1000) {
            return;
          }
          d.user = this.userMap[d.operator] || {name: 'MS'};
          let message = d.user.name + getOperation(d.operation) + getResource(d) + ": " + d.resourceName;
          let title = d.type === 'MENTIONED_ME' ? this.$t('commons.mentioned_me_notice') : this.$t('commons.system_notice');
          setTimeout(() => {
            this.$notify({
              title: title,
              type: 'info',
              message: message,
            });
            // 弹出之后标记成已读
            this.$get('/notification/read/' + d.id);
            this.noticeShow = true;
          });
        });
      });
    }
  }
};
</script>

<style>
.ms-drawer-task {
  top: 42px !important;
}
</style>

<style scoped>
.el-icon-check {
  color: #44b349;
  margin-left: 10px;
}

.report-container {
  height: calc(100vh - 180px);
  min-height: 600px;
  overflow-y: auto;
}

.align-right {
  float: right;
}

.icon {
  width: 24px;
}

/deep/ .el-drawer__header {
  font-size: 18px;
  color: #0a0a0a;
  border-bottom: 1px solid #E6E6E6;
  background-color: #FFF;
  margin-bottom: 10px;
  padding: 10px;
}

.ms-card-task >>> .el-card__body {
  padding: 10px;
}

.global {
  color: #fff;
}

.header-top-menu {
  height: 40px;
  line-height: 40px;
  color: inherit;
}

.header-top-menu.el-menu--horizontal > li {
  height: 40px;
  line-height: 40px;
  color: inherit;
}

.header-top-menu.el-menu--horizontal > li.el-submenu > * {
  height: 39px;
  line-height: 40px;
  color: inherit;
}

.header-top-menu.el-menu--horizontal > li.is-active {
  background: var(--color_shallow) !important;
}

.ms-card-task:hover {
  cursor: pointer;
  border-color: #783887;
}


/deep/ .el-menu-item {
  padding-left: 0;
  padding-right: 0;
}

/deep/ .el-badge__content.is-fixed {
  top: 25px;
}

/deep/ .el-badge__content {
  border-radius: 10px;
  /*height: 10px;*/
  /*line-height: 10px;*/
}

.item {
  margin-right: 10px;
}

.ms-task-error {
  color: #F56C6C;
}

.ms-task-success {
  color: #67C23A;
}
</style>
