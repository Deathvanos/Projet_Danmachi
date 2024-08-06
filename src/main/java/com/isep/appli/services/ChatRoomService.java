package com.isep.appli.services;

import com.isep.appli.dbModels.ChatRoom;
import com.isep.appli.repositories.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatRoomService {
    private final ChatRoomRepository chatRoomRepository;
    private final MessageService messageService;

    public Optional<ChatRoom> findChatRoomById(long id){return chatRoomRepository.findById(id);}
}
