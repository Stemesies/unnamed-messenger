package server.requests;

import utils.elements.SuperRequest;

public class FriendRequest extends SuperRequest {
    public boolean requested;
    public boolean isResponsed;
    public int friendId; // id пользователя, добавляемого в друзья
}
