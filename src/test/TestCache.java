import com.jayqqaa12.j2cache.core.J2Cache;


public class TestCache {

    public static void main(String[] args) throws Exception {
        System.setProperty("java.net.preferIPv4Stack", "true"); //Disable IPv6 in JVM



        User user1 = new User();
        user1.setName("12");
        user1.setAge("34");

        J2Cache.set("session", "a12",user1);//region
        J2Cache.set("session", "a13",user1);//region

        J2Cache.set("a13",user1);//region

        Object object =J2Cache.get("session", "a12");
        System.out.println("+++++a12 value :"+object);


        object =J2Cache.get("session", "a12");
        System.out.println("+++++a12 value :" + object);


        /*object =cacheKit.get("session", "a12");
        System.out.println("+++++a12 value after delete:"+object);
        object =cacheKit.get("session", "a13");
        System.out.println("+++++a13 value after delete:" + object);
*/

       /* long loop = 0;
        while (loop++ < 10) {
            Thread.sleep(100000);
        }*/

        /*int i=0;
        while (i++<100){
            System.out.println("=====================  "+i);
            cacheKit.remove("session", "a12");
            Thread.sleep(100);
        }*/

        J2Cache.close();
    }
}
