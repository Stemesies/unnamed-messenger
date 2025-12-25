package server.managers;

import server.elements.User;
import server.requests.FriendRequest;

public class FriendManager extends FriendRequest implements Manager {
    /**
     * TODO написать документацию.
     *
     * @param user1 - кого принимают в друзья
     * @param user2 - кто принимает в друзья
     * @param response - ответ пользователя, которому отправили завявку (принял/отклонил)
     */
    private void addToFriends(User user1, User user2, boolean response) {
        if (response)
            user2.getFriends().add(user1.getUserId());
    }

    User user1;
    User user2;
    boolean response;

    public FriendManager(User user1, User user2, boolean resp) {
        this.user1 = user1;
        this.user2 = user2;
        this.response = resp;
    }

    @Override
    public void applyManager() {
        this.addToFriends(this.user1, this.user2, this.response);
    }
}
