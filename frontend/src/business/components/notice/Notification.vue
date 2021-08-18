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
          <div @click="showNoticeCenter" v-if="runningTotal > 0">
            <el-badge is-dot class="item" type="danger">
              <font-awesome-icon class="icon global focusing" :icon="['fas', 'bell']"/>
            </el-badge>
          </div>
          <font-awesome-icon @click="showNoticeCenter" class="icon global focusing" :icon="['fas', 'bell']" v-else/>
        </el-tooltip>
      </el-menu-item>
    </el-menu>

    <el-drawer :visible.sync="taskVisible" :destroy-on-close="true" direction="rtl"
               :withHeader="true" :modal="false" :title="$t('commons.notice_center')" size="600px"
               custom-class="ms-drawer-task">
      <div style="margin: 0px 20px 0px">
        <el-tabs :active-name="activeName">
          <!--          <el-tab-pane label="@提到我的" name="mentionedMe">-->

          <!--          </el-tab-pane>-->
          <el-tab-pane label="系统通知" name="systemNotice">
            <div style="padding-left: 320px; padding-bottom: 5px; width: 100%">
              <span style="color: gray; padding-right: 10px">({{ totalCount }} 条消息)</span>
              <el-dropdown @command="handleCommand" style="padding-right: 10px">
                <span class="el-dropdown-link">
                  {{ goPage }}/{{ totalPage }}<i class="el-icon-arrow-down el-icon--right"></i>
                </span>
                <el-dropdown-menu slot="dropdown">
                  <el-dropdown-item v-for="i in totalPage" :key="i" :command="i">{{ i }}</el-dropdown-item>
                </el-dropdown-menu>
              </el-dropdown>
              <el-button icon="el-icon-arrow-left" size="mini" :disabled="goPage === 1" @click="prevPage"/>
              <el-button icon="el-icon-arrow-right" size="mini" :disabled="goPage === totalPage" @click="nextPage"/>
            </div>
            <div class="report-container">
              <div v-for="item in taskData" :key="item.id" style="margin-bottom: 5px">
                <el-card class="ms-card-task">
                  <el-row type="flex" justify="space-between">
                    <el-col :span="12">
                      {{ item.title }}
                    </el-col>
                    <el-col :span="6">
                      {{ item.createTime | timestampFormatDate }}
                    </el-col>
                  </el-row>
                  <span>
                 {{ item.content }}
                </span>
                  <br/>
                  <el-row>
                  </el-row>
                </el-card>
              </div>
            </div>
            <div style="color: black; padding-top: 50px; text-align: center">
              - 仅显示最近3个月的站内消息 -
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-drawer>

  </div>
</template>

<script>
import MsDrawer from "../common/components/MsDrawer";
import {getCurrentProjectID} from "@/common/js/utils";
import MsRequestResultTail from "../../components/api/definition/components/response/RequestResultTail";
import MsTipButton from "@/business/components/common/components/MsTipButton";

export default {
  name: "MsNotification",
  components: {
    MsTipButton,
    MsDrawer,
    MsRequestResultTail
  },
  inject: [
    'reload'
  ],
  data() {
    return {
      runningTotal: 0,
      taskVisible: false,
      result: {},
      taskData: [],
      response: {},
      initEnd: false,
      visible: false,
      showType: "",
      maintainerOptions: [],
      websocket: Object,
      activeName: 'systemNotice',
      pageSize: 20,
      goPage: 1,
      totalPage: 0,
      totalCount: 0,
    };
  },
  props: {
    color: String
  },
  created() {
    this.init();
  },
  watch: {
    taskVisible(v) {
      if (!v) {
        this.close();
      }
    }
  },
  methods: {
    initWebSocket() {
      let protocol = "ws://";
      if (window.location.protocol === 'https:') {
        protocol = "wss://";
      }
      const uri = protocol + window.location.host + "/notification/count/" + getCurrentProjectID();
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
      let taskTotal = e.data;
      this.runningTotal = taskTotal;
      this.initIndex++;
      if (this.taskVisible && taskTotal > 0 && this.initEnd) {
        setTimeout(() => {
          this.initEnd = false;
          this.init();
        }, 3000);
      }
    },
    onClose(e) {
    },
    showNoticeCenter() {
      this.getTaskRunning();
      this.init();
      this.readAll();
      this.taskVisible = true;
    },
    close() {
      this.visible = false;
      this.taskVisible = false;
      this.showType = "";
      if (this.websocket && this.websocket.close instanceof Function) {
        this.websocket.close();
      }
    },
    open() {
      this.showNoticeCenter();
      this.initIndex = 0;
    },
    init() {
      this.result.loading = true;
      this.result = this.$post('/notification/list/all/' + this.goPage + '/' + this.pageSize, {}, response => {
        this.taskData = response.data.listObject;
        this.totalPage = response.data.pageCount;
        this.totalCount = response.data.itemCount;
        this.calculationRunningTotal();
        this.initEnd = true;
      });
    },
    handleCommand(i) {
      this.goPage = i;
      this.init();
    },
    readAll() {
      this.$get('/notification/read/all');
    },
    getTaskRunning() {
      this.initWebSocket();
    },
    calculationRunningTotal() {
      if (this.taskData) {
        this.runningTotal = this.taskData.filter(t => t.status === 'UNREAD').length;
      }
    },
    prevPage() {
      if (this.goPage < 1) {
        this.goPage = 1;
      } else {
        this.goPage--;
      }
      this.init();
    },
    nextPage() {
      if (this.goPage > this.totalPage) {
        this.goPage = this.totalPage;
      } else {
        this.goPage++;
      }
      this.init();
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
