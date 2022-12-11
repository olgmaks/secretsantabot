package ua.bot.secretsaintnicholas;

import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Test;
import ua.bot.secretsaintnicholas.models.UserModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UnitTest {


    private final List<UserModel> data = new ArrayList<UserModel>();


    @Test
    public void test() {

        

        data.add(UserModel.builder().id(9010L).build());
        data.add(UserModel.builder().id(400L).build());
        data.add(UserModel.builder().id(107L).build());
        data.add(UserModel.builder().id(12300L).build());
        data.add(UserModel.builder().id(10120L).build());
        data.add(UserModel.builder().id(1700L).build());
        data.add(UserModel.builder().id(112L).build());
        data.add(UserModel.builder().id(1001L).build());
        data.add(UserModel.builder().id(120L).build());
        data.add(UserModel.builder().id(105430L).build());
        data.add(UserModel.builder().id(1241L).build());

        Map<Long, Long> map = new HashMap<>();

        for (UserModel gifter : new ArrayList<>(data)) {

            UserModel receiver;
            do {
                receiver = data.get(RandomUtils.nextInt(0, data.size()));
            }
            while (receiver.getId().equals(gifter.getId()));

            map.put(gifter.getId(), receiver.getId());
            data.remove(receiver);
        }

        map.forEach((k,v) -> System.out.printf("%s    -     %s\n", k, v));
    }
}
