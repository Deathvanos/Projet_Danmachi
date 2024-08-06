package com.isep.appli.repositories;

import com.isep.appli.dbModels.ChatRoom;
import com.isep.appli.dbModels.Message;
import com.isep.appli.services.ChatRoomService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    Message findMessageById(Long id);

    List<Message> findByChatRoom(ChatRoom chatRoom);
}