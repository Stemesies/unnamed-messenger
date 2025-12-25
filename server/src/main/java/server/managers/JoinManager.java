package server.managers;

import server.elements.Group;
import server.elements.User;
import server.requests.JoinRequest;

public class JoinManager extends JoinRequest implements Manager {

    /**
     * TODO написать документацию.
     *
     * @param user - пользователь
     * @param group - группа, в которую вступает пользователь
     */
    private void joinGroup(User user, Group group) {
        group.includeUser(user.getUserId());
    }

    User user;
    Group group;

    public JoinManager(User user, Group group) {
        this.user = user;
        this.group = group;
    }

    @Override
    public void applyManager() {
        this.joinGroup(this.user, this.group);
    }

}
