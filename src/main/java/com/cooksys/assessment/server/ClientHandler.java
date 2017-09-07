package com.cooksys.assessment.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cooksys.assessment.model.Message;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ClientHandler implements Runnable {
	private Logger log = LoggerFactory.getLogger(ClientHandler.class);
	
	private Socket socket;

	public ClientHandler(Socket socket) {
		super();
		this.socket = socket;
	}

	public void run() {
		try {
			
			Server server = new Server();

			ObjectMapper mapper = new ObjectMapper();
			BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));

			while (!socket.isClosed()) {
				
				String raw = reader.readLine();
				Message message = mapper.readValue(raw, Message.class);
				
				//Setting a timestamp in all incoming messages before to send them out
				message.setTimestamp(timeNow());
				
				//choose action based on command
				switch (message.getCommand()) {
				
				//Connect new user to the chat
					case "connect":
						
						//Checking if new user's name already chosen 
						for (String clientName : Server.users.keySet()){
							if(message.getUsername().equals(clientName)){
								message.setContents("Name is unavailable, please choose another one and try to re-connect");
								message.setUsername("");
								writer.write(mapper.writeValueAsString(message));
								writer.flush();
								this.socket.close();
								break;
							}
						}
						//Adding new user and his PrinteWriter to users Map on a server
						if(message.getUsername()!=""){
							server.addUser(message.getUsername(),writer);
							log.info("user <{}> connected", message.getUsername());
							message.setContents(" has connected");
							toEverybody(writer,mapper,message);
						}
						break;
						
						//Disconnect user from the chat, delete it's name and printer from users Map collection on server
					case "disconnect":
							log.info("user <{}> disconnected", message.getUsername());
							message.setContents(" has disconnected");
							toEverybody(writer,mapper,message);
							Server.users.remove(message.getUsername(), writer);
							this.socket.close();
						break;
						
						//Handle echo messages
					case "echo":
							message.setContents("(echo): " + message.getContents());
							String response = mapper.writeValueAsString(message);  // transform message from obj to JSON format
							writer.write(response);
							writer.flush();
						break;
						
						//Send list of users from users Map on Server to anyone who ask for it
					case "users":
							String res= "";
							for(String names : Server.users.keySet()){
								res+="\n" + "@" + names;
							}
							message.setContents(res);
							message.setUsername("Currently connected users:");
							writer.write(mapper.writeValueAsString(message));
							writer.flush();
						break;
						
						//Handling broadcast messages and sending confirmation to sender
					case "broadcast":
							message.setContents("(all): " + message.getContents());
							toEverybody(writer,mapper,message);
							message.setContents("sent to " + message.getCommand());
							message.setCommand("service");
							message.setUsername("YOU");
							writer.write(mapper.writeValueAsString(message));
							writer.flush();
						break;
						
						//Looking for specific user in Map of users to establish direct messaging
						//and sending confirmation to sender
					default:
							for(String names : Server.users.keySet()){
								if(message.getCommand().equals(names)){
									PrintWriter prwt = Server.users.get(names);
									message.setContents("(whisper): " + message.getContents());
									prwt.write(mapper.writeValueAsString(message));
									prwt.flush();
									message.setContents("sent to " + message.getCommand());
									message.setCommand("service");
									message.setUsername("YOU");
									writer.write(mapper.writeValueAsString(message));
									writer.flush();
								break;
							}
						}
						break;
						
				}
				
			}

		} catch (IOException e) {
			log.error("Something went wrong :/", e);
		}
	}
	
	//Time for timeStamp
	public String timeNow (){
		return new SimpleDateFormat("hh:mm:ss a").format(new Date());
		
	}
	
	//Use PrintWriters of all connected users to send a message to everybody
	public void toEverybody(PrintWriter writer, ObjectMapper mapper, Message message) throws JsonProcessingException{
		
		for(PrintWriter wrt : Server.users.values() ){
			if(wrt!=writer){
			wrt.write(mapper.writeValueAsString(message));
			wrt.flush();
			}
		}
		
	}

}
