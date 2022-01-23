<template>
  <ms-table-header
      :condition="condition"
      @search="$emit('refresh')"
      :show-create="false"
      :tip="$t('commons.search_by_id_name_tag')">
    <template v-slot:title>
      场景用例
    </template>
    <template v-slot:button>
      <ms-table-button v-permission="['PROJECT_TRACK_PLAN:READ+RELEVANCE_OR_CANCEL']" icon="el-icon-connection"
                       :content="$t('test_track.plan_view.relevance_test_case')"
                       @click="$emit('relevanceCase')"/>
      <version-select v-xpack :project-id="projectId" @changeVersion="$emit('changeVersion', $event)" margin-left="10"/>
    </template>

  </ms-table-header>
</template>

<script>
import MsTableHeader from "../../../../../common/components/MsTableHeader";
import MsTableButton from "../../../../../common/components/MsTableButton";
import MsEnvironmentSelect from "../../../../../api/definition/components/case/MsEnvironmentSelect";
const requireComponent = require.context('@/business/components/xpack/', true, /\.vue$/);
const VersionSelect = requireComponent.keys().length > 0 ? requireComponent("./version/VersionSelect.vue") : {};

export default {
  name: "TestPlanScenarioListHeader",
  components: {
    MsEnvironmentSelect, MsTableButton, MsTableHeader, 'VersionSelect': VersionSelect.default,
  },
  props: ['condition', 'isReadOnly', 'projectId'],
  methods: {}
};
</script>

<style scoped>

/deep/ .environment-select {
  margin-right: 10px;
}

</style>
