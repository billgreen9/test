package com.tudou.util;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

public class RedisUtil {

	/**
	 * 非切片客户端链接
	 */
	private Jedis jedis;

	/**
	 * 非切片链接池
	 */
	private JedisPool jedisPool;

	/**
	 * 切片客户端链接
	 */
	private ShardedJedis shardedJedis;

	/**
	 * 切片链接池
	 */
	private ShardedJedisPool shardedJedisPool;
	private String ip = "172.16.205.186";

	public ShardedJedisPool getShardedJedisPool(List<String> addr) {
		// 池基本配置
		JedisPoolConfig config = new JedisPoolConfig();
		config.setMaxTotal(100);
		config.setMaxIdle(5);
		config.setTestOnBorrow(false);
		List<JedisShardInfo> shards = new ArrayList<JedisShardInfo>();
		for (String uri : addr) {
			// String ip = item.split(",")[0];
			// String port = item.split(",")[0];
			shards.add(new JedisShardInfo(uri));
		}
		// 构造池
		shardedJedisPool = new ShardedJedisPool(config, shards);
		return shardedJedisPool;
	}

	/**
	 * 构造函数
	 */
	public void init() {
		initialPool();
		initialShardedPool();
		shardedJedis = shardedJedisPool.getResource();
		jedis = jedisPool.getResource();
	}

	private void initialPool() {
		// 池基本配置
		JedisPoolConfig config = new JedisPoolConfig();
		config.setMaxTotal(20);
		config.setMaxIdle(5);
		config.setTestOnBorrow(false);
		jedisPool = new JedisPool(config, ip, 6379);
	}

	/**
	 * 初始化切片池
	 */
	private void initialShardedPool() {
		// 池基本配置
		JedisPoolConfig config = new JedisPoolConfig();
		config.setMaxTotal(100);
		config.setMaxIdle(5);
		config.setTestOnBorrow(false);
		// slave链接
		List<JedisShardInfo> shards = new ArrayList<JedisShardInfo>();
		shards.add(new JedisShardInfo(ip, 6379));
		// 构造池
		shardedJedisPool = new ShardedJedisPool(config, shards);

	}

	public static void main(String[] args) {
		RedisUtil cli = new RedisUtil();
		List<String> addr = new ArrayList<>();
		addr.add("tcp://10.10.22.140:6379/1");// /tcp://10.10.22.140:6379/1
		addr.add("tcp://10.10.22.140:6385/1");
		addr.add("tcp://10.10.22.140:6386/1");
		addr.add("tcp://10.10.22.140:6387/1");
		ShardedJedisPool pool = cli.getShardedJedisPool(addr);
		ShardedJedis jedis = pool.getResource();
		int start = 10001;
		int end = start+100;
		for (int i = start; i < end; i++) {
			String key = "key" + i;
			jedis.set(key, "value" + i);
			System.out.println(jedis.getShardInfo(key).toString());
		}
		jedis.close();
		pool.close();
		System.out.println("结束！");
	}

}
