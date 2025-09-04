//package catholic.ac.kr.secureuserapp.websocket;
//
//import catholic.ac.kr.secureuserapp.security.JwtUtil;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.server.ServerHttpResponse;
//import org.springframework.http.server.ServletServerHttpRequest;
//import org.springframework.http.server.ServerHttpRequest;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.stereotype.Component;
//import org.springframework.web.socket.WebSocketHandler;
//import org.springframework.web.socket.server.HandshakeInterceptor;
//
//import java.util.Map;
//
//@Component
//@RequiredArgsConstructor
//public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {
//    private final JwtUtil jwtUtil;
//
//    @Override
//    public boolean beforeHandshake(
//            ServerHttpRequest request,
//            ServerHttpResponse response,
//            WebSocketHandler wsHandler,
//            Map<String, Object> attributes
//    ) throws Exception {
//        if (request instanceof ServletServerHttpRequest servletRequest) {
//            String token = servletRequest.getServletRequest().getParameter("token");
//            if (token != null && jwtUtil.isTokenValid(token)) {
//                Authentication authentication = jwtUtil.getAuthentication(token);
//                SecurityContextHolder.getContext().setAuthentication(authentication);
//                attributes.put("username", authentication.getName());
//                return true;
//            }
//        }
//        response.setStatusCode(HttpStatus.UNAUTHORIZED); // hoặc HttpStatus.FORBIDDEN
//        return false;
//    }
//
//
//    @Override
//    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
//                               WebSocketHandler wsHandler, Exception ex) {
//    }
//}
