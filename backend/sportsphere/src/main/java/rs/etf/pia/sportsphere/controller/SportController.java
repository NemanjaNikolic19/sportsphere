package rs.etf.pia.sportsphere.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import rs.etf.pia.sportsphere.entity.Sport;
import rs.etf.pia.sportsphere.service.SportService;

import java.util.List;


@RestController
@RequestMapping("/api/sports")
public class SportController {
    
    private final SportService sportService;

    public SportController(SportService sportService) {
        this.sportService = sportService;
    }

    @GetMapping
    public List<Sport> getAllSports() {
        return sportService.getAllSports();
    }
    
}
