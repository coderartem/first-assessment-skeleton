import vorpal from 'vorpal'
import { words } from 'lodash'
import { connect } from 'net'
import { Message } from './Message'

export const cli = vorpal()

let username
let server
let host
let port

let cmmnd;

cli
  .delimiter(cli.chalk['yellow']('ftd~$'))  //preMessage, cli talk to command Line

cli
  .mode('connect <username> <host> <port>')
  .delimiter(cli.chalk['green']('connected>'))  //preMessage
  .init(function (args, callback) {
    username = args.username
    host = args.host
    port = args.port
    server = connect({ host: host, port: port }, () => {
      server.write(new Message({ username, command: 'connect' }).toJSON() + '\n')
      callback()
    })

    server.on('data', (buffer) => {                       //Socket listener
      this.log(Message.fromJSON(buffer).toString());       // Write content of message to console
     
    })

    server.on('end', () => {
      cli.exec('exit')
    })
  })
  .action(function (input, callback) {     //Slushaet action v cli
    const [ command, ...rest ] = words(input)   //chitaet vvedennye dannye izvestnuyu commandu i message
    const contents = rest.join(' ')       //Sozdaet message, razdelyaet ego na slova

    if (command === 'disconnect') {
      server.end(new Message({ username, command }).toJSON() + '\n')

    } else if (command === 'echo') {
      cmmnd = command;
      server.write(new Message({ username, command, contents }).toJSON() + '\n')

//---------------
    } else if(command === 'broadcast'){
      cmmnd = command;
      server.write(new Message({ username, command, contents }).toJSON() + '\n')

    }else if(command === 'users'){
      cmmnd = command;
      server.write(new Message({ username, command, contents }).toJSON() + '\n')  
    
    } else if(input[0] === "@"){
      cmmnd = command;
      server.write(new Message({ username, command, contents }).toJSON() + '\n')

    }else if(cmmnd){
      let msg = command.concat(' ',contents);
      server.write(new Message({ username, command: cmmnd, contents: msg }).toJSON() + '\n')


    //-------------------
    }else{
      console.log(cmmnd)
      this.log(`Command <${command}> was not recognized`)
    }

    callback()
  })
