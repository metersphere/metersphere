<template>
  <div style="margin: 5px">
    <div v-for="(item,index) in data" :key="index">
      <report-attach-info @deleteDetail="deleteDetail" v-if="item.type === 'txt'" :report-detail.sync="data[index]"
                          :read-only="false" ref="reportItem"/>
      <report-pic :report-id="item.Id" @deleteDetail="deleteDetail" v-if="item.type === 'report'"
                  :report-detail.sync="data[index]" :read-only="false" ref="reportItemPic"/>
    </div>
  </div>
</template>

<script>
import ReportAttachInfo from "@/business/enterprisereport/components/container/ReportAttachInfo";
import ReportPic from "@/business/enterprisereport/components/container/ReportPic";

export default {
  name: "EmailComponent",
  components: {ReportAttachInfo, ReportPic},
  props: {
    data: Array,
    readOnly: {
      type: Boolean,
      default() {
        return false;
      }
    }
  },
  created() {
  },
  watch: {
    data() {
    }
  },
  methods: {
    initDatas() {
      if (this.$refs.reportItemPic && this.$refs.reportItemPic.length > 0) {
        this.$refs.reportItemPic.forEach(item => {
          item.initData();
        });
      }
      if (this.$refs.reportItem && this.$refs.reportItem.length > 0) {
        this.$refs.reportItem.forEach(item => {
          item.initData();
        });
      }
    },
    deleteDetail(deleteData) {
      this.$alert(this.$t('commons.confirm') + this.$t('commons.delete') + deleteData.name + "？", '', {
        confirmButtonText: this.$t('commons.confirm'),
        callback: (action) => {
          if (action === 'confirm') {
            let index = this.data.indexOf(deleteData);
            if (index >= 0) {
              this.data.splice(index, 1);
            }
          }
        }
      });
    }
  },
}
</script>

<style scoped>
.ms-span {
  margin: 10px 10px 0px
}

.tip {
  float: left;
  font-size: 14px;
  border-radius: 2px;
  border-left: 2px solid #783887;
  margin: 10px 20px 0px;
}
</style>
