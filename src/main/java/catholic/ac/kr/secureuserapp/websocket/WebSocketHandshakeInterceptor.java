//package catholic.ac.kr.secureuserapp.websocket;
//
//import catholic.ac.kr.secureuserapp.security.JwtUtil;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.security.core.Authentication;
//import org.springframework.stereotype.Component;
//import org.springframework.web.socket.WebSocketHandler;
//import org.springframework.web.socket.server.HandshakeInterceptor;
//import org.springframework.http.server.ServerHttpRequest;
//import org.springframework.http.server.ServerHttpResponse;
//
//import java.util.Map;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {
//
//    private final JwtUtil jwtUtil;
//
//    @Override
//    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
//                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
//        String query = request.getURI().getQuery();
//        String token = null;
//        if (query != null) {
//            for (String param : query.split("&")) {
//                if (param.startsWith("token=")) {
//                    token = param.substring("token=".length());
//                    break;
//                }
//            }
//        }
//
//        if (token != null && token.startsWith("Bearer ")) token = token.substring(7);
//
//        if (token != null) {
//            Authentication auth = jwtUtil.getAuthentication(token);
//            attributes.put("user", auth);
//            return true;
//        }
//        return false;
//    }
//
//    @Override
//    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
//                               WebSocketHandler wsHandler, Exception ex) {}
//}
