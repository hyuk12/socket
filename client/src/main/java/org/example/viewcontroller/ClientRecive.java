package org.example.viewcontroller;

import java.awt.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import com.google.gson.JsonIOException;
import org.example.dto.response.*;
import org.example.entity.Room;
import org.example.entity.RoomInfo;
import org.example.view.ChattingClient;


import com.google.gson.Gson;


import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ClientRecive extends Thread{
	
	private final Socket socket;
	private InputStream inputStream;
	private Gson gson;

	private CardLayout mainLayout;
	private boolean isRunning = true;
	
	@Override
	public void run() {
		try {
			inputStream = socket.getInputStream();
			BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
			gson = new Gson();
			
			while(isRunning) {
				String request = in.readLine();
				ResponseDto responseDto = gson.fromJson(request, ResponseDto.class);
				
				switch(responseDto.getResource()) {
					
					case "message" :
						MessageRespDto messageRespDto = gson.fromJson(responseDto.getBody(), MessageRespDto.class);
						String message = messageRespDto.getMessage();
						
						
						ChattingClient.getInstance().getContentView().append(message + "\n");

						break;
						
					case "createRoom":
						try {
							CreateRoomRespDto createRoomRespDto = gson.fromJson(responseDto.getBody(), CreateRoomRespDto.class);
							
							String rNames = createRoomRespDto.getRoomName();
							

							ChattingClient.getInstance().getRoomTitle().setText("제목: "+ rNames + "의 방입니다.");
							ChattingClient.getInstance().getContentView().setText("");
							ChattingClient.getInstance().getContentView().append(rNames + "방이 생성되었습니다."+"\n");
						
						} catch (JsonIOException e) {
							e.printStackTrace();

						}

						break;
						
					case "joinRoom":
						JoinRoomRespDto joinRoomRespDto = gson.fromJson(responseDto.getBody(), JoinRoomRespDto.class);

						String joinName = joinRoomRespDto.getJoinName();
						String roomName = joinRoomRespDto.getRoomName();
						
						if(ChattingClient.getInstance().getNickname().equals(joinName)) {
							
							ChattingClient.getInstance().getContentView().setText("");
						}
						
						ChattingClient.getInstance().getRoomTitle().setText("제목: "+ roomName+ "의 방입니다.");
						ChattingClient.getInstance().getContentView().append(joinName + "님이 방에 입장하셨습니다."+"\n");
						
						break;
						
					case "exitRoom":
						System.out.println("리시브: "+responseDto.getBody());
						ExitRespDto exitRespDto = gson.fromJson(responseDto.getBody(), ExitRespDto.class);
						String exitUsername = exitRespDto.getKingName();

						ChattingClient.getInstance().getContentView().append( exitUsername+ "님이 나가셨습니다."+"\n");
						
						break;
						
					case "exitKingRoom":
						
						mainLayout = (CardLayout)ChattingClient.getInstance().getMainPanel().getLayout();
						mainLayout.show(ChattingClient.getInstance().getMainPanel(), "chattingList");
						
						break;
						
					case "reflashRoom":
						List<String> roomNames = gson.fromJson(responseDto.getBody(), List.class);
						
						
						ChattingClient.getInstance().getModel().clear();
						ChattingClient.getInstance().getModel().addAll(roomNames);
						
						break;
						
					case "exit":
						throw new InterruptedException();
				}
				
			}
		} catch (InterruptedException e) {
			ChattingClient.getInstance().exit();
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
}
