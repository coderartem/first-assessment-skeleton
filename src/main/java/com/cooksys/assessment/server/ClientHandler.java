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
			
			Server srv = new Server();

			ObjectMapper mapper = new ObjectMapper();
			BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));

			while (!socket.isClosed()) {
				String raw = reader.readLine();
				Message message = mapper.readValue(raw, Message.class);
				message.setTimestamp(timeNow());
				
				
				switch (message.getCommand()) {
				
					case "connect":
						
						srv.addUser(message.getUsername(),writer);
						
						log.info("user <{}> connected", message.getUsername());
						message.setContents(" has connected");
						toEverybody(writer,mapper,message);
						break;
						
					case "disconnect":
						log.info("user <{}> disconnected", message.getUsername());
						message.setContents(" has disconnected");
						toEverybody(writer,mapper,message);
						Server.users.remove(message.getUsername(), writer);
						this.socket.close();
						break;
						
					case "echo":
						log.info("user <{}> echoed message <{}>", message.getUsername(), message.getContents());
						message.setContents("(echo): " + message.getContents());
						String response = mapper.writeValueAsString(message);  // transform message from obj to JSON format
						writer.write(response);
						writer.flush();
						break;
						
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
						
					case "broadcast":
						message.setContents("(all): " + message.getContents());
						toEverybody(writer,mapper,message);
						break;
						
					default:
						for(String names : Server.users.keySet()){
							if(message.getCommand().equals(names)){
								PrintWriter prwt = Server.users.get(names);
								message.setContents("(whisper): " + message.getContents());
								prwt.write(mapper.writeValueAsString(message));
								prwt.flush();
								
							}
						}
						break;
						
				}
				
			}

		} catch (IOException e) {
			log.error("Something went wrong :/", e);
		}
	}
	
	public String timeNow (){
		return new SimpleDateFormat("hh:mm:ss a").format(new Date());
		
	}
	
	public void toEverybody(PrintWriter writer, ObjectMapper mapper, Message message) throws JsonProcessingException{
		
		for(PrintWriter wrt : Server.users.values() ){
			if(wrt!=writer){
			wrt.write(mapper.writeValueAsString(message));
			wrt.flush();
			}
		}
		
	}

}
