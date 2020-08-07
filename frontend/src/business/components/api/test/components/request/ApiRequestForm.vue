<template>
  <div>
    <component :is="component" :is-read-only="isReadOnly" :request="request"/>
<!--    <ms-scenario-results :scenarios="content.scenarios"/>-->
  </div>
</template>

<script>
  import {Request, RequestFactory} from "../../model/ScenarioModel";
  import MsApiHttpRequestForm from "./ApiHttpRequestForm";
  import MsApiDubboRequestForm from "./ApiDubboRequestForm";
  import MsScenarioResults from "../../../report/components/ScenarioResults";

  export default {
    name: "MsApiRequestForm",
    components: {MsScenarioResults, MsApiDubboRequestForm, MsApiHttpRequestForm},
    props: {
      request: Request,
      isReadOnly: {
        type: Boolean,
        default: false
      }
    },
    data() {
      return {
        reportId: "",
        content:{}
      }
    },
    computed: {
      component({request: {type}}) {
        let name;
        switch (type) {
          case RequestFactory.TYPES.DUBBO:
            name = "MsApiDubboRequestForm";
            break;
          default:
            name = "MsApiHttpRequestForm";
        }
        return name;
      }
    },
    created() {
      this.getReport();
    },
    methods: {
      getReport() {
        // // this.reportId = "00143d36-a58a-477e-a05a-556c1d48046c";
        // if (this.reportId) {
        //   let url = "/api/report/get/" + this.reportId;
        //   this.$get(url, response => {
        //     let report = response.data || {};
        //     if (response.data) {
        //       // if (this.isNotRunning) {
        //         try {
        //           this.content = JSON.parse(report.content);
        //         } catch (e) {
        //           console.log(report.content)
        //           throw e;
        //         }
        //         // this.getFails();
        //         // this.loading = false;
        //       // }
        //       // else {
        //       //   setTimeout(this.getReport, 2000)
        //       // }
        //     } else {
        //       this.loading = false;
        //       this.$error(this.$t('api_report.not_exist'));
        //     }
        //   });
        // }
      }
    }
  }
</script>

<style scoped>

  .scenario-results {
    margin-top: 15px;
  }

</style>
