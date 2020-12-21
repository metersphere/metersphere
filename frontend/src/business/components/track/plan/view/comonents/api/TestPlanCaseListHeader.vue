<template>
  <ms-table-header :is-tester-permission="true" :condition="condition" @search="$emit('refresh')"
                   :show-create="false" :tip="$t('commons.search_by_name_or_id')">
    <!--<template v-slot:title>-->
    <!--<node-breadcrumb class="table-title" :nodes="selectParentNodes" @refresh="breadcrumbRefresh"/>-->
    <!--</template>-->
    <template v-slot:button>
      <!--<ms-table-button :is-tester-permission="true" v-if="!showMyTestCase" icon="el-icon-s-custom"-->
      <!--:content="$t('test_track.plan_view.my_case')" @click="initTable"/>-->
      <!--<ms-table-button :is-tester-permission="true" v-if="showMyTestCase" icon="el-icon-files"-->
      <!--:content="$t('test_track.plan_view.all_case')" @click="searchMyTestCase"/>-->
      <ms-table-button :is-tester-permission="true" icon="el-icon-connection"
                       :content="$t('test_track.plan_view.relevance_test_case')"
                       @click="$emit('relevanceCase')"/>
      <!--<ms-table-button :is-tester-permission="true" v-if="!testPlan.reportId" icon="el-icon-document"-->
      <!--:content="$t('test_track.plan_view.create_report')" @click="openTestReport"/>-->
      <!--<ms-table-button :is-tester-permission="true" v-if="testPlan.reportId" icon="el-icon-document"-->
      <!--:content="$t('test_track.plan_view.view_report')" @click="openReport"/>-->
      <!--<ms-table-button :is-tester-permission="true" icon="el-icon-document-remove"-->
      <!--:content="$t('test_track.plan_view.cancel_all_relevance')" @click="handleDeleteBatch"/>-->
    </template>

    <!--<template v-slot:searchBarBefore>-->
      <!--<ms-environment-select :project-id="projectId" :is-read-only="isReadOnly" @setEnvironment="setEnvironment"/>-->
    <!--</template>-->
  </ms-table-header>
</template>

<script>
    import MsTableHeader from "../../../../../common/components/MsTableHeader";
    import MsTableButton from "../../../../../common/components/MsTableButton";
    import MsEnvironmentSelect from "../../../../../api/definition/components/case/MsEnvironmentSelect";
    export default {
      name: "TestPlanCaseListHeader",
      components: {MsEnvironmentSelect, MsTableButton, MsTableHeader},
      props: ['condition', 'projectId', 'isReadOnly', 'planId'],
      methods: {
        setEnvironment(data) {
          if (this.planId) {
            let param = {};
            param.id = this.planId;
            param.environmentId = data.id;
            this.$post('/test/plan/edit', param, () => {
              this.$emit('setEnvironment', data);
            });
          }
        }
      }
    }
</script>

<style scoped>

  /deep/ .environment-select {
    margin-right: 10px;
  }

</style>
