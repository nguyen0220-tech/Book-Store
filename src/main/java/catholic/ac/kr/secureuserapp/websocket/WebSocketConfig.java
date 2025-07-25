package catholic.ac.kr.secureuserapp.websocket;

import catholic.ac.kr.secureuserapp.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Autowired
    private WebSocketHandshakeInterceptor handshakeInterceptor;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Cho phép client subscribe các topic này
        config.enableSimpleBroker("topic","queue"); // Gửi đến client

        // Client sẽ gửi dữ liệu vào đây
        config.setApplicationDestinationPrefixes("/app"); // Client gửi đến server

        config.setUserDestinationPrefix("/user"); // Cho từng user riêng biệt
    }


    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Định nghĩa endpoint để kết nối WebSocket từ client
        registry.addEndpoint("/ws")
                .addInterceptors(handshakeInterceptor)
                .setAllowedOrigins("*")
                .withSockJS();

    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    Authentication auth = (Authentication) accessor.getSessionAttributes().get("user");
                    System.out.println(("Auth in attributes: " + accessor.getSessionAttributes().get("user")));

                    if (auth != null) {
                        accessor.setUser(auth);
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    }
                }
                return message;
            }
        });
    }

}
