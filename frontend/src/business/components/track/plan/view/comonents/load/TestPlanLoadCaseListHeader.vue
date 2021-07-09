<template>
  <ms-table-header
    :condition="condition"
    @search="$emit('refresh')"
    :show-create="false"
    :tip="$t('commons.search_by_name_or_id')">
    <template v-slot:title>
      {{ $t('test_track.plan.load_case.case') }}
    </template>
    <template v-slot:button>
      <ms-table-button v-permission="['PROJECT_TRACK_PLAN:READ+RELEVANCE_OR_CANCEL']" icon="el-icon-connection"
                       :content="$t('test_track.plan_view.relevance_test_case')"
                       @click="$emit('relevanceCase')"/>
    </template>

  </ms-table-header>
</template>

<script>
import MsTableButton from "@/business/components/common/components/MsTableButton";
import MsTableHeader from "@/business/components/common/components/MsTableHeader";

export default {
  name: "TestPlanLoadCaseListHeader",
  components: {MsTableButton, MsTableHeader},
  props: ['condition'],
};
</script>

<style scoped>

</style>
