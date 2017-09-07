
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
    let textColor;
    let backgroundColor;

    //Choosing a text and background colors based on message type
    switch (this.command){
      case 'echo':
        backgroundColor = 'bgYellow';
        textColor = 'blue';
      break;
      case 'users':
        backgroundColor = 'bgBlack';
        textColor = 'magenta';
      break;
      case 'broadcast':
        backgroundColor = 'bgGreen';
        textColor = 'red';
      break;
      case 'connect':
      case 'disconnect':
        backgroundColor = 'bgBlack';
        textColor = 'red';
      break;
      case 'service':
        backgroundColor = 'bgBlack';
        textColor = 'yellow';
      break;
      default:
        backgroundColor = 'bgRed';
        textColor = 'green';
      break;
    }

    return this.timestamp + " " + '<' + cli.chalk['magenta'](this.username) + '>' + " " +  cli.chalk[backgroundColor](cli.chalk[textColor](this.contents));
  }
}
