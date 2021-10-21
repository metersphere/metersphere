<template>
  <el-dialog :close-on-click-modal="false" :title="$t('project.code_segment.code_segment')" :visible.sync="visible" :destroy-on-close="true"
             @close="close" width="60%" top="10vh" v-loading="result.loading" append-to-body class="customFunc">
    <div>
      <el-alert
        :title="$t('project.code_segment.relate_tip')"
        type="info"
        style="width: 350px;float: left;"
        :closable="false" show-icon>
      </el-alert>
      <ms-table-search-bar :condition.sync="condition" @change="init" class="search-bar" :tip="$t('project.code_segment.search')"/>
      <el-table border class="adjust-table" :data="data" style="width: 100%" ref="table"
                highlight-current-row @current-change="handleCurrentChange">
        <el-table-column prop="name" :label="$t('commons.name')" show-overflow-tooltip/>
        <el-table-column prop="description" :label="$t('commons.description')" show-overflow-tooltip>
          <template v-slot:default="scope">
            <pre>{{ scope.row.description }}</pre>
          </template>
        </el-table-column>
        <el-table-column prop="tags" :label="$t('api_test.automation.tag')">
          <template v-slot:default="scope">
            <ms-tag v-for="(itemName,index)  in scope.row.tags" :key="index" type="success" effect="plain"
                    :content="itemName" style="margin-left: 0; margin-right: 2px">
            </ms-tag>
            <span></span>
          </template>
        </el-table-column>
        <el-table-column prop="type" :label="$t('project.code_segment.language')" show-overflow-tooltip/>
        <el-table-column prop="createTime"
                         :label="$t('commons.create_time')"
                         show-overflow-tooltip>
          <template v-slot:default="scope">
            <span>{{ scope.row.createTime | timestampFormatDate }}</span>
          </template>
        </el-table-column>
      </el-table>
      <ms-table-pagination :change="init" :current-page.sync="currentPage" :page-size.sync="pageSize" :total="total"/>
    </div>

    <template v-slot:footer>
      <el-button @click="close" size="medium">{{ $t('commons.cancel') }}</el-button>
      <el-button type="primary" @click="submit" size="medium" style="margin-left: 10px;">
        {{ $t('commons.confirm') }}
      </el-button>
    </template>
  </el-dialog>
</template>

<script>
import MsTablePagination from "@/business/components/common/pagination/TablePagination";
import MsTag from "@/business/components/common/components/MsTag";
import MsTableOperator from "@/business/components/common/components/MsTableOperator";
import MsTableOperatorButton from "@/business/components/common/components/MsTableOperatorButton";
import {getCurrentProjectID} from "@/common/js/utils";
import MsTableSearchBar from "@/business/components/common/components/MsTableSearchBar";

export default {
  name: "CustomFunctionRelate",
  components: {
    MsTablePagination,
    MsTag,
    MsTableOperator,
    MsTableOperatorButton,
    MsTableSearchBar
  },
  data() {
    return {
      visible: false,
      result: {},
      condition: {},
      data: [],
      currentPage: 1,
      pageSize: 10,
      total: 0,
      screenHeight: 'calc(100vh - 195px)',
      currentRow: {}
    }
  },
  methods: {
    init(language) {
      if (language) {
        this.condition.type = language;
      }
      this.condition.projectId = getCurrentProjectID();
      this.result = this.$post("/custom/func/list/" + this.currentPage + "/" + this.pageSize, this.condition, res => {
        let tableData = res.data;
        const {itemCount, listObject} = tableData;
        this.total = itemCount;
        this.data = listObject;
        this.data.forEach(item => {
          if (item.tags && item.tags.length > 0) {
            item.tags = JSON.parse(item.tags);
          }
        })
      });
    },
    open(language) {
      this.visible = true;
      this.init(language);
    },
    close() {
      this.visible = false;
    },
    handleCurrentChange(val) {
      this.currentRow = val;
    },
    submit() {
      if (!this.currentRow) {
        this.$warning(this.$t('project.code_segment.select_tip'));
        return;
      }
      this.result = this.$get("/custom/func/get/" + this.currentRow.id, res => {
        if (!res.data) {
          this.$warning(this.$t('project.code_segment.none_content'))
        }
        let {script} = res.data;
        this.$emit("addCustomFuncScript", script);
        this.close();
      });
    },
  }
}
</script>

<style scoped>
pre {
  margin: 0 0;
  font-family: "Helvetica Neue", Helvetica, "PingFang SC", "Hiragino Sans GB", Arial, sans-serif;
}
.search-bar {
  width: 300px;
  float: right;
}
.customFunc >>> .el-dialog__body {
  padding: 0 20px;
}
</style>
