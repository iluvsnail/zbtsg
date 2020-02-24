package cn.smilegoo.hs.service

import java.text.SimpleDateFormat
import java.util.Date

import cn.hutool.extra.mail.{MailAccount, MailUtil}
import com.alibaba.fastjson.{JSONArray, JSONObject}
import javax.annotation.PostConstruct
import org.apache.commons.pool2.impl.GenericObjectPoolConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import redis.clients.jedis.exceptions.JedisConnectionException
import redis.clients.jedis.{Jedis, JedisPool}

import scala.collection.JavaConversions._


@Service
class HsService {


    val TK_TOOL = "tk:tool"


    val simpFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")


    val KEY_EXPIRES = 600


    @Autowired
    private val mailAccount:MailAccount = null

    private var pool: JedisPool = null
    @Autowired
    private val redisIP =  ""
    @Autowired
    private val redisPort:Int = 0


    @PostConstruct
    private def init(): Unit = {
        val config = new GenericObjectPoolConfig
        config.setMaxIdle(200)
        config.setMaxTotal(5120)
        config.setMaxWaitMillis(5000)
        config.setTestOnBorrow(true)
        config.setTestOnReturn(true)
        pool = new JedisPool(config, redisIP, redisPort,60000)
    }

    def reportPrice(platform: String, flightNo: String, orgDate: String, time: String, price: String, spare: String): String = {
        val redis = getRedisInstance(2)
        val key = TK_TOOL+":"+platform+":"+orgDate
        val hv = flightNo+" "+time
        val v = price+" "+spare
        val op = redis.hget(key,hv)
        if(!v.equals(op)){
            redis.hset(key,hv,v)
            var sm = orgDate+" "+time+" "+flightNo+"\n旧的：\n    "+op+"\n新的："
            redis.hkeys(key).foreach(k=>{
               sm+=("\n    "+k+"  "+redis.hget(key,k))
            })
            sendMail(sm)
        }
        redis.close()
        var nextTime = "2020-01-22"
        orgDate match {
            case "2020-01-22"=>{
                nextTime="2020-01-23"
            }
            case "2020-01-23"=>{
                nextTime = "2020-01-24"
            }
            case "2020-01-29"=>{
                nextTime = "2020-01-30"
            }
            case "2020-01-30"=>{
                nextTime = "2020-01-31"
            }
            case "2020-01-31"=>{
                nextTime = "2020-02-01"
            }
            case "2020-02-01"=>{
                nextTime = "2020-01-29"
            }
        }
        nextTime
    }

    def listPrices():JSONObject = {
        val redis = getRedisInstance(2)
        val jsa = new JSONArray()
        val rst = new JSONObject()
        redis.keys("*").foreach(day=>{
            val jso = new JSONObject()
            val jsa1 = new JSONArray()
            redis.hkeys(day).foreach(k=>{
                val v = redis.hget(day,k)
                jsa1.add(k+":"+v)
            })
            jso.put(day.substring(day.lastIndexOf(":")+1),jsa1)
            jsa.add(jso)
        })
        redis.close()
        rst.put("count", jsa.size())
        rst.put("messages",jsa)
        rst
    }

    private def getRedisInstance(db:Int):Jedis = {
        var jedis:Jedis = null
        try {
            jedis = pool.getResource
            jedis.select(db)
        }
        catch {
            case e: JedisConnectionException =>{
                e.printStackTrace()
                if (jedis != null) jedis.close()
            }
        }
        jedis
    }
    private def getRedisInstance():Jedis = {
        getRedisInstance(0)
    }
   private def getFormateNow: String = {
        simpFormat.format(new Date)
    }
    private def sendMail(content:String):Unit = {
        MailUtil.send(mailAccount,"cyl@gfire.cn","机票价格变啦",content,false)
    }
}
