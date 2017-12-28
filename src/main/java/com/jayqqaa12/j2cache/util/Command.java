/**
 *
 */
package com.jayqqaa12.j2cache.util;

import com.jayqqaa12.j2cache.CacheConstans;
import com.jayqqaa12.j2cache.serializer.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.util.Random;

/**
 * copy
 *
 * 命令消息封装
 * 格式：
 * 第1个字节为命令代码，长度1 [OPT]
 * 第2、3个字节为region长度，长度2 [R_LEN]
 * 第4、N 为 region 值，长度为 [R_LEN]
 * 第N+1、N+2 为 key 长度，长度2 [K_LEN]
 * 第N+3、M为 key值，长度为 [K_LEN]
 */
public class Command {

    private final static Logger log = LoggerFactory.getLogger(Command.class);

    private final static int SRC_ID = genRandomSrc(); //命令源标识，随机生成

    public final static byte OPT_DELETE_KEY = 0x01;    //删除缓存
    public final static byte OPT_CLEAR_KEY = 0x02;        //清除缓存

    private int src;
    private byte operator;
    private String region;
    private Object key;

    private static int genRandomSrc() {
        long ct = System.currentTimeMillis();
        Random random = new Random(ct);
        return (int) (random.nextInt(10000) * 1000 + ct % 1000);
    }


    public Command(byte operator, String region, Object key) {
        this.operator = operator;
        if (region == null)
            region = CacheConstans.EHCACHE_DEFAULT_REGION;
        this.region = region;
        this.key = key;
        this.src = SRC_ID;
    }

    public byte[] toBuffers() {
        byte[] keyBuffers = null;
        try {
            keyBuffers = SerializationUtils.serialize(key);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        int r_len = region.getBytes().length;
        int k_len = keyBuffers.length;

        byte[] buffers = new byte[9 + r_len + k_len];
        System.arraycopy(int2bytes(this.src), 0, buffers, 0, 4);
        int idx = 4;
        buffers[idx] = operator;
        buffers[++idx] = (byte) (r_len >> 8);
        buffers[++idx] = (byte) (r_len & 0xFF);
        System.arraycopy(region.getBytes(), 0, buffers, ++idx, r_len);
        idx += r_len;
        buffers[idx++] = (byte) (k_len >> 8);
        buffers[idx++] = (byte) (k_len & 0xFF);
        System.arraycopy(keyBuffers, 0, buffers, idx, k_len);
        return buffers;
    }

    public static Command parse(byte[] buffers) {
        Command cmd = null;
        try {
            int idx = 4;
            byte opt = buffers[idx];
            int r_len = buffers[++idx] << 8;
            r_len += buffers[++idx];
            if (r_len > 0) {
                String region = new String(buffers, ++idx, r_len);
                idx += r_len;
                int k_len = buffers[idx++] << 8;
                k_len += buffers[idx++];
                if (k_len > 0) {
                    byte[] keyBuffers = new byte[k_len];
                    System.arraycopy(buffers, idx, keyBuffers, 0, k_len);
                    Serializable key = SerializationUtils.deserialize(keyBuffers);
                    cmd = new Command(opt, region, key);
                    cmd.src = bytes2int(buffers);
                }
            }
        } catch (Exception e) {
            log.error("Unabled to parse received command.", e);
        }
        return cmd;
    }

    private static byte[] int2bytes(int i) {
        byte[] b = new byte[4];

        b[0] = (byte) (0xff & i);
        b[1] = (byte) ((0xff00 & i) >> 8);
        b[2] = (byte) ((0xff0000 & i) >> 16);
        b[3] = (byte) ((0xff000000 & i) >> 24);

        return b;
    }

    private static int bytes2int(byte[] bytes) {
        int num = bytes[0] & 0xFF;
        num |= ((bytes[1] << 8) & 0xFF00);
        num |= ((bytes[2] << 16) & 0xFF0000);
        num |= ((bytes[3] << 24) & 0xFF000000);
        return num;
    }

    public boolean isLocalCommand() {
        return this.src == SRC_ID;
    }

    public byte getOperator() {
        return operator;
    }


    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public Object getKey() {
        return key;
    }

    public void setKey(Serializable key) {
        this.key = key;
    }

}
