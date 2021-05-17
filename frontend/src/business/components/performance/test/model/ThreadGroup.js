import {xml2json} from "xml-js";

let travel = function (elements, threadGroups) {
  if (!elements) {
    return;
  }
  for (let element of elements) {
    switch (element.name) {
      case "SetupThreadGroup":
      case "PostThreadGroup":
      case "ThreadGroup":
      case "kg.apc.jmeter.threads.UltimateThreadGroup":
      case "com.blazemeter.jmeter.threads.concurrency.ConcurrencyThreadGroup":
      case "com.blazemeter.jmeter.threads.arrivals.FreeFormArrivalsThreadGroup":
      case "com.blazemeter.jmeter.threads.arrivals.ArrivalsThreadGroup":
      case "com.octoperf.jmeter.OctoPerfThreadGroup":
        threadGroups.push(element);
        break;
      default:
        break;
    }
    travel(element.elements, threadGroups);
  }
};

export function findThreadGroup(jmxContent, handler) {
  let jmxJson = JSON.parse(xml2json(jmxContent));
  let threadGroups = [];
  travel(jmxJson.elements, threadGroups);
  threadGroups.forEach(tg => {
    tg.deleted = 'false';
    tg.handler = handler;
    tg.enabled = tg.attributes.enabled;
    tg.tgType = tg.name;
    if (tg.name === 'SetupThreadGroup' || tg.name === 'PostThreadGroup') {
      tg.threadType = 'ITERATION';
      tg.threadNumber = 1;
      tg.iterateRampUp = 1;
    } else {
      tg.threadType = 'DURATION';
      tg.threadNumber = 1;
    }
    tg.unit = 'S';
  });
  return threadGroups;
}


export function findTestPlan(jmxContent) {
  let jmxJson = JSON.parse(xml2json(jmxContent));
  for (let element of jmxJson.elements[0].elements[0].elements) {
    if (element.name === 'TestPlan') {
      return element;
    }
  }
}
