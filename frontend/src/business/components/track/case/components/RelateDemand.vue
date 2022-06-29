<template>
  <div>
    <el-dialog
      :title="$t('test_track.please_related_requirements')"
      :visible.sync="dialogVisible"
      width="30%"
      class="batch-edit-dialog"
      :destroy-on-close="true"
      @close="handleClose"
      v-loading="result.loading"
    >
      <el-form :model="form" label-position="right" size="medium" ref="form">
        <el-form-item :label="$t('test_track.related_requirements')" prop="demandId">
          <el-cascader v-model="demandValue" :show-all-levels="false" :options="demandOptions"
                       clearable filterable :filter-method="filterDemand" style="width: 100%;">
            <template slot-scope="{ node, data }">
              <span class="demand-span" :title="data.label">{{ data.label }}</span>
            </template>
          </el-cascader>
        </el-form-item>

        <el-form-item :label="$t('test_track.case.demand_name_id')" prop="demandName" v-if="form.demandId === 'other'">
          <el-input v-model="form.demandName"></el-input>
        </el-form-item>
      </el-form>
      <template v-slot:footer>
        <ms-dialog-footer @cancel="dialogVisible = false" @confirm="submit()"/>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import {getCurrentProjectID, removeGoBackListener} from "@/common/js/utils";
import MsDialogFooter from "@/business/components/common/components/MsDialogFooter";

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
      dialogVisible: false
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
    open() {
      this.form = {};
      this.dialogVisible = true;
      this.getDemandOptions();
    },
    handleClose() {
      this.form = {};
      this.dialogVisible = false;
      removeGoBackListener(this.handleClose);
    },
    getDemandOptions() {
      if (this.demandOptions.length === 0) {
        this.result = {loading: true};
        this.$get("/issues/demand/list/" + getCurrentProjectID()).then(response => {
          this.demandOptions = [];
          if (response.data.data && response.data.data.length > 0) {
            this.buildDemandCascaderOptions(response.data.data, this.demandOptions, []);
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
        });
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
        this.$warning(this.$t('test_track.demand.relate_is_null_warn'));
        return;
      }
      if (this.form.demandId === 'other' && !this.form.demandName) {
        this.$warning(this.$t('test_track.demand.relate_name_is_null_warn'));
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
</style>
