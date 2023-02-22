package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.example.dto.request.CreateRoomReqDto;
import org.example.dto.request.JoinReqDto;
import org.example.dto.request.JoinRoomReqDto;
import org.example.dto.request.MessageReqDto;
import org.example.dto.request.RequestDto;
import org.example.dto.response.JoinRoomRespDto;
import org.example.dto.response.MessageRespDto;
import org.example.dto.response.ResponseDto;
import org.example.entity.Room;
import org.example.entity.RoomInfo;
import org.example.util.ServerUtil;

import com.google.gson.Gson;

import lombok.Getter;

@Getter
public class ServerThread extends Thread{
	@Getter
	private static List<ServerThread> socketList = new ArrayList<ServerThread>();
	private static List<Room> rooms = new ArrayList<>();
	@Getter
	private final Socket socket;
	private InputStream inputStream;
	private Gson gson;

	private String username;
	private Room room;

	private ServerUtil serverUtil;

	public ServerThread(Socket socket) {
		this.socket = socket;
		this.gson = new Gson();
		socketList.add(this);

	}



	@Override
	public void run() {
		try {
			inputStream = socket.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
			
			while(true) {
				
				String request = reader.readLine();

				RequestDto<String> requestDto = gson.fromJson(request, RequestDto.class);
				OutputStream outputStream;
				PrintWriter writer;
				ResponseDto responseDto;

				switch (requestDto.getResource()) {
				case "join":
					JoinReqDto joinReqDto = gson.fromJson(requestDto.getBody(), JoinReqDto.class);
					username = joinReqDto.getNickname();

					reflashRoomList();
					break;

				case "message":
					MessageReqDto messageReqDto = gson.fromJson(requestDto.getBody(), MessageReqDto.class);
					String message = messageReqDto.getMessage();
					
					MessageRespDto messageRespDto = new MessageRespDto(message);
					String messageJson = gson.toJson(messageRespDto);
					
					responseDto = new ResponseDto(requestDto.getResource(), "ok", messageJson);
					
//					serverUtil.sendToRoom(requestDto.getResource(), "ok", createdRoomName, createRoomReqDto.getRoomName());
					
					outputStream = this.getSocket().getOutputStream();
					writer = new PrintWriter(outputStream, true);
							
					writer.println(gson.toJson(responseDto));
					
					break;

				case "createRoom":
					CreateRoomReqDto createRoomReqDto = gson.fromJson(requestDto.getBody(), CreateRoomReqDto.class);
					room = new Room(createRoomReqDto.getKingName(), createRoomReqDto.getRoomName());
					room.getUsers().add(this);
					rooms.add(room);
					
					reflashRoomList();

					break;

				case "joinRoom":
					JoinRoomReqDto joinRoomReqDto = gson.fromJson(requestDto.getBody(), JoinRoomReqDto.class);

					String roomName = joinRoomReqDto.getRoomName();
					rooms.forEach(room -> {
						if(room.getRoomName().equals(roomName)) {
							this.room = room;
						}
					});

					room.getUsers().add(this);
					
					String joinName = joinRoomReqDto.getJoinName();

					JoinRoomRespDto joinRoomRespDto = new JoinRoomRespDto(joinName, roomName);
					String joinRoomJson = gson.toJson(joinRoomRespDto);

					responseDto = new ResponseDto(requestDto.getResource(), "ok", joinRoomJson);
					sendAll(responseDto, room.getUsers());
					break;

				case "deleteRoom":


				default:
					break;
				}
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}
	
	public void reflashRoomList() {
		List<String> roomNames = new ArrayList<>();
		rooms.forEach(room -> {
			roomNames.add(room.getRoomName());
		});
		ResponseDto responseDto = new ResponseDto("reflashRoom", "ok", gson.toJson(roomNames));
		
		sendAll(responseDto, socketList);
	}
	
	public void sendAll(ResponseDto responseDto, List<? extends ServerThread> threadList) {
		
		for (ServerThread s : threadList) {

			OutputStream outputStream;
			try {
				outputStream = s.getSocket().getOutputStream();

				PrintWriter writer = new PrintWriter(outputStream, true);

				writer.println(gson.toJson(responseDto));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
}
