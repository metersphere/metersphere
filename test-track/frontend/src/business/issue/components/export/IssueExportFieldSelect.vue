<template>
  <div v-loading="loading">
    <div class="export-title"
         @click="showSelect = !showSelect">
      <span>
       {{ $t('test_track.case.import.select_import_field') }}
      </span>
      <i class="el-icon-arrow-down"
         v-if="showSelect"/>
      <i class="el-icon-arrow-left"
         v-if="!showSelect"/>
    </div>

    <el-divider/>

    <div v-show="showSelect">
      <el-checkbox class="select-all-checkbox"
                   v-model="selectAll" @change="handleSelectAllChange">
        {{ $t('test_track.case.import.select_import_all_field') }}
      </el-checkbox>

      <issue-export-field-select-item
        type="EXPORT_BASE_FIELD"
        :title="$t('test_track.case.import.base_field')"
        :fields="baseFields"
        @selectAllChange="handleItemSelectAllChange"
        ref="baseSelectItem"/>

      <issue-export-field-select-item
        type="EXPORT_CUSTOM_FIELD"
        :title="$t('test_track.case.import.custom_field')"
        :fields="customFields"
        @selectAllChange="handleItemSelectAllChange"
        ref="customSelectItem"/>

      <issue-export-field-select-item
        type="EXPORT_OTHER_FIELD"
        :title="$t('test_track.case.import.other_field')"
        :fields="otherFields"
        @selectAllChange="handleItemSelectAllChange"
        ref="otherSelectItem"/>

      <div class="other-field-tip">
        {{ $t('test_track.case.import.other_field_tip') }}
      </div>

    </div>
  </div>
</template>

<script>
import IssueExportFieldSelectItem from "@/business/issue/components/export/IssueExportFieldSelectItem";
import {enableThirdPartTemplate, getIssuePartTemplateWithProject} from "@/api/issue";
import {getCurrentProjectID} from "@/business/utils/sdk-utils";

export default {
  name: "IssueExportFieldSelect",
  components: {IssueExportFieldSelectItem},
  data() {
    return {
      selectAll: false,
      showSelect: true,
      baseFields: [
        {
          id: 'id',
          key: 'A',
          name: this.$t("test_track.issue.id"),
          enable: true,
          disabled: true
        },
        {
          id: 'title',
          key: 'B',
          name: this.$t("test_track.issue.title"),
          enable: true,
          disabled: true
        },
        {
          id: 'description',
          key: 'D',
          name: this.$t("test_track.issue.description"),
          enable: true
        }
      ],
      loading: false,
      customFields: [],
      otherFields: [
        {
          id: 'creator',
          key: 'E',
          name: this.$t("commons.creator"),
          enable: false
        },
        {
          id: 'caseCount',
          key: 'A',
          name: this.$t("test_track.home.case_size"),
          enable: false
        },
        {
          id: 'comment',
          key: 'B',
          name: this.$t("commons.comment"),
          enable: false
        },
        {
          id: 'resource',
          key: 'C',
          name: this.$t("test_track.issue.issue_resource"),
          enable: false
        },
        {
          id: 'platform',
          key: 'D',
          name: this.$t("test_track.issue.issue_platform"),
          enable: false
        },
        {
          id: 'createTime',
          key: 'E',
          name: this.$t("commons.create_time"),
          enable: false
        },
      ]
    }
  },
  computed: {
    selectItems() {
      return [this.$refs.baseSelectItem, this.$refs.customSelectItem, this.$refs.otherSelectItem];
    }
  },
  created() {
    this.loading = true;
    getIssuePartTemplateWithProject((template) => {
      if (template.platform && template.platform !== 'Local') {
        this.customFields.push({
          id: 'platformStatus',
          key: '#',
          name: this.$t("test_track.issue.platform_status"),
          enable: true
        });
      }
      if (template.platform && template.platform === 'Tapd') {
        this.customFields.push({
          id: this.$t("test_track.issue.tapd_current_owner"),
          key: '&',
          name: this.$t("test_track.issue.tapd_current_owner"),
          enable: true
        });
      }
      template.customFields.forEach(item => {
        item.enable = true;
        this.customFields.push(item);
      });
      this.loading = false;
    });
    enableThirdPartTemplate(getCurrentProjectID())
      .then((res) => {
        if (res.data) {
          // 第三方模板
          this.baseFields = this.baseFields.filter(item => (item.id !== 'description' && item.id !== 'title'));
        }
      });
  },
  methods: {
    getExportParam() {
      return {
        baseHeaders: this.selectItems[0].getExportParam(),
        customHeaders: this.selectItems[1].getExportParam(),
        otherHeaders: this.selectItems[2].getExportParam(),
      }
    },
    handleSelectAllChange() {
      this.selectItems.forEach(item => {
        item.selectAllChange(this.selectAll);
      });
    },
    handleItemSelectAllChange() {
      let isSelectAll = true;
      this.selectItems.forEach(item => {
        if (!item.selectAll) {
          isSelectAll = false;
        }
      });
      this.selectAll = isSelectAll;
    }
  }
}
</script>

<style scoped>

.export-title {
  font-size: 16px;
  font-weight: bold;
  margin: 20px 5px 15px 0px;
}

.export-title span:first-child {
  margin-right: 5px;
}

.select-all-checkbox {
  margin-top: 10px
}

.export-title {
  cursor: pointer;
}

.other-field-tip {
  margin-top: 30px;
  font-size: 10px;
  color: #9ea0a3;
}
</style>
