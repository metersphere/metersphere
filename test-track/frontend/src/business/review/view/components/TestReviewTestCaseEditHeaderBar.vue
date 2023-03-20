<template>
  <div class="bar-container">
    <div class="left-item bar-item">
      <el-tooltip v-if="longerThanLimit" :content="testCase.name" effect="dark" placement="bottom-start">
        <span>
          {{ this.displayName }}
        </span>
      </el-tooltip>
      <span v-else>
        {{ testCase.name }}
      </span>

      <el-popover v-if="reviewerStatus && reviewerStatus.length > 0" placement="right" trigger="hover">

        <div class="status-tip" v-for="item in reviewerStatus" :key="item.author">
          <span style="font-size: 14px;color: #909399;font-weight: bold">
            {{ item.author }}
          </span>
          <review-status :value="item.status"/>
        </div>

        <review-status slot="reference" :value="testCase.reviewStatus"/>
      </el-popover>
      <review-status v-else :value="testCase.reviewStatus"/>
    </div>

    <div class="right-item bar-item click-item">
      <i class="el-icon-close" @click="close()"/>
    </div>

    <div class="right-item bar-item">
      <el-divider direction="vertical"/>
    </div>

    <div class="right-item bar-item click-item" @click="openTestCase">
      <img class="icon-item"
           src="/assets/module/figma/icon_describe_outlined.svg"/>
      <span>
        {{ $t('test_track.case.view_case') }}
      </span>
    </div>

  </div>

</template>

<script>

import ReviewStatus from "@/business/case/components/ReviewStatus";
import {getReviewerStatusComment} from "@/api/test-review-test-case";
import MsUserIcon from "metersphere-frontend/src/components/MsUserIcon";
import {openCaseEdit} from "@/business/case/test-case";
import { checkProjectPermission } from "@/api/testCase";

export default {
  name: "TestReviewTestCaseEditHeaderBar",
  components: {ReviewStatus, MsUserIcon},
  data() {
    return {
      reviewerStatus: null,
      nameLengthLimit: 50
    };
  },
  props: {
    testCase: {
      type: Object,
      default() {
        return {};
      },
    }
  },
  computed: {
    displayName() {
      if (this.longerThanLimit) {
        return this.testCase.name.substring(0, this.nameLengthLimit) + '...';
      }
      return this.testCase.name;
    },
    longerThanLimit() {
      return this.testCase.name && this.testCase.name.length > this.nameLengthLimit;
    }
  },
  watch: {
    testCase() {
      this.getReviewerStatus();
    }
  },
  methods: {
    getReviewerStatus() {
      if (this.testCase && this.testCase.id) {
        getReviewerStatusComment(this.testCase.id)
          .then(r => {
            this.reviewerStatus = r.data;
          });
      } else {
        this.reviewerStatus = null;
      }
    },
    openTestCase() {
      checkProjectPermission(this.testCase.projectId).then((r) => {
        if (r.data) {
          openCaseEdit({caseId: this.testCase.caseId, projectId: this.testCase.projectId}, this);
        } else {
          this.$error(this.$t("commons.project_permission"));
        }
      });
    },
    close() {
      this.$emit('close');
    }
  }
}
</script>

<style scoped>

.bar-container {
  height: 56px;
  width: 100%;
  box-shadow: 0px -1px 4px rgba(31, 35, 41, 0.4);
  background: #FFFFFF;
  padding: 15px 24px;
  box-sizing: border-box;
}

.right-item {
  float: right;
}

.left-item {
  float: left;
}

.bar-item {
  display: inline-block;
  line-height: 24px;
  margin-right: 24px;
}

.icon-item {
  width: 14.67px;
  height: 13px;
}

.el-divider {
  width: 1px;
  height: 20px;

  background: #BBBFC4;

  flex: none;
  order: 1;
  flex-grow: 0;
}

.click-item {
  cursor:pointer;
}

.status-tip {
  text-align: center;
  margin-bottom: 10px;
}

</style>
