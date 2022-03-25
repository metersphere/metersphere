<template>
  <div>
    <div style="padding-bottom: 5px; width: 100%; height: 50px;">
      <div style="float:right;">
        <span style="color: gray; padding-right: 10px">({{ totalCount }} {{ $t('commons.notice_count') }})</span>
        <el-dropdown @command="handleCommand" style="padding-right: 10px">
        <span class="el-dropdown-link" v-if="totalPage > 0">
          {{ goPage }}/{{ totalPage }}<i class="el-icon-arrow-down el-icon--right"></i>
        </span>
          <span v-else class="el-dropdown-link">0/0</span>
          <el-dropdown-menu slot="dropdown">
            <div class="dropdown-content">
              <el-dropdown-item v-for="i in totalPage" :key="i" :command="i">{{ i }}</el-dropdown-item>
            </div>
          </el-dropdown-menu>
        </el-dropdown>
        <el-button icon="el-icon-arrow-left" size="mini" :disabled="goPage === 1" @click="prevPage"/>
        <el-button icon="el-icon-arrow-right" size="mini" :disabled="goPage === totalPage" @click="nextPage"/>
      </div>
    </div>
    <div class="report-container">
      <el-table :data="systemNoticeData"
                :show-header="false"
                :highlight-current-row="true"
                style="width: 100%">
        <el-table-column prop="content" :label="$t('commons.name')">
          <template v-slot="{row}">
            <el-row type="flex" align="start" class="current-user">
              <el-col :span="2">
                <div class="icon-title">
                  {{ row.user.name.substring(0, 1) }}
                </div>
              </el-col>
              <el-col :span="22">
                <span class="username">{{ row.user.name }}</span>
                <span class="operation">{{ getOperation(row.operation) }}{{ getResource(row) }}:
                  <span v-if="row.resourceId && row.operation.indexOf('DELETE') < 0" @click="clickResource(row)"
                        style="color: #783887; cursor: pointer;">
                    {{ row.resourceName }}
                  </span>
                  <span v-else>{{ row.resourceName }}</span>
                </span>
              </el-col>
            </el-row>
            <el-row type="flex" justify="space-between">
              <el-col :span="12">

              </el-col>
              <el-col :span="6">
                <span class="time-style">{{ row.createTime | timestampFormatDate }}</span>
              </el-col>
            </el-row>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <div style="color: gray; padding-top:20px; text-align: center">
      - {{ $t('commons.notice_tips') }} -
    </div>
  </div>
</template>

<script>
import {getOperation, getResource, getUrl} from "@/business/components/notice/util";

export default {
  name: "NotificationData",
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
    userList: Array,
    type: String,
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
      let param = {type: this.type};
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
  max-width: 100px;
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
  max-width: 320px;
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

.dropdown-content {
  max-height: 240px;
  overflow: auto;
  /*margin-top: 5px;*/
}

/* 设置滚动条的样式 */
.dropdown-content::-webkit-scrollbar {
  width: 8px;
}

/* 滚动槽 */
.dropdown-content::-webkit-scrollbar-track {
  border-radius: 10px;
}

/* 滚动条滑块 */
.dropdown-content::-webkit-scrollbar-thumb {
  border-radius: 10px;
  background: rgba(0, 0, 0, 0.2);
}

.dropdown-content::-webkit-scrollbar-thumb:window-inactive {
  background: rgba(255, 0, 0, 0.4);
}

.time-style {
  font-size: 10px;
}
</style>
