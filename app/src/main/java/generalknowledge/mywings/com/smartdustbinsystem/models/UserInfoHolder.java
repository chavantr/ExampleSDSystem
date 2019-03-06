package generalknowledge.mywings.com.smartdustbinsystem.models;

public class UserInfoHolder {

    private User user;

    public static UserInfoHolder getInstance() {
        return UserInfoHolderHelper.INSTANCE;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    private static class UserInfoHolderHelper {
        static final UserInfoHolder INSTANCE = new UserInfoHolder();
    }

}
