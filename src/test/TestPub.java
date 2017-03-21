import com.jayqqaa12.j2cache.core.CacheConstans;
import com.jayqqaa12.j2cache.util.Command;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.util.SafeEncoder;


public class TestPub {
    public static void main(String[] args) throws Exception {
        Jedis jedis = getJedis();
        System.out.println(jedis.keys("*"));
        User user = new User();
        user.setName("asdfsa");


        //发布
        long loop = 0;
        while (loop++ < 200) {

            Command cmd = new Command(Command.OPT_DELETE_KEY, "12", "12");
            jedis.publish(SafeEncoder.encode(CacheConstans.REDIS_CHANNEL), cmd.toBuffers());
            Thread.sleep(500);
        }
    }

    private static JedisPool pool;

    static {
        // 创建jedis池配置实例
        JedisPoolConfig config = new JedisPoolConfig();
        // 设置池配置项值
        config.setMaxTotal(100);
        config.setMaxIdle(20);
        config.setMaxWaitMillis(3000);
        config.setTestOnReturn(true);
        config.setTestOnBorrow(true);
        //need password
        pool = new JedisPool(config, "106.14.37.173", 6379, 2000, "Hyxx201611Test",1);
    }

    /**
     * 单个jedis*
     *
     * @return
     */
    public static Jedis getJedis() {
        Jedis Jedis = getPool().getResource();
        return Jedis;
    }

    public static JedisPool getPool() {
        return pool;
    }
}
