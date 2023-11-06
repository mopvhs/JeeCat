package com.jeesite.modules.cat.cache;

/**
 * @author qunlin.yi
 * @since Created in 17:23 2018/7/2
 */
public interface CacheService {

    void init();

//    void destroy();

    boolean tryLock(String key, int seconds);

    /**
     * 毫秒级锁
     * @param key
     * @param milliseconds
     * @return
     */
    boolean tryLockWithMilliseconds(String key, int milliseconds);

    boolean releaseLock(String key);

    String get(String key);

    boolean set(String key, String value);

    /**
     * 设置redis值
     *
     * @param key   redis key
     * @param value redis value
     * @param nxxx  [NX|XX] Only set the key if it does not already exist|Only set the key if it already exist
     * @param expx  [EX|PX] seconds|milliseconds
     * @param time  过期时间
     * @return String
     */
    String set(final String key, final String value, final String nxxx, final String expx, final long time);

    /**
     * 设置redis值
     *
     * @param key   redis key
     * @param value redis value
     * @param expx  [EX|PX] seconds|milliseconds
     * @param time  过期时间
     * @return String
     */
    String set(final String key, final String value, final String expx, final long time);


    /**
     * 设置过期时间
     *
     * @param key     redis key
     * @param seconds 过期时间 单位：秒
     * @return long
     */
    Long expire(final String key, final int seconds);

    /**
     * 查看redis key 是否设置 过期时间
     *
     * @param key redis key
     * @return long
     */
    Long ttl(final String key);

    Long incr(String key);

    Long incrBy(String key, Long incrBy);

    boolean setWithExpireTime(String key, String value, int seconds);

    boolean delete(String key);
}
