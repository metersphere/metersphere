<template>
  <div>
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
      <div v-for="item in systemNoticeData" :key="item.id" style="margin-bottom: 5px">
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
  </div>
</template>

<script>
export default {
  name: "MentionedMeData",
  data() {
    return {
      systemNoticeData: [],
      pageSize: 20,
      goPage: 1,
      totalPage: 0,
      totalCount: 0,
    };
  },
  created() {
    this.init();
  },
  methods: {
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
        this.initEnd = true;
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
    }
  }
};
</script>

<style scoped>
.report-container {
  height: calc(100vh - 180px);
  min-height: 600px;
  overflow-y: auto;
}
</style>
