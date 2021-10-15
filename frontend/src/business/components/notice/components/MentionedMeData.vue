<template>
  <div>
    <div style="padding-left: 320px; padding-bottom: 5px; width: 100%">
      <span style="color: gray; padding-right: 10px">({{ totalCount }} {{ $t('commons.notice_count') }})</span>
      <el-dropdown @command="handleCommand" style="padding-right: 10px">
        <span class="el-dropdown-link" v-if="totalPage > 0">
          {{ goPage }}/{{ totalPage }}<i class="el-icon-arrow-down el-icon--right"></i>
        </span>
        <span v-else class="el-dropdown-link">0/0</span>
        <el-dropdown-menu slot="dropdown">
          <el-dropdown-item v-for="i in totalPage" :key="i" :command="i">{{ i }}</el-dropdown-item>
        </el-dropdown-menu>
      </el-dropdown>
      <el-button icon="el-icon-arrow-left" size="mini" :disabled="goPage === 1" @click="prevPage"/>
      <el-button icon="el-icon-arrow-right" size="mini" :disabled="goPage === totalPage" @click="nextPage"/>
    </div>
    <div class="report-container">
      <div v-for="item in systemNoticeData" :key="item.id" style="margin-bottom: 5px">
        <el-card class="ms-card-task">
          <el-row type="flex" justify="space-between">
            <el-col :span="12">

            </el-col>
            <el-col :span="6">
              {{ item.createTime | timestampFormatDate }}
            </el-col>
          </el-row>
          <el-row type="flex" align="start" class="current-user">
            <div class="icon-title">
              {{ item.user.name.substring(0, 1) }}
            </div>
            <span class="username">{{ item.user.name }}</span>
            <span class="operation">
             {{ getOperation(item.operation) }}{{ getResource(item) }}:
              <span v-if="item.resourceId && item.operation.indexOf('DELETE') < 0"
                    @click="clickResource(item)" style="color: #783887; cursor: pointer;">{{ item.resourceName }}</span>
              <span v-else>{{ item.resourceName }}</span>
            </span>
          </el-row>
        </el-card>
      </div>
    </div>
    <div style="color: gray; padding-top:20px; text-align: center">
      - {{ $t('commons.notice_tips') }} -
    </div>
  </div>
</template>

<script>
import {getOperation, getResource, getUrl} from "@/business/components/notice/util";

export default {
  name: "MentionedMeData",
  data() {
    return {
      systemNoticeData: [],
      pageSize: 20,
      goPage: 1,
      totalPage: 0,
      totalCount: 0,
      userMap: {}
    };
  },
  props: {
    userList: Array
  },
  created() {
    this.init();
    this.userMap = this.userList.reduce((r, c) => {
      r[c.id] = c;
      return r;
    }, {});
  },
  methods: {
    getOperation,
    getResource,
    handleCommand(i) {
      this.goPage = i;
      this.init();
    },
    init() {
      let param = {type: 'MENTIONED_ME'};
      this.result = this.$post('/notification/list/all/' + this.goPage + '/' + this.pageSize, param, response => {
        this.systemNoticeData = response.data.listObject;
        this.totalPage = response.data.pageCount;
        this.totalCount = response.data.itemCount;

        this.systemNoticeData.forEach(n => {
          n.user = this.userMap[n.operator] || {name: "MS"};
        });
      });
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
    },
    clickResource(resource) {
      let resourceId = resource.resourceId;
      if (!resourceId) {
        return;
      }
      let uri = getUrl(resource);

      this.$get('/user/update/currentByResourceId/' + resourceId, () => {
        this.toPage(uri);
      });
    },
    toPage(uri) {
      let id = "new_a";
      let a = document.createElement("a");
      a.setAttribute("href", uri);
      a.setAttribute("target", "_blank");
      a.setAttribute("id", id);
      document.body.appendChild(a);
      a.click();

      let element = document.getElementById(id);
      element.parentNode.removeChild(element);
    }
  }
};
</script>

<style scoped>
.report-container {
  height: calc(100vh - 250px);
  overflow-y: auto;
}

.ms-card-task {
  padding-bottom: 10px;
}

.current-user .username {
  display: inline-block;
  font-size: 14px;
  font-weight: 500;
  margin: 5px;
  overflow-x: hidden;
  padding-bottom: 0;
  text-overflow: ellipsis;
  vertical-align: middle;
  white-space: nowrap;
  max-width: 180px;
}

.current-user .operation {
  display: inline-block;
  font-size: 14px;
  margin: 5px;
  overflow-x: hidden;
  padding-bottom: 0;
  text-overflow: ellipsis;
  vertical-align: middle;
  white-space: nowrap;
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
  background-color: #783887;
  height: 30px;
  line-height: 30px;
  text-align: center;
  border-radius: 30px;
  font-size: 14px;
}

</style>
