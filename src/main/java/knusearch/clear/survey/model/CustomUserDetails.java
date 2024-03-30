package knusearch.clear.survey.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Getter
@Setter
public class CustomUserDetails implements UserDetails {
    private String username;
    private String password;
    private Long participantId;
    private Collection<? extends GrantedAuthority> authorities;

    @Override
    public boolean isAccountNonExpired() { // 계정이 만료되지 않았는지 여부를 반환
        return true;
    }

    @Override
    public boolean isAccountNonLocked() { // 계정이 잠겨있지 않은지 여부를 반환
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() { // 인증 정보(보통 비밀번호)가 만료되지 않았는지 여부를 반환
        return true;
    }

    @Override
    public boolean isEnabled() { // 계정이 사용 가능 상태인지
        return true;
    }
}
