package ifrs.edu.avaliacao_mnr.controller;

import ifrs.edu.avaliacao_mnr.model.TestEntity;
import ifrs.edu.avaliacao_mnr.repository.TestRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/test")
public class TestController {

    private final TestRepository testRepository;

    public TestController(TestRepository testRepository) {
        this.testRepository = testRepository;
    }

    @GetMapping
    public ResponseEntity<List<TestEntity>> getTestRecords() {
        return ResponseEntity.ok(testRepository.findAll());
    }
}
