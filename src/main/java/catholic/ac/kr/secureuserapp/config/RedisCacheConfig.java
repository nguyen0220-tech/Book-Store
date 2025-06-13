//package catholic.ac.kr.secureuserapp.config;
//
//import org.springframework.cache.annotation.EnableCaching;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.redis.cache.RedisCacheConfiguration;
//
//import java.time.Duration;
//
//@Configuration
//@EnableCaching //Bật cơ chế caching
//public class RedisCacheConfig {
//
//    @Bean
//    public RedisCacheConfiguration cacheConfiguration() {
//        return RedisCacheConfiguration.defaultCacheConfig()
//                .entryTtl(Duration.ofMinutes(10)) // TTL: dữ liệu cache sống 10 phút
//                .disableCachingNullValues(); // Không cache nếu return null
//    }
//}


