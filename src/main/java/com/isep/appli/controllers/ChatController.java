package com.isep.appli.controllers;

import com.isep.appli.dbModels.*;
import com.isep.appli.models.ChatNotification;
import com.isep.appli.services.ChatRoomService;
import com.isep.appli.services.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final MessageService messageService;
    private final ChatRoomService chatRoomService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat")
    public void processMessage(@Payload Message message){
        Message saveMessage = messageService.save(message);
        messagingTemplate.convertAndSendToUser(
                String.valueOf(message.getChatRoom()), "queue/message",
                ChatNotification.builder()
                        .id(message.getId())
                        .sender(message.getSender())
                        .chatRoom(message.getChatRoom())
                        .content(message.getContent())
                        .build()
        );
    }

    @GetMapping("/chatPage")
    public String chatPage(Model model){
        return "chat/chat";
    }

    @GetMapping("/messages/{senderId}/{recipientId}")
    public ResponseEntity<List<Message>> findChatMessages( @PathVariable long recipientId) {
        ChatRoom chatRoom;
        if ( chatRoomService.findChatRoomById(recipientId).isPresent() ){
            chatRoom = chatRoomService.findChatRoomById(recipientId).get();
        } else {
            chatRoom = new ChatRoom();
        }
        return ResponseEntity.ok(messageService.findMessagesByChatRoom(chatRoom));
    }


}