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
						message.setContents("has connected");
						writer.write(mapper.writeValueAsString(message));
						writer.flush();
						break;
						
					case "disconnect":
						log.info("user <{}> disconnected", message.getUsername());
						message.setContents("has disconnected");
						writer.write(mapper.writeValueAsString(message));
						writer.flush();
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
						message.setUsername("Connected users");
						writer.write(mapper.writeValueAsString(message));
						writer.flush();
						break;
						
					case "broadcast":
						
						message.setContents("(all): " + message.getContents());
						for(PrintWriter wrt : Server.users.values() ){
							if(wrt!=writer){
							wrt.write(mapper.writeValueAsString(message));
							wrt.flush();
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

}
