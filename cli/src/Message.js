

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
   /* let clr;
    switch (this.command){
      case 'echo':
      clr="\x1b[34m%s\x1b[0m";
      break;
      case 'users':
      clr="\x1b[36m%s\x1b[0m";
      break;
      case 'broadcast':
      clr="\x1b[32m%s\x1b[0m";
      break;
      default:
      clr="\x1b[37m%s\x1b[0m";
      break;
    }*/

    return /*clr + ',' + */this.timestamp + " " + '<' + this.username + '>' + " " +  this.contents
  }
}
