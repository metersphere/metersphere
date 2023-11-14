/* eslint-disable */


function _typeof(obj) { if (typeof Symbol === "function" && typeof Symbol.iterator === "symbol") { _typeof = function _typeof(obj) { return typeof obj; }; } else { _typeof = function _typeof(obj) { return obj && typeof Symbol === "function" && obj.constructor === Symbol && obj !== Symbol.prototype ? "symbol" : typeof obj; }; } return _typeof(obj); }

/**
 * Check if arg is either an array with at least 1 element, or a dict with at least 1 key
 * @return boolean
 */
function isCollapsable(arg) {
  return arg instanceof Object && Object.keys(arg).length > 0;
}
/**
 * Check if a string represents a valid url
 * @return boolean
 */


function isUrl(string) {
  const regexp = /^(ftp|http|https):\/\/(\w+:{0,1}\w*@)?(\S+)(:[0-9]+)?(\/|\/([\w#:.?+=&%@!\-/]))?/;
  return regexp.test(string);
}
/**
 * Transform a json object into html representation
 * @return string
 */


function json2html(json, options) {
  let html = '';

  if (typeof json === 'string') {
    // Escape tags
    const tmp = json.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');

    if (json.includes('Number')) {
      html += "<span class=\"json-literal\">".concat(json.replace(/Number\(([^)]+)\)/g, "$1"), "</span>");
    } else if (isUrl(tmp)) {
      html += "<a href=\"".concat(tmp, "\" class=\"json-string\">").concat(tmp, "</a>");
    } else {
      html += "<span class=\"json-string\">\"".concat(tmp, "\"</span>");
    }
  } else if (typeof json === 'number') {
    html += "<span class=\"json-literal\">".concat(json, "</span>");
  } else if (typeof json === 'boolean') {
    html += "<span class=\"json-literal\">".concat(json, "</span>");
  } else if (json === null) {
    html += '<span class="json-literal">null</span>';
  } else if (json instanceof Array) {
    if (json.length > 0) {
      html += '[<ol class="json-array">';

      for (let i = 0; i < json.length; i += 1) {
        html += "<li data-key-type=\"array\" data-key=\"".concat(i, "\">"); // Add toggle button if item is collapsable

        if (isCollapsable(json[i])) {
          html += '<a href class="json-toggle"></a>';
        }

        html += json2html(json[i], options); // Add comma if item is not last

        if (i < json.length - 1) {
          html += ',';
        }

        html += '</li>';
      }

      html += '</ol>]';
    } else {
      html += '[]';
    }
  } else if (_typeof(json) === 'object') {
    let keyCount = Object.keys(json).length;

    if (keyCount > 0) {
      html += '{<ul class="json-dict">';

      for (const key in json) {
        if (json.hasOwnProperty(key)) {
          html += "<li data-key-type=\"object\" data-key=\"".concat(key, "\">");
          const keyRepr = options.outputWithQuotes ? "<span class=\"json-string\">\"".concat(key, "\"</span>") : key; // Add toggle button if item is collapsable

          if (isCollapsable(json[key])) {
            html += "<a href class=\"json-toggle\">".concat(keyRepr, "</a>");
          } else {
            html += keyRepr;
          }

          html += `<span class="pick-path" title="Pick path">&${  options.pickerIcon  };</span>`;
          html += ": ".concat(json2html(json[key], options)); // Add comma if item is not last

          keyCount -= 1;

          if (keyCount > 0) {
            html += ',';
          }

          html += '</li>';
        }
      }

      html += '</ul>}';
    } else {
      html += '{}';
    }
  }

  return html;
}
/**
 * Remove an event listener
 * @param  {String}   event    The event type
 * @param  {Node}     elem     The element to remove the event to (optional, defaults to window)
 * @param  {Function} callback The callback that ran on the event
 * @param  {Boolean}  capture  If true, forces bubbling on non-bubbling events
 */


function off(event, elem, callback, capture) {
  let captureIntern = capture;
  let callbackIntern = callback;
  let elemIntern = elem;

  if (typeof elem === 'function') {
    captureIntern = callback;
    callbackIntern = elem;
    elemIntern = window;
  }

  captureIntern = !!captureIntern;
  elemIntern = typeof elemIntern === 'string' ? document.querySelector(elemIntern) : elemIntern;
  if (!elemIntern) return;
  elemIntern.removeEventListener(event, callbackIntern, captureIntern);
}
/**
 * Equivalent of JQuery $().siblings(sel) with callback features
 *
 * Retrieve all siblings/neighbors of a node
 * Usage:
 * - siblings(node, '.collapse', (sib) => { });
 * - const sibs = siblings(node);
 * - const sibs = siblings(node, '.collapse');
 *
 * @param {HTMLNode} el Element to apply siblings methods
 * @param {String} sel CSS Selector
 * @param {Function} callback (sib) => {}
 */


function siblings(el, sel, callback) {
  const sibs = [];

  for (let i = 0; i < el.parentNode.children.length; i += 1) {
    const child = el.parentNode.children[i];

    if (child !== el && typeof sel === 'string' && child.matches(sel)) {
      sibs.push(child);
    }
  } // If a callback is passed, call it on each sibs


  if (callback && typeof callback === 'function') {
    for (let _i = 0; _i < sibs.length; _i += 1) {
      callback(sibs[_i]);
    }
  }

  return sibs;
}
/**
 * Fire a click handler to the specified node.
 * Event handlers can detect that the event was fired programatically
 * by testing for a 'synthetic=true' property on the event object
 * @param {HTMLNode} node The node to fire the event handler on.
 */


function fireClick(node) {
  // Make sure we use the ownerDocument from the provided node to avoid cross-window problems
  let doc;

  if (node.ownerDocument) {
    doc = node.ownerDocument;
  } else if (node.nodeType === 9) {
    // the node may be the document itself, nodeType 9 = DOCUMENT_NODE
    doc = node;
  } else {
    throw new Error("Invalid node passed to fireEvent: ".concat(node.id));
  }

  if (node.dispatchEvent) {
    const eventClass = 'MouseEvents';
    const event = doc.createEvent(eventClass);
    event.initEvent('click', true, true); // All events created as bubbling and cancelable.

    event.synthetic = true;
    node.dispatchEvent(event, true);
  } else if (node.fireEvent) {
    // IE-old school style, you can drop this if you don't need to support IE8 and lower
    const _event = doc.createEventObject();

    _event.synthetic = true; // allow detection of synthetic events

    node.fireEvent('onclick', _event);
  }
}
/**
 * Check if an element is visible or not
 * @param {HTMLNode} elem Element to check
 * @returns {Boolean}
 */


function isHidden(elem) {
  const width = elem.offsetWidth;
  const height = elem.offsetHeight;
  return width === 0 && height === 0 || window.getComputedStyle(elem).display === 'none';
}
/**
 * Method use to retrieve parents of given element
 * @param {HTMLNode} elem Element which we want parents
 * @param {Strign} sel selector to filter parents (CSS selectors)
 * @returns {Array<HTMLNode>}
 */


function getParents(elem, sel) {
  const result = [];

  for (let p = elem && elem.parentElement; p; p = p.parentElement) {
    if (typeof sel === 'string' && p.matches(sel)) {
      result.push(p);
    }
  }

  return result;
}

function HandlerEventToggle(elm, event) {
  // Change class
  elm.classList.toggle('collapsed'); // Fetch every json-dict and json-array to toggle them

  const subTarget = siblings(elm, 'ul.json-dict, ol.json-array', function (el) {
    el.style.display = el.style.display === '' || el.style.display === 'block' ? 'none' : 'block';
  }); // ForEach subtarget, previous siblings return array so we parse it

  for (let i = 0; i < subTarget.length; i += 1) {
    if (!isHidden(subTarget[i])) {
      // Parse every siblings with '.json-placehoder' and remove them (previous add by else)
      siblings(subTarget[i], '.json-placeholder', function (el) {
        return el.parentNode.removeChild(el);
      });
    } else {
      // count item in object / array
      const childs = subTarget[i].children;
      let count = 0;

      for (let j = 0; j < childs.length; j += 1) {
        if (childs[j].tagName === 'LI') {
          count += 1;
        }
      }

      const placeholder = count + (count > 1 ? ' items' : ' item'); // Append a placeholder

      subTarget[i].insertAdjacentHTML('afterend', "<a href class=\"json-placeholder\">".concat(placeholder, "</a>"));
    }
  } // Prevent propagation


  event.stopPropagation();
  event.preventDefault();
}

function ToggleEventListener(event) {
  let t = event.target;

  while (t && t !== this) {
    if (t.matches('a.json-toggle')) {
      HandlerEventToggle.call(null, t, event);
      event.stopPropagation();
      event.preventDefault();
    }

    t = t.parentNode;
  }
}

; // Simulate click on toggle button when placeholder is clicked

function SimulateClickHandler(elm, event) {
  siblings(elm, 'a.json-toggle', function (el) {
    return fireClick(el);
  });
  event.stopPropagation();
  event.preventDefault();
}

function SimulateClickEventListener(event) {
  let t = event.target;

  while (t && t !== this) {
    if (t.matches('a.json-placeholder')) {
      SimulateClickHandler.call(null, t, event);
    }

    t = t.parentNode;
  }
}

function PickPathHandler(elm) {
  if (targetList.length === 0) {
    return;
  }

  const $parentsList = getParents(elm, 'li').reverse();
  let pathSegments = [];

  for (let i = 0; i < $parentsList.length; i += 1) {
    let {key} = $parentsList[i].dataset;
    const {keyType} = $parentsList[i].dataset;

    if (keyType === 'object' && typeof key !== 'number' && options.processKeys && options.keyReplaceRegexPattern !== undefined) {
      const keyReplaceRegex = new RegExp(options.keyReplaceRegexPattern, options.keyReplaceRegexFlags);
      const keyReplacementText = options.keyReplacementText === undefined ? '' : options.keyReplacementText;
      key = key.replace(keyReplaceRegex, keyReplacementText);
    }

    pathSegments.push({
      key,
      keyType
    });
  }

  const quotes = {
    none: '',
    single: '\'',
    "double": '"'
  };
  const quote = quotes[options.pathQuotesType];
  pathSegments = pathSegments.map(function (segment, idx) {
    const isBracketsNotation = options.pathNotation === 'brackets';
    const isKeyForbiddenInDotNotation = !/^\w+$/.test(segment.key) || typeof segment.key === 'number';

    if (segment.keyType === 'array' || segment.isKeyANumber) {
      return "[".concat(segment.key, "]");
    }

    if (isBracketsNotation || isKeyForbiddenInDotNotation) {
      return "[".concat(quote).concat(segment.key).concat(quote, "]");
    }

    if (idx > 0) {
      return ".".concat(segment.key);
    }

    return segment.key;
  });
  const path = pathSegments.join('');

  for (let _i2 = 0; _i2 < targetList.length; _i2 += 1) {
    if (targetList[_i2].value !== undefined) {
      targetList[_i2].value = path;
    }
  }
}

function PickEventListener(event) {
  let t = event.target;

  while (t && t !== this) {
    if (t.matches('.pick-path')) {
      PickPathHandler.call(null, t);
    }

    t = t.parentNode;
  }
} // Uniq id generator


function uuidv4() {
  function randomString(length, chars) {
    let result = '';

    for (let i = length; i > 0; --i) {
      result += chars[Math.floor(Math.random() * chars.length)];
    }

    return result;
  }

  return randomString(32, 'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ');
}

var targetList = [];
var options = {};
/**
 * Plugin method
 * @param source: Element
 * @param json: a javascript object
 * @param target: NodeListOf<Element> | Element | { value: String }[] | { value: String }
 * @param opt: an optional options hash
 */

function jsonPathPicker(source, json, target, opt) {
  options = opt || {};

  if (!(source instanceof Element)) {
    return 1;
  }

  if (target) {
    if (target.length) {
      targetList = target;
    } else if (target.value) {
      targetList = [target];
    } else {
      return 3;
    }
  } else {
    return 3;
  } // Add to source unique identifier


  const uuid = uuidv4();
  source.id = source.id ? "".concat(source.id, " ").concat(uuid) : uuid;
  source.setAttribute('data-jsonpath-uniq-id', uuid);
  options.pathQuotesType = options.pathQuotesType !== undefined ? options.pathQuotesType : 'single'; // Transform to HTML

  options.pickerIcon = options.pickerIcon || '#x1f4cb';
  let html = json2html(json, options);
  if (isCollapsable(json)) html = "<a href class=\"json-toggle\"></a>".concat(html); // Insert HTML in target DOM element

  source.innerHTML = html; // Bind click on toggle buttons

  off('click', source);
  source.addEventListener('click', ToggleEventListener);
  source.addEventListener('click', SimulateClickEventListener); // Bind picker only if user didn't diseable it

  if (!options.WithoutPicker) {
    source.addEventListener('click', PickEventListener);
  } else {
    // Remove every picker icon
    const sourceSelector = source.getAttribute('data-jsonpath-uniq-id'); // Prevent affect other jp-picker

    document.querySelectorAll("[id*='".concat(sourceSelector, "'] .pick-path")).forEach(function (el) {
      return el.parentNode.removeChild(el);
    });
  }

  if (options.outputCollapsed === true) {
    // Trigger click to collapse all nodes
    const elms = document.querySelectorAll('a.json-toggle');

    for (let i = 0; i < elms.length; i += 1) {
      fireClick(elms[i]);
    }
  }
}
/**
 * Plugin clear method
 * @param source: Element
 */


function clearJsonPathPicker(source) {
  if (!(source instanceof Element)) {
    return 1;
  } // Remove event listener


  source.removeEventListener('click', PickEventListener);
  source.removeEventListener('click', ToggleEventListener);
  source.removeEventListener('click', SimulateClickEventListener);
}

export default {
  jsonPathPicker,
  clearJsonPathPicker
};