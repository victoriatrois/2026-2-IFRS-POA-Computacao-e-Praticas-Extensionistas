package ifrs.edu.avaliacao_mnr.controller;

import ifrs.edu.avaliacao_mnr.model.TestEntity;
import ifrs.edu.avaliacao_mnr.repository.TestRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/test")
@Tag(name = "Health and Test", description = "Database connectivity test endpoint")
public class TestController {

    private final TestRepository testRepository;

    public TestController(TestRepository testRepository) {
        this.testRepository = testRepository;
    }

    @GetMapping
    @Operation(summary = "Read test records", description = "Returns the test rows created by the initial database migration. Public endpoint for verifying database connectivity.")
    @ApiResponse(responseCode = "200", description = "Test records returned")
    public ResponseEntity<List<TestEntity>> getTestRecords() {
        return ResponseEntity.ok(testRepository.findAll());
    }
}
