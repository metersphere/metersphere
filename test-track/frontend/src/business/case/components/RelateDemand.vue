<template>
  <div>
    <el-dialog
      :title="$t('test_track.batch_related_requirements')"
      :visible.sync="dialogVisible"
      width="40%"
      class="batch-edit-dialog"
      :destroy-on-close="true"
      @close="handleClose"
      append-to-body
      :close-on-click-modal="false"
      v-loading="result.loading"
    >
      <span class="select-row">{{$t('test_track.batch_operate_select_row_count', [size])}}</span>

      <el-form :model="form" label-position="right" size="medium" ref="form" style="margin-top: 24px;">
        <el-form-item :label="$t('test_track.related_requirements')" prop="demandId">
          <el-cascader v-model="demandValue" :show-all-levels="false" :options="demandOptions"
                       clearable filterable :filter-method="filterDemand" style="width: 100%;">
            <template slot-scope="{ data }">
              <div class="story-box">
                <div class="story-platform">{{ data.platform }}</div>
                <div class="story-label" v-if="data.value === 'other'">
                  {{ $t("test_track.case.other") }}
                </div>
                <div class="story-label" v-else>{{ data.label }}</div>
              </div>
            </template>
          </el-cascader>
        </el-form-item>

        <el-form-item :label="$t('test_track.case.demand_name_id')" prop="demandName" v-if="form.demandId === 'other'">
          <el-input v-model="form.demandName"></el-input>
        </el-form-item>
      </el-form>
      <template v-slot:footer>
<!--        <ms-dialog-footer @cancel="dialogVisible = false" @confirm="submit()"/>-->
        <el-button @click="dialogVisible = false" size="small">{{ $t('commons.cancel') }}</el-button>
        <el-button v-prevent-re-click :type="!form.demandId ? 'info' : 'primary'" @click="submit"
                   @keydown.enter.native.prevent size="small" :disabled="!form.demandId" style="margin-left: 12px">{{ $t('commons.confirm') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import MsDialogFooter from "metersphere-frontend/src/components/MsDialogFooter";
import {getCurrentProjectID, removeGoBackListener} from "@/business/utils/sdk-utils";
import {issueDemandList} from "@/api/issue";

export default {
  name: "RelateDemand",
  components: {
    MsDialogFooter,
  },
  data() {
    return {
      result: {},
      form: {
        demandId: ''
      },
      demandValue: [],
      demandOptions: [],
      dialogVisible: false,
      size: 0,
    }
  },
  watch: {
    demandValue() {
      if (this.demandValue.length > 0) {
        this.form.demandId = this.demandValue[this.demandValue.length - 1];
      } else {
        this.form.demandId = null;
      }
    }
  },
  methods: {
    open(size) {
      this.form = {};
      this.dialogVisible = true;
      this.getDemandOptions();
      if (size) {
        this.size = size;
      } else {
        this.size = this.$parent.selectDataCounts;
      }
    },
    handleClose() {
      this.form = {};
      this.dialogVisible = false;
      removeGoBackListener(this.handleClose);
    },
    getDemandOptions() {
      if (this.demandOptions.length === 0) {
        this.result = {loading: true};
        issueDemandList(getCurrentProjectID())
          .then(response => {
            this.demandOptions = [];
            if (response.data && response.data.length > 0) {
              this.buildDemandCascaderOptions(response.data, this.demandOptions, []);
            }
            this.demandOptions.unshift({
              value: 'other',
              label: 'Other: ' + this.$t('test_track.case.other'),
              platform: 'Other'
            });
            if (this.form.demandId === 'other') {
              this.demandValue = ['other'];
            }
            this.result = {loading: false};
          }).catch(() => {
            this.demandOptions.unshift({
              value: 'other',
              label: 'Other: ' + this.$t('test_track.case.other'),
              platform: 'Other'
            });
            if (this.form.demandId === 'other') {
              this.demandValue = ['other'];
            }
            this.result = {loading: false};
          })
      }
    },
    buildDemandCascaderOptions(data, options, pathArray) {
      data.forEach(item => {
        let option = {
          label: item.platform + ': ' + item.name,
          value: item.id
        }
        options.push(option);
        pathArray.push(item.id);
        if (item.id === this.form.demandId) {
          this.demandValue = [...pathArray]; // 回显级联选项
        }
        if (item.children && item.children.length > 0) {
          option.children = [];
          this.buildDemandCascaderOptions(item.children, option.children, pathArray);
        }
        pathArray.pop();
      });
    },
    filterDemand(node, keyword) {
      return !!(keyword && node.text.toLowerCase().indexOf(keyword.toLowerCase()) !== -1);
    },
    submit() {
      if (!this.form.demandId) {
        this.$warning(this.$t('test_track.demand.relate_is_null_warn'), false);
        return;
      }
      if (this.form.demandId === 'other' && !this.form.demandName) {
        this.$warning(this.$t('test_track.demand.relate_name_is_null_warn'), false);
        return;
      }
      this.$emit('batchRelate', this.form);
      this.form = {demandId: ''};
      this.demandValue = [];
      this.dialogVisible = false;
    }
  }
}
</script>

<style scoped>
.demand-span {
  display: inline-block;
  max-width: 400px;
  white-space: nowrap;
  text-overflow: ellipsis;
  overflow: hidden;
  word-break: break-all;
  margin-right: 5px;
}

.select-row {
  font-family: 'PingFang SC';
  font-style: normal;
  font-weight: 400;
  font-size: 14px;
  line-height: 22px;
  color: #646A73;
  flex: none;
  order: 1;
  align-self: center;
  flex-grow: 0;
}

.el-form-item__label {
  line-height: 36px;
  font-family: 'PingFang SC';
  font-style: normal;
  font-weight: 400;
  font-size: 14px;
  line-height: 22px;
  color: #1F2329;
  flex: none;
  order: 0;
  flex-grow: 0;
  margin-bottom: 8px;
}

:deep(.el-button--small span) {
  font-family: 'PingFang SC';
  font-style: normal;
  font-weight: 400;
  font-size: 14px;
  line-height: 22px;
  position: relative;
  top: -5px;
}

.el-button--small {
  min-width: 80px;
  height: 32px;
  border-radius: 4px;
}

/* 关联需求下拉框 */
.story-box {
  display: flex;
}

.story-platform {
  font-weight: 400;
  font-size: 14px;
  line-height: 22px;
  color: #783887;
  padding: 1px 6px;
  gap: 4px;
  width: 49px;
  height: 24px;
  background: rgba(120, 56, 135, 0.2);
  border-radius: 2px;
  margin-right: 8px;
}

.story-label {
  line-height: 22px;
  color: #1f2329;
}
</style>
