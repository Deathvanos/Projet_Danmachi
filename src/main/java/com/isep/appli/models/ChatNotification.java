package com.isep.appli.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.isep.appli.dbModels.ChatRoom;
import com.isep.appli.dbModels.Personnage;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
public class ChatNotification {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sender_id", nullable = false)
    @JsonBackReference
    private Personnage sender;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "chatroom_id", nullable = false)
    @JsonBackReference
    private ChatRoom chatRoom;
}
