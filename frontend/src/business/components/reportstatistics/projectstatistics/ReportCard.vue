<template>
  <div class="ms-content">
    <el-row class="row-class">
      <el-col :span="4" class="col-one">
        <el-card :body-style="{ padding: '0px' }" class="ms-col" @click.native="openCard('countTestCase')">
          <img src="@/assets/test_case_analysis_png.png" class="image">
          <div style="padding: 10px;">
            <span>{{ $t('commons.report_statistics.test_case_count') }}</span>
            <div class="bottom clearfix">
              <time class="time">{{ $t('commons.report_statistics.test_case_count_activity') }}</time>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card :body-style="{ padding: '0px' }" class="ms-col" @click.native="openCard('trackTestCase')">
          <img src="@/assets/track.jpg" class="image">
          <div style="padding: 10px;">
            <span>{{ $t('commons.report_statistics.test_case_analysis') }}</span>
            <div class="bottom clearfix">
              <time class="time">{{ $t('commons.report_statistics.test_case_activity') }}</time>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card :body-style="{ padding: '0px' }" class="ms-col">
          <img src="@/assets/other.png" class="image">
          <div style="padding: 10px;">
            <span>{{ $t('commons.report_statistics.reserved') }}</span>
            <div class="bottom clearfix">
              <time class="time">{{ $t('commons.report_statistics.test_case_activity') }}</time>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card :body-style="{ padding: '0px' }" class="ms-col">
          <img src="@/assets/other.png" class="image">
          <div style="padding: 10px;">
            <span>{{ $t('commons.report_statistics.reserved') }}</span>
            <div class="bottom clearfix">
              <time class="time">{{ $t('commons.report_statistics.test_case_activity') }}</time>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card :body-style="{ padding: '0px' }" class="ms-col">
          <img src="@/assets/other.png" class="image">
          <div style="padding: 10px;">
            <span>{{ $t('commons.report_statistics.reserved') }}</span>
            <div class="bottom clearfix">
              <time class="time">{{ $t('commons.report_statistics.test_case_activity') }}</time>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card :body-style="{ padding: '0px' }" class="ms-col">
          <img src="@/assets/other.png" class="image">
          <div style="padding: 10px;">
            <span>{{ $t('commons.report_statistics.reserved') }}</span>
            <div class="bottom clearfix">
              <time class="time">{{ $t('commons.report_statistics.test_case_activity') }}</time>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import {hasPermission} from "@/common/js/utils";

export default {
  name: "ReportCard",
  components: {},
  data() {
    return {}
  },
  methods: {
    openCard(type) {
      if (!hasPermission('PROJECT_REPORT_ANALYSIS:READ')) {
        this.$warning("无查看权限！");
        return;
      }
      this.$emit('openCard', type);
    }
  },
}
</script>

<style scoped>
.row-class {
  display: flex;
}

.col-one {
  display: flex;
  align-items: stretch;
}

.time {
  font-size: 13px;
  color: #999;
}

.bottom {
  margin-top: 13px;
  line-height: 12px;
}

.button {
  padding: 0;
  float: right;
}

.image {
  width: 100%;
  display: block;
}

.clearfix:before,
.clearfix:after {
  display: table;
  content: "";
}

.clearfix:after {
  clear: both
}

.ms-col {
  margin: 5px;
}

.ms-content {
  padding: 15px 10px 15px 15px;
}

.ms-col:hover {
  cursor: pointer;
  border-color: #783887;
}
</style>
