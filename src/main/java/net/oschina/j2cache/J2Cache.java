///**
// * Copyright (c) 2015-2017, Winter Lau (javayou@gmail.com).
// * <p>
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// * <p>
// * http://www.apache.org/licenses/LICENSE-2.0
// * <p>
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package net.oschina.j2cache;
//
//import net.oschina.j2cache.redis.RedisPubSubClusterPolicy;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.util.Properties;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//
///**
// * J2Cache 的缓存入口
// * @author Winter Lau(javayou@gmail.com)
// */
//public class J2Cache {
//
//	private final static Logger log = LoggerFactory.getLogger(J2Cache.class);
//
//	private final static String CONFIG_FILE = "/j2cache.properties";
//
//	private final static CacheChannel channel;
//	private static ClusterPolicy policy; //不同的广播策略
//
//	private static String serializer;
//
//	static {
//		try {
//			initFromConfig();
//			/* 初始化缓存接口 */
//			channel = new CacheChannel(){
//				//为了避免发送广播的堵塞或者延迟导致的应用响应速度慢，因此采用线程方式发送
//				ExecutorService threadPool = Executors.newCachedThreadPool();
//
//				@Override
//				public void sendClearCmd(String region) {
//					threadPool.execute(()->policy.sendClearCmd(region));
//				}
//
//				@Override
//				public void sendEvictCmd(String region, String...keys) {
//					threadPool.execute(()->policy.sendEvictCmd(region, keys));
//				}
//
//				@Override
//				public void close() {
//					threadPool.shutdownNow();
//					policy.disconnect();
//					CacheProviderHolder.shutdown();
//				}
//			};
//		} catch (IOException e) {
//			throw new CacheException("Failed to load j2cache configuration " + CONFIG_FILE, e);
//		}
//	}
//
//	/**
//	 * 返回缓存操作接口
//	 * @return CacheChannel
//	 */
//	public static CacheChannel getChannel(){
//		return channel;
//	}
//
//	/**
//	 * 返回配置中定义的序列化方式
//	 * @return 序列化方式的名称(fst,kyro,java)
//	 */
//	public static String getSerializer() {
//		return serializer;
//	}
//
//	/**
//	 * 加载配置
//	 * @return
//	 * @throws IOException
//	 */
//	private static void initFromConfig() throws IOException {
//		try(InputStream configStream = getConfigStream()){
//			Properties props = new Properties();
//			props.load(configStream);
//			serializer = props.getProperty("j2cache.serialization");
//			//初始化两级的缓存管理
//			CacheProviderHolder.initCacheProvider(props, (region, key)->{
//				//当一级缓存中的对象失效时，自动清除二级缓存中的数据
//				try {
//					CacheProviderHolder.evict(CacheProviderHolder.LEVEL_2, region, key);
//					log.debug(String.format("Level 1 cache object expired, evict level 2 cache object [%s,%s]", region, key));
//				} catch (IOException e){
//					log.error(String.format("Failed to evict level 2 cache object [%s:%s]",region,key), e);
//				}
//				if(policy != null)
//					policy.sendEvictCmd(region, key);
//			});
//
//			String cache_broadcast = props.getProperty("j2cache.broadcast");
//			if ("redis".equalsIgnoreCase(cache_broadcast)) {
//				String channel = props.getProperty("redis.channel");
////				policy = ClusterPolicyFactory.redis(channel, CacheProviderHolder.getRedisClient(), props);//.getInstance();
////				RedisPubSubClusterPolicy policy = new RedisPubSubClusterPolicy(name, redis);
////				policy.connect(props);
//			}
//
//			log.info("Using cluster policy : " + policy.getClass().getName());
//		}
//	}
//
//	/**
//	 * get j2cache properties stream
//	 * @return
//	 */
//	private static InputStream getConfigStream() {
//		log.info("Load J2Cache Config File : [{}].", CONFIG_FILE);
//		InputStream configStream = J2Cache.class.getResourceAsStream(CONFIG_FILE);
//		if(configStream == null)
//			configStream = J2Cache.class.getClassLoader().getParent().getResourceAsStream(CONFIG_FILE);
//		if(configStream == null)
//			throw new CacheException("Cannot find " + CONFIG_FILE + " !!!");
//		return configStream;
//	}
//
//}
