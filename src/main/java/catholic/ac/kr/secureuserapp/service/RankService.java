package catholic.ac.kr.secureuserapp.service;

import catholic.ac.kr.secureuserapp.repository.RankRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RankService {
    private final RankRepository rankRepository;
}
