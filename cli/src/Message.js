
import { cli } from './cli';

export class Message {
  static fromJSON (buffer) {
    return new Message(JSON.parse(buffer.toString()))
  }

  constructor ({ username, command, contents, timestamp }) {
    this.username = username
    this.command = command
    this.contents = contents
    this.timestamp = timestamp
  }

  toJSON () {
    return JSON.stringify({
      username: this.username,
      command: this.command,
      contents: this.contents
    })
  }

  toString () {
    let clr;
    switch (this.command){
      case 'echo':
      clr='blue';
      break;
      case 'users':
      clr='magenta';
      break;
      case 'broadcast':
      clr='green';
      break;
      case 'connect':
      case 'disconnect':
      clr = 'red';
      break;
      default:
      clr='cyan';
      break;
    }

    return this.timestamp + " " + '<' + cli.chalk['magenta'](this.username) + '>' + " " +  cli.chalk[clr](this.contents)
  }
}
