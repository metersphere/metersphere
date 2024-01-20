interface MimeTypes {
  [key: string]: string;
}

interface Signatures {
  [key: string]: string;
}

const MimeType = () => {
  const SPLITOR = '\uFEFF';
  const MIMETYPE: MimeTypes = {
    'application/km': '\uFFFF',
  };
  const SIGN: Signatures = {
    '\uFEFF': 'SPLITOR',
    '\uFFFF': 'application/km',
  };

  function getSplitor(): string {
    return SPLITOR;
  }

  function isPureText(text: string): boolean {
    // eslint-disable-next-line no-bitwise
    return !~text.indexOf(getSplitor());
  }

  function getPureText(text: string): string {
    if (isPureText(text)) {
      return text;
    }
    return text.split(getSplitor())[1];
  }

  function getMimeType(sign?: string): MimeTypes | string | null {
    if (sign !== undefined) {
      return SIGN[sign] || null;
    }
    return MIMETYPE;
  }

  function whichMimeType(text: string): MimeTypes | string | null {
    if (isPureText(text)) {
      return null;
    }
    return getMimeType(text.split(getSplitor())[0]);
  }

  function process(mimetype: string | false, text: string): string {
    if (!isPureText(text)) {
      const _mimetype = whichMimeType(text);
      if (!_mimetype) {
        throw new Error('unknown mimetype!');
      }
      text = getPureText(text);
    }
    if (mimetype === false) {
      return text;
    }
    return mimetype + SPLITOR + text;
  }

  function registerMimeTypeProtocol(type: string, sign: string): void {
    if (sign && SIGN[sign]) {
      throw new Error('sign has registered!');
    }
    if (type && !!MIMETYPE[type]) {
      throw new Error('mimetype has registered!');
    }
    SIGN[sign] = type;
    MIMETYPE[type] = sign;
  }

  function getMimeTypeProtocol(type: string, text?: string): any {
    const mimetype = MIMETYPE[type] || false;

    if (text === undefined) {
      return process.bind(null, mimetype);
    }

    return process(mimetype, text);
  }

  return {
    registerMimeTypeProtocol,
    getMimeTypeProtocol,
    getSplitor,
    getMimeType,
    whichMimeType,
    process,
    getPureText,
    isPureText,
  };
};

const MimeTypeRuntime = function r(this: any) {
  if (this.minder.supportClipboardEvent && !window.kity.Browser.gecko) {
    this.MimeType = MimeType();
  }
};

export default MimeTypeRuntime;
