import com.jayqqaa12.j2cache.core.J2Cache;

/**
 * Created by 12 on 2017/7/14.
 */
public class TestLock {

    public static void main(String[] args) {

        boolean flag = J2Cache.lock().isLock("sb", 10);

        for (int i = 0; i < 10; i++) {
            J2Cache.lock().spinLock("sb", 10);
            System.out.println(flag);
        }

        J2Cache.close();

    }
}
