package knusearch.clear.jpa.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import knusearch.clear.jpa.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DateService {

    private final MemberRepository memberRepository; //나중에 필요한 repository로 변경할 것

    @Transactional
    public LocalDate currentDate() {
        //LocalDate는 변경 불가능한(immutable) 객체이므로 여러 스레드에서 안전하게 공유될 수 있습니다.
        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String dateString = currentDate.format(formatter);

        return currentDate;
    }

    @Transactional
    public LocalDate minDate() {
        //DAO(respository)이용해서 minDate 받아오기

        return LocalDate.of(2000, 1, 1);
    }

}
