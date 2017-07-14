import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by 12 on 2017/7/6.
 */
public class TestSpring {

    @Autowired
    TestService testService;

    @Test
    public void test(){


        Assert.assertEquals(testService.test2(111111), 1);
        Assert.assertEquals(testService.test2(111111), 1);





        Assert.assertEquals(testService.test4(), 1);
        Assert.assertEquals(testService.test4(), 1);


        Assert.assertEquals(testService.test5(), 1);
        Assert.assertEquals(testService.test5(), 1);

        Assert.assertEquals(testService.test6(), 1);
        Assert.assertEquals(testService.test6(), 1);

    }
}
