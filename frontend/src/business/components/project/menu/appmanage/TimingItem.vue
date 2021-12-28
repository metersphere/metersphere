<template>
  <app-manage-item :title="$t('project.timing_clean_report')" :append-span="4" :middle-span="10" :prepend-span="10">
    <template #middle>
      <span class="timing_name">{{ $t('project.keep_recent') }}</span>
      <el-select v-model="selfQuantity" placeholder=" " size="mini"
                 class="timing_select" :disabled="selfChoose">
        <el-option
          v-for="item in quantityOptions"
          :key="item"
          :label="item"
          :value="item">
        </el-option>
      </el-select>
      <el-select v-model="selfUnit" placeholder=" " size="mini"
                 class="timing_select" :disabled="selfChoose">
        <el-option
          v-for="item in unitOptions"
          :key="item.value"
          :label="item.label"
          :value="item.value">
        </el-option>
      </el-select>
      <span class="timing_name" style="margin-left: 3px;">{{ $t('commons.report') }}</span>
    </template>
    <template #append>
      <el-switch v-model="selfChoose" @change="handleChange"></el-switch>
    </template>
  </app-manage-item>
</template>

<script>
import AppManageItem from "@/business/components/project/menu/appmanage/AppManageItem";

export default {
  name: "TimingItem",
  components: {
    AppManageItem
  },
  props: {
    quantity: {
      type: String,
      default() {
        return "";
      }
    },
    unit: {
      type: String,
      default() {
        return "";
      }
    },
    choose: {
      type: Boolean,
      default() {
        return false;
      }
    }
  },
  watch: {
    selfQuantity(val) {
      this.$emit("update:quantity", val);
    },
    selfUnit(val) {
      this.$emit("update:unit", val);
    },
    selfChoose(val) {
      this.chooseChange(val);
    }
  },
  data() {
    return {
      selfQuantity: "",
      selfUnit: "",
      selfChoose: false,
      quantityOptions: [
        "1", "2", "3", "4", "5", "6", "7", "8", "9",
        "10", "11", "12", "13", "14", "15",
        "16", "17", "18", "19", "20", "21",
        "22", "23", "24", "25", "26",
        "27", "28", "29", "30", "31"
      ],
      unitOptions: [
        {value: "D", label: "天"},
        {value: "M", label: "月"},
        {value: "Y", label: "年"},
      ]
    }
  },
  methods: {
    chooseChange(val) {
      this.$emit("update:choose", val);
      this.$emit("chooseChange");
    },
    handleChange(val) {
      if (val && (!this.selfQuantity || !this.selfUnit)) {
        this.$warning(this.$t('project.please_select_cleaning_time'));
        this.selfChoose = false;
        return false;
      }
    }
  }
}
</script>

<style scoped>
.timing_name {
  color: var(--primary_color);
}

.timing_select {
  width: 60px;
  margin-left: 3px;
}
</style>
