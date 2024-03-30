package knusearch.clear.survey.service;

import knusearch.clear.survey.model.CustomUserDetails;
import knusearch.clear.survey.repository.ParticipantRepository;
import knusearch.clear.survey.model.Participant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ParticipantDetailsService implements UserDetailsService {

    private final ParticipantRepository participantRepository;
    private final BCryptPasswordEncoder encoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Participant participant = participantRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        log.info(participant.getUsername());

        CustomUserDetails customUserDetails = new CustomUserDetails();
        customUserDetails.setUsername(participant.getUsername());
        customUserDetails.setPassword(encoder.encode(participant.getPassword()));
        customUserDetails.setParticipantId(participant.getId());

        return customUserDetails;
    }
}
