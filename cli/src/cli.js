import vorpal from 'vorpal'
import { words } from 'lodash'
import { connect } from 'net'
import { Message } from './Message'

export const cli = vorpal();

let username;
let server;
let host;
let port;
let lastCommand;
const validCommands =
'  List of valid commands:'+'\n'+
'    broadcast   - send message to everybody'+'\n'+
'    cmnd        - list of valid commands'+'\n'+
'    disconnect  - disconnect from the chat'+'\n'+
'    echo        - send message back to yourself'+'\n'+
'    users       - list of currently connected users '+'\n'+
'    @username   - direct messaging';

cli
  .delimiter(cli.chalk['yellow']('ftd~$'));  

cli
  .mode('connect <username> <host> <port>')
  .delimiter(cli.chalk['green']('connected>'))  
  .init(function (args, callback) {
    username = args.username;
    host = args.host;
    port = args.port;
    server = connect({ host: host, port: port }, () => {
      server.write(new Message({ username, command: 'connect' }).toJSON() + '\n');
      this.log(cli.chalk['yellow']("Hi there! Watch out, your entries are case sensitive!"+'\n'+validCommands));
      callback();
    })

    //Console reader
    server.on('data', (buffer) => {                       
      this.log(Message.fromJSON(buffer).toString());       
     
    })

    server.on('end', () => {
      cli.exec('exit');
    })
  })
  .action(function (input, callback) {    
    const [ command, ...rest ] = words(input);
    const contents = rest.join(' ');

    //List of actions based on command input
    if (command === 'disconnect') {
      this.log(cli.chalk['yellow']("Ciao !!!"));
      server.end(new Message({ username, command }).toJSON() + '\n');

    } else if (command === 'echo' || command === 'users' || command === 'broadcast' || input[0] === "@") {
      lastCommand = command;
      server.write(new Message({ username, command, contents }).toJSON() + '\n');

    }else if (command === 'cmnd') {
      this.log(cli.chalk['yellow'](validCommands));

    }else if(lastCommand) {
      let msg = command.concat(' ',contents);
      server.write(new Message({ username, command: lastCommand, contents: msg }).toJSON() + '\n');

    }else{
      this.log(cli.chalk['red'](`Command <${command}> was not recognized`)+'\n'+cli.chalk['yellow'](validCommands));
    }

    callback();
  })
