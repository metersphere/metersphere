<template>

  <common-component :title="$t('test_track.plan_view.failure_case')">
    <functional-failure-cases-list v-if="showFunctional" :functional-test-cases="failureTestCases.functionalTestCases"/>
    <api-failure-cases-list v-if="showApi" :api-test-cases="failureTestCases.apiTestCases"/>
    <scenario-failure-cases-list v-if="showScenario" :scenario-test-cases="failureTestCases.scenarioTestCases"/>
    <load-failure-cases-list v-if="showLoad" :load-test-cases="failureTestCases.loadTestCases"/>
  </common-component>

</template>

<script>
    import CommonComponent from "./CommonComponent";
    import PriorityTableItem from "../../../../../common/tableItems/planview/PriorityTableItem";
    import TypeTableItem from "../../../../../common/tableItems/planview/TypeTableItem";
    import MethodTableItem from "../../../../../common/tableItems/planview/MethodTableItem";
    import StatusTableItem from "../../../../../common/tableItems/planview/StatusTableItem";
    import {hub} from "@/business/components/track/plan/event-bus";
    import FunctionalFailureCasesList from "./component/FunctionalFailureCasesList";
    import ApiFailureCasesList from "./component/ApiFailureCasesList";
    import ScenarioFailureCasesList from "./component/ScenarioFailureCasesList";
    import LoadFailureCasesList
      from "@/business/components/track/plan/view/comonents/report/TemplateComponent/component/LoadFailureCasesList";
    export default {
      name: "FailureResultAdvanceComponent",
      components: {
        ScenarioFailureCasesList,
        ApiFailureCasesList,
        FunctionalFailureCasesList,
        StatusTableItem, MethodTableItem, TypeTableItem, PriorityTableItem, CommonComponent, LoadFailureCasesList},
      props: {
        failureTestCases: {
          type: Object,
          default() {
            return {
              functionalTestCases: [

              ],
              apiTestCases: [

              ],
              scenarioTestCases: [

              ],
              loadTestCases: [

              ]
            }
          }
        }
      },
      computed: {
        showFunctional() {
          return this.failureTestCases.functionalTestCases.length > 0
            || (this.failureTestCases.apiTestCases.length <= 0 && this.failureTestCases.scenarioTestCases.length <= 0 && this.failureTestCases.loadTestCases.length <= 0);
        },
        showApi() {
          return this.failureTestCases.apiTestCases.length > 0;
        },
        showScenario() {
          return this.failureTestCases.scenarioTestCases.length > 0;
        },
        showLoad() {
          return this.failureTestCases.loadTestCases.length > 0;
        },
      },
      methods: {
        goFailureTestCase(row) {
          hub.$emit("openFailureTestCase", row);
        }
      }
    }
</script>

<style scoped>

  /deep/ .failure-cases-list-header {
    font-size: 14px;
    font-weight: 600;
    margin-bottom: 10px;
  }

  .failure-cases-list {
    margin-bottom: 40px;
  }

</style>
