package com.example.chatify;

public class Conversation {

    private String user1_id;
    private String user2_id;
    private String conversation_id;
    private ChatMessage lastMessage;

    public Conversation(String user1_id, String user2_id,ChatMessage lastMessage)
    {
        this.user1_id = user1_id;
        this.user2_id = user2_id;
        conversation_id = user1_id +"_"+ user2_id;
        this.lastMessage = lastMessage;
    }

    public Conversation()
    {

    }

    public String getConversation_id() {
        return conversation_id;
    }

    public void setConversation_id(String conversation_id) {
        this.conversation_id = conversation_id;
    }

    public String getUser2_id() {
        return user2_id;
    }

    public void setUser2_id(String user2_id) {
        this.user2_id = user2_id;
    }

    public String getUser1_id() {
        return user1_id;
    }

    public void setUser1_id(String user1_id) {
        this.user1_id = user1_id;
    }

    public void setLastMessage(ChatMessage lastMessage) {
        this.lastMessage = lastMessage;
    }

    public ChatMessage getLastMessage() {
        return lastMessage;
    }
}
