package com.jeesite.modules.cat.cache;

import com.google.common.collect.Sets;
import com.jeesite.modules.cat.config.RedisProperies;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;


/**
 * 基于redis的分布式锁
 * @author qunlin.yi
 * @since Created in 17:26 2018/7/2
 */
@Service(value = "cacheService")
@EnableConfigurationProperties(RedisProperies.class) /** https://blog.csdn.net/AlbenXie/article/details/105709976 */
public class RedisCacheServiceImpl implements CacheService {

    private static final String LOCK_SUCCESS = "OK";
    private static final String SET_IF_NOT_EXIST = "NX";
    private static final String SET_WITH_EXPIRE_TIME = "EX";
    private static final String SET_WITH_EXPIRE_TIME_MILLISECONDS = "PX";


    private static final int TIME_OUT = 200;
    private static Logger LOGGER = LoggerFactory.getLogger(RedisCacheServiceImpl.class);
    //    private JedisCluster jedisCluster;
    private volatile static JedisPool jedisPool;

    @Resource
    private RedisProperies redisProperies;

    @Override
    @PostConstruct
    public void init() {
        String redisIp = redisProperies.getHost();
        int redisPost = redisProperies.getPort();
        GenericObjectPoolConfig config = initGenericObjectPoolConfig(redisProperies);

        checkNotNull(config);
        checkNotNull(redisIp);
//        List<String> ips = Arrays.asList(redisIp.split(","));
        // todo 先不使用集群
        List<String> ips = Arrays.asList(redisIp + ":" + redisPost);
        if (ips.size() == 0) {
            throw new RuntimeException("The redis ip is null");
        }
        Set<HostAndPort> hostAndPorts = Sets.newHashSet();
        for (String item : ips) {
            String address;
            int port;
            try {
                String[] temp = item.split(":");
                address = temp[0];
                port = Integer.parseInt(temp[1]);
                LOGGER.info("init redis node, address:{}, port:{}", address, port);
            } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
                // 缺少端口或者非法格式
                continue;
            }
            hostAndPorts.add(new HostAndPort(address, port));
        }
        //String pwd = SecretKeyClient.getPassword(System.getProperty("datakeeper.application.redis.robot.key_name"));
        //jedisCluster = new JedisCluster(hostAndPorts, TIME_OUT, 1000, 3, pwd, config);

        jedisPool = getJedisPool();

    }

    private JedisPool getJedisPool() {
        if (jedisPool == null) {
            synchronized (RedisCacheServiceImpl.class) {
                if (jedisPool == null) {
                    jedisPool = new JedisPool(redisProperies, redisProperies.getHost(),
                            redisProperies.getPort(), redisProperies.getConnectionTimeout(),
                            redisProperies.getSoTimeout(), redisProperies.getPassword(),
                            redisProperies.getDatabase(), redisProperies.getClientName(),
                            redisProperies.isSsl(), redisProperies.getSslSocketFactory(),
                            redisProperies.getSslParameters(), redisProperies.getHostnameVerifier());
                }
            }
        }
        return jedisPool;
    }

    @Override
    public boolean tryLock(String key, int seconds) {
        Jedis resource = jedisPool.getResource();
        try {
            return LOCK_SUCCESS.equals(resource.set(key, "1", SET_IF_NOT_EXIST, SET_WITH_EXPIRE_TIME, seconds));
        } finally {
            resource.close();
        }
    }

    @Override
    public boolean tryLockWithMilliseconds(String key, int milliseconds) {
        Jedis resource = jedisPool.getResource();

        try {
            return LOCK_SUCCESS.equals(resource.set(key,
                    "1", SET_IF_NOT_EXIST, SET_WITH_EXPIRE_TIME_MILLISECONDS, milliseconds));
        } finally {
            resource.close();
        }

    }

    @Override public boolean releaseLock(String key) {
        Jedis resource = jedisPool.getResource();

        try {
            Long del = resource.del(key);
            return del == 1;
        } catch (Exception e) {
            LOGGER.error("payment release lock try del redis key failed ,key:{}", key);
            return false;
        } finally {
            resource.close();
        }
    }

    @Override
    public String get(String key) {
        Jedis resource = jedisPool.getResource();
        try {
            return resource.get(key);
        } finally {
            resource.close();
        }
    }

    @Override
    public boolean set(String key, String value) {
        Jedis resource = jedisPool.getResource();

        try {
            return LOCK_SUCCESS.equals(resource.set(key, value));
        } finally {
            resource.close();
        }

    }

    public static GenericObjectPoolConfig initGenericObjectPoolConfig(RedisProperies redisProperies) {
        GenericObjectPoolConfig config = new GenericObjectPoolConfig();
        config.setMinIdle(redisProperies.getMinIdle());
        config.setMaxIdle(redisProperies.getMaxIdle());
        config.setMaxTotal(redisProperies.getMaxTotal());

        return config;
    }

    @Override
    public String set(String key, String value, String nxxx, String expx, long time) {
        Jedis resource = jedisPool.getResource();

        try {
            return resource.set(key, value, nxxx, expx, time);
        } finally {
            resource.close();
        }

    }

    @Override
    public String set(String key, String value, String expx, long time) {
        Jedis resource = jedisPool.getResource();

        try {
            return resource.set(key, value, SET_IF_NOT_EXIST, expx, time);
        } finally {
            resource.close();
        }
    }

    @Override
    public Long expire(String key, int seconds) {
        Jedis resource = jedisPool.getResource();

        try {
            return resource.expire(key, seconds);
        } finally {
            resource.close();
        }

    }

    @Override
    public Long ttl(String key) {
        Jedis resource = jedisPool.getResource();

        try {
            return resource.ttl(key);
        } finally {
            resource.close();
        }
    }

    @Override
    public Long incr(String key) {
        Jedis resource = jedisPool.getResource();

        try {
            return resource.incr(key);
        } finally {
            resource.close();
        }
    }

    @Override
    public Long incrBy(String key, Long incrBy) {
        Jedis resource = jedisPool.getResource();

        try {
            return resource.incrBy(key, incrBy);
        } finally {
            resource.close();
        }

    }
}