<template>
  <el-row>
    <el-col :span="16">
      <el-select v-model="selfQuantity" placeholder=" " size="mini" filterable default-first-option
                 allow-create
                 class="timing_select" :disabled="selfChoose" @change="chooseChange(true)">
        <el-option
          v-for="item in quantityOptions"
          :key="item"
          :label="item"
          :value="item">
        </el-option>
      </el-select>
      <el-select v-model="selfUnit" placeholder=" " size="mini"
                 class="timing_select" :disabled="selfChoose" @change="chooseChange(true)">
        <el-option
          v-for="item in unitOptions"
          :key="item.value"
          :label="item.label"
          :value="item.value">
        </el-option>
      </el-select>
    </el-col>
  </el-row>
</template>

<script>

export default {
  name: "MiniTimingItem",
  components: {},
  props: {
    choose: {
      type: Boolean,
      default() {
        return false;
      },
    },
    expr: {
      type: String,
      default() {
        return "";
      }
    },
    title: {
      type: String,
      default() {
        return "";
      }
    },
    shareLink: {
      type: Boolean,
      default() {
        return false;
      },
    },
    unitOptions: {
      type: Array,
      default() {
        return [
          {value: "H", label: this.$t('commons.date_unit.hour')},
          {value: "D", label: this.$t('commons.date_unit.day')},
          {value: "M", label: this.$t('commons.date_unit.month')},
          {value: "Y", label: this.$t('commons.date_unit.year')},
        ];
      },
    }
  },
  watch: {
    expr: {
      handler(val) {
        this.parseExpr(val);
      },
      immediate: true
    },
    choose(val) {
      this.selfChoose = val;
    }
  },
  data() {
    return {
      selfQuantity: "",
      selfUnit: "",
      selfChoose: this.choose,
      selfExpr: this.expr,
      quantityOptions: [
        "1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
        "11", "12", "13", "14", "15", "16", "17", "18", "19", "20",
        "21", "22", "23", "24", "25", "26", "27", "28", "29", "30",
        "31"
      ],
    }
  },
  methods: {
    chooseChange(val) {
      if (val && (!this.selfQuantity || !this.selfUnit)) {
        this.$warning(this.$t('project.please_select_cleaning_time'));
        this.selfChoose = false;
        return false;
      }
      if (val && this.selfQuantity) {
        if (typeof this.selfQuantity !== 'number' && isNaN(parseInt(this.selfQuantity))) {
          this.$warning(this.$t('api_test.request.time') + this.$t('commons.type_of_integer'));
          this.selfChoose = false;
          return false;
        }
        if (this.selfQuantity <= 0 || parseInt(this.selfQuantity) <= 0) {
          this.$warning(this.$t('commons.adv_search.operators.gt') + "0");
          this.selfChoose = false;
          return false;
        }
        if (Number(this.selfQuantity) > parseInt(this.selfQuantity)) {
          this.$warning(this.$t('api_test.request.time') + this.$t('commons.type_of_integer'));
          this.selfChoose = false;
          return false;
        }
      }

      this.$emit("update:choose", val);
      this.$emit("update:expr", parseInt(this.selfQuantity) + this.selfUnit);
      this.$emit("chooseChange");
    },
    parseExpr(expr) {
      if (!expr) {
        return;
      }
      // 1D 1M 1Y
      this.selfUnit = expr.substring(expr.length - 1);
      this.selfQuantity = expr.substring(0, expr.length - 1);
    }
  }
}
</script>

<style scoped>
.timing_name {
  color: var(--primary_color);
}

.timing_select {
  width: 80px;
  margin-left: 2px;
}
</style>
