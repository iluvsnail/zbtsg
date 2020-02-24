package cn.smilegoo.hs.component

import cn.hutool.extra.mail.MailAccount
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.{Bean, Configuration}
import org.springframework.web.client.RestTemplate

@Configuration
@EnableConfigurationProperties
class CloudConfig {
    @Value("${redis.host.ip}")
    val redisIP:String = null
    @Value("${redis.host.port}")
    val redisPort:Int = 0

    @Value("${mail.host}")
    val host:String = null
    @Value("${mail.port}")
    val port:Int = 0
    @Value("${mail.user}")
    val user:String = null
    @Value("${mail.pass}")
    val pass:String = null
    @Value("${mail.from}")
    val from:String = null
    @Value("${mail.ssl}")
    val ssl:Boolean = false



    @Bean
    def getRestTemplate: RestTemplate = {
        new RestTemplate()
    }

    @Bean
    def getRedisIP:String = {
        redisIP
    }
    @Bean
    def getRedisPort:Int = {
        redisPort
    }

    @Bean
    def getMailAccount():MailAccount = {
        val ma = new MailAccount()
        ma.setHost(host)
        ma.setPort(port)
        ma.setFrom(from)
        ma.setUser(user)
        ma.setPass(pass)
        ma.setSslEnable(ssl)
    }
}
