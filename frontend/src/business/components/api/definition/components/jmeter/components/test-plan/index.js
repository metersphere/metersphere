import HashTreeElement from "../../hashtree";
import {elementProp, stringProp} from "../../props";

const DEFAULT_OPTIONS = {
  options: {
    attributes: {guiclass: "TestPlanGui", testclass: "TestPlan", testname: "TestPlan", enabled: "true"},
  }
};

export const TYPE = "TestPlan";

export default class TestPlan extends HashTreeElement {
  constructor(options = DEFAULT_OPTIONS) {
    super(options);
    this.type = TYPE;
    this.hashTree = [];
  }

  updateProps() {
    let collectionProp = this.props['TestPlan.user_defined_variables'].elements['Arguments.arguments'];
    collectionProp.clear();
    this.userDefinedVariables.forEach(variable => {
      if (variable.enable !== false) {
        let ep = elementProp(variable.name, "Argument");
        ep.add(stringProp("Argument.name", variable.name));
        ep.add(stringProp("Argument.value", variable.value));
        ep.add(stringProp("Argument.metadata", "="));
        collectionProp.add(ep)
      }
    })
  }

  toJson() {
    this.updateProps();
    return super.toJson();
  }
}

export const schema = {
  TestPlan: TestPlan
}
