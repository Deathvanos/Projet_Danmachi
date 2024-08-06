package com.isep.appli.services;

import com.isep.appli.dbModels.ChatRoom;
import com.isep.appli.dbModels.Message;
import com.isep.appli.dbModels.Personnage;
import com.isep.appli.models.FormattedMessage;
import com.isep.appli.repositories.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final PersonnageService personnageService;


    public Message findById(Long id) {
        return messageRepository.findMessageById(id);
    }

    public Message save(Message message) {
        return messageRepository.save(message);
    }

    public void deleteMessageById(long id) {
        Message message = findById(id);
        messageRepository.delete(message);
    }

    public String displayDestination(Personnage personnage) {
        return personnage.getFirstName() + " " + personnage.getLastName();
    }

    public String formatDate(Date date) {
        if (date == null) {
            return " ";
        }
        Date now = new Date();
        SimpleDateFormat simpleDateFormat;
        if (now.getDate() == date.getDate() && now.getMonth() == date.getMonth() && now.getYear() == date.getYear()) {
            simpleDateFormat = new SimpleDateFormat("HH:mm");
        }
        else {
            simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        }
        return simpleDateFormat.format(date);
    }

    public List<Message> findMessagesByChatRoom(ChatRoom chatRoom) {
        return messageRepository.findByChatRoom(chatRoom);
    }

    public List<FormattedMessage> getFormattedMessagesByChatRoom(ChatRoom chatRoom, Personnage personnage) {
        List<Message> messages = findMessagesByChatRoom(chatRoom);
        Collections.sort(messages, Comparator.comparing(Message::getDate));
        List<FormattedMessage> formattedMessages = new ArrayList();
        for (Message message : messages) {
            FormattedMessage formattedMessage = new FormattedMessage (
                    message.getId(),
                    message.getContent(),
                    displayDestination(message.getSender()),
                    formatDate(message.getDate()),
                    !message.getSender().equals(personnage)
            );
            formattedMessages.add(formattedMessage);
        }
        return formattedMessages;
    }
}