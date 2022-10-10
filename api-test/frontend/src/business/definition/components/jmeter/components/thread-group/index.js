import HashTreeElement from "../../hashtree";

const DEFAULT_OPTIONS = {
  options: {
    attributes: {guiclass: "ThreadGroupGui", testclass: "ThreadGroup", testname: "ThreadGroup", enabled: "true"},
  }
};

export const TYPE = "ThreadGroup";

export default class ThreadGroup extends HashTreeElement {
  constructor(options = DEFAULT_OPTIONS) {
    super(options);
    this.type = TYPE;
    this.onSampleError = true;
  }
}

export const schema = {
  ThreadGroup: ThreadGroup
}
