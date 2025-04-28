package catholic.ac.kr.secureuserapp.security.userdetails;

import catholic.ac.kr.secureuserapp.model.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@AllArgsConstructor
@Getter
public class MyUserDetails implements UserDetails {
    private final User user;

    // Lấy danh sách quyền của người dùng
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Mỗi quyền (role) sẽ được wrap thành SimpleGrantedAuthority
        return Collections.singletonList(new SimpleGrantedAuthority(user.getRole()));
    }

    // Lấy password để Spring Security so sánh
    @Override
    public String getPassword() {
        return user.getPassword();
    }

    // Lấy username để Spring Security xác thực
    @Override
    public String getUsername() {
        return user.getUsername();
    }

    // Xác nhận tài khoản có hết hạn không (ví dụ bạn muốn khóa tài khoản sau 1 năm chẳng hạn)
    @Override
    public boolean isAccountNonExpired() {
        return true; // luôn cho là không hết hạn
    }


    // Xác nhận tài khoản có bị khóa không (ví dụ nhập sai password nhiều lần thì khóa)
    @Override
    public boolean isAccountNonLocked() {
        return true;  // luôn cho là không bị khóa
    }

    // Xác nhận credentials (mật khẩu) có hết hạn không (ví dụ mật khẩu hết hạn sau 90 ngày)
    @Override
    public boolean isCredentialsNonExpired() {
        return true; // luôn cho là không hết hạn mật khẩu
    }

    // Xác nhận tài khoản có kích hoạt (enabled) không (xác thực email xong mới enabled)
    @Override
    public boolean isEnabled() {
        return user.isEnabled();
    }

}
