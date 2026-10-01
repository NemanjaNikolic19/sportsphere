package rs.etf.pia.sportsphere.service;

import java.util.List;

import org.springframework.stereotype.Service;

import rs.etf.pia.sportsphere.entity.Sport;
import rs.etf.pia.sportsphere.repository.SportRepository;

@Service
public class SportService {
    
    private final SportRepository sportRepository;

    public SportService(SportRepository sportRepository) {
        this.sportRepository = sportRepository;
    }

    public List<Sport> getAllSports(){
        return sportRepository.findAll();
    }
}
