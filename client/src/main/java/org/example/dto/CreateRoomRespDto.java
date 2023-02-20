package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.example.entity.Room;

import java.util.List;

@AllArgsConstructor
@Getter
public class CreateRoomRespDto {
	private List<String> roomList;
}
