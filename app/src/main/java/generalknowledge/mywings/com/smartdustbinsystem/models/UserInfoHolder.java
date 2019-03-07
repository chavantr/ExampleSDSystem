package generalknowledge.mywings.com.smartdustbinsystem.models;

import java.util.List;

public class UserInfoHolder {

    private User user;

    private List<Dustbin> dustbin;

    public static UserInfoHolder getInstance() {
        return UserInfoHolderHelper.INSTANCE;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<Dustbin> getDustbin() {
        return dustbin;
    }

    public void setDustbin(List<Dustbin> dustbin) {
        this.dustbin = dustbin;
    }

    private static class UserInfoHolderHelper {
        static final UserInfoHolder INSTANCE = new UserInfoHolder();
    }

}
