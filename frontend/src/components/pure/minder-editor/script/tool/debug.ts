import format from './format';

// eslint-disable-next-line @typescript-eslint/no-empty-function
function noop() {}

function stringHash(str: string): number {
  let hash = 0;
  for (let i = 0; i < str.length; i++) {
    hash += str.charCodeAt(i);
  }
  return hash;
}

class Debug {
  private flagged: boolean;

  public log: (...args: any) => void;

  constructor(flag: string) {
    this.flagged = window.location.search.indexOf(flag) !== -1;

    if (this.flagged) {
      const h = stringHash(flag) % 360;
      const flagStyle = format(
        'background: hsl({0}, 50%, 80%); ' +
          'color: hsl({0}, 100%, 30%); ' +
          'padding: 2px 3px; ' +
          'margin: 1px 3px 0 0;' +
          'border-radius: 2px;',
        h
      );

      const textStyle = 'background: none; color: black;';
      this.log = () => {
        // eslint-disable-next-line no-console
        console.log(format('%c{0}%c{1}', flag), flagStyle, textStyle);
      };
    } else {
      this.log = noop;
    }
  }
}

export default Debug;
