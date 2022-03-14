import {xml2json} from "xml-js";

let travel = function (elements, threadGroups, relateFiles) {
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
      case "CSVDataSet":
        relateFiles.push(element);
        break;
      default:
        break;
    }
    travel(element.elements, threadGroups, relateFiles);
  }
};

export function findThreadGroup(jmxContent, handler) {
  let jmxJson = JSON.parse(xml2json(jmxContent));
  let threadGroups = [], relateFiles = [];
  travel(jmxJson.elements, threadGroups, relateFiles);

  let csvFiles = new Set;
  relateFiles.forEach(f => {
    if (f.attributes.enabled === 'false') {
      return;
    }
    f.elements.forEach(e => {
      if (e.attributes.name === 'filename') {
        let filename = e.elements[0].text;
        if (filename.lastIndexOf('\\') > -1) {
          let split = filename.split('\\');
          filename = split[split.length - 1];
        } else {
          let split = filename.split('/');
          filename = split[split.length - 1];
        }
        csvFiles.add(filename);
      }
    });
  });
  threadGroups.forEach(tg => {
    for (let i = 0; i < tg.elements.length; i++) {
      if (tg.elements[i].attributes.name === 'ThreadGroup.on_sample_error') {
        tg.onSampleError = tg.elements[i].elements[0].text;
        break;
      }
    }
    tg.deleted = 'false';
    tg.handler = handler;
    tg.enabled = tg.attributes.enabled;
    tg.tgType = tg.name;
    tg.csvFiles = [...csvFiles];
    tg.strategy = 'auto';
    tg.resourceNodeIndex = 0;
    tg.ratios = '';
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
