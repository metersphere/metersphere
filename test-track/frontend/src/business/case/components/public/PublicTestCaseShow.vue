<template>
  <el-drawer
    :visible.sync="visible"
    append-to-body
    size="90%"
    :with-header="false"
    :close-on-click-modal="false"
    class="case-desc-drawer-layout"
  >
    <div style="height: 100%; overflow: auto">
      <test-case-edit :is-public-show="true" :public-case-id.sync="publicCaseId" @close="close" @previousCase="previous" @nextCase="next"
                      :is-first-public.sync="isFirstPublic" :is-last-public.sync="isLastPublic"/>
    </div>
  </el-drawer>
</template>

<script>
import TestCaseEdit from "@/business/case/components/TestCaseEdit"
import {testCasePublicList} from "@/api/testCase";

export default {
  name: "TestCasePreview",
  components: {
    TestCaseEdit
  },
  props: {
    condition: {
      type: Object
    },
    pageSize: {
      type: Number
    },
    pageTotal: {
      type: Number
    }
  },
  data() {
    return {
      visible: false,
      publicCaseId: "",
      tableData: [],
      currentPage: 0
    }
  },
  computed: {
    isFirstPublic() {
      let currentIndex = this.tableData.findIndex(item => item.id === this.publicCaseId);
      if (this.currentPage === 1 && currentIndex === 0) {
        return true;
      }
      return false;
    },
    isLastPublic() {
      let currentIndex = this.tableData.findIndex(item => item.id === this.publicCaseId);
      if (this.currentPage === (parseInt(this.pageTotal / this.pageSize) + 1) && currentIndex === this.tableData.length - 1) {
        return true;
      }
      return false;
    }
  },
  methods: {
    open(caseId) {
      this.visible = true;
      this.publicCaseId = caseId;
    },
    close() {
      this.visible = false;
    },
    async previous(currentCaseId) {
      let currentIndex = this.tableData.findIndex(item => item.id === currentCaseId);
      if (currentIndex === 0) {
        if (this.currentPage === 1) {
          return;
        }
        this.currentPage--;
        await testCasePublicList({pageNum: this.currentPage, pageSize: this.pageSize}, this.condition)
          .then(response => {
            this.tableData = response.data.listObject;
          });
        this.publicCaseId = this.tableData[this.tableData.length - 1].id
      } else {
        currentIndex--;
        this.publicCaseId = this.tableData[currentIndex].id;
      }
    },
    async next(currentCaseId) {
      let currentIndex = this.tableData.findIndex(item => item.id === currentCaseId);
      if (currentIndex === this.tableData.length - 1) {
        if (this.currentPage === (parseInt(this.pageTotal / this.pageSize) + 1)) {
          return;
        }
        this.currentPage++;
        await testCasePublicList({pageNum: this.currentPage, pageSize: this.pageSize}, this.condition)
          .then(response => {
            this.tableData = response.data.listObject;
          });
        this.publicCaseId = this.tableData[0].id
      } else {
        currentIndex++;
        this.publicCaseId = this.tableData[currentIndex].id;
      }
    }
  }
}
</script>

<style scoped>
:deep(.el-drawer__header) {
  font-family: 'PingFang SC';
  font-style: normal;
  font-weight: bold;
  font-size: 16px;
  line-height: 24px;
  color: #1F2329;
  flex: none;
  flex-grow: 0;
  padding: 16px 24px;
  border-bottom: 1px solid rgba(31, 35, 41, 0.15);
  margin: 0;
}


:deep(i.el-dialog__close.el-icon.el-icon-close) {
  float: right;
}
</style>
